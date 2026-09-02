package com.orca.app.util

import android.content.Context
import android.util.Log
import java.io.File

/**
 * One-shot move of on-disk state from the old `i*` names to `orca*`.
 *
 * The identifiers renamed alongside this class are data contracts, not labels:
 * a SharedPreferences file name, a Room database file name and two directories
 * under `filesDir` all decide which bytes the app reads at startup. Renaming
 * them in source without moving the files would leave every existing install
 * looking at paths that do not exist — chat history, alarms and settings would
 * appear to vanish while the old files sat untouched on disk. No error, no
 * crash, just an app that looks freshly installed.
 *
 * (The earlier `minis` -> `i` rebrand shipped without this step, which is why
 * nothing here has a `minis` leg: those files are long gone from any install
 * that has run since.)
 *
 * Must run before anything opens a database or reads preferences — see the
 * call at the top of [com.orca.app.OrcaApp.onCreate].
 */
object BrandMigration {

    private const val TAG = "BrandMigration"
    private const val MARKER_PREFS = "orca_brand_migration"
    private const val KEY_DONE = "i_to_orca_done"

    /** Plain SharedPreferences: safe to move as files. */
    private val PLAIN_PREFS = listOf(
        "i_alarms_prefs" to "orca_alarms_prefs",
        "i_memory_prefs" to "orca_memory_prefs",
        "i_scheduled_notifications" to "orca_scheduled_notifications",
        "i_scheduled_tasks_prefs" to "orca_scheduled_tasks_prefs",
        "i_config_permission" to "orca_config_permission",
        "i_auto_compact_prefs" to "orca_auto_compact_prefs",
        "i_enhanced_cache_prefs" to "orca_enhanced_cache_prefs",
        "i_fast_mode_prefs" to "orca_fast_mode_prefs",
        "i_settings" to "orca_settings",
    )

    /** Room / SQLite files. Journal siblings must travel with the main file. */
    private val DATABASES = listOf(
        "i.db" to "orca.db",
        "i-config-audit.db" to "orca-config-audit.db",
    )

    /** Directories directly under filesDir. */
    private val FILE_DIRS = listOf(
        "i-global" to "orca-global",
        "i-sessions" to "orca-sessions",
    )

    fun runIfNeeded(context: Context) {
        val marker = context.getSharedPreferences(MARKER_PREFS, Context.MODE_PRIVATE)
        if (marker.getBoolean(KEY_DONE, false)) return

        val moved = runCatching { migrate(context) }
            .onFailure { Log.e(TAG, "migration failed — leaving old files in place", it) }
            .getOrDefault(0)

        // Mark done even on partial success: a retry would find the old paths
        // already consumed and could clobber freshly written data. Anything that
        // failed is still on disk and recoverable by hand.
        marker.edit().putBoolean(KEY_DONE, true).apply()
        Log.i(TAG, "brand migration complete, $moved item(s) moved")
    }

    private fun migrate(context: Context): Int {
        var moved = 0
        val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")

        for ((old, new) in PLAIN_PREFS) {
            if (moveFile(File(prefsDir, "$old.xml"), File(prefsDir, "$new.xml"))) moved++
        }

        for ((old, new) in DATABASES) {
            val oldDb = context.getDatabasePath(old)
            val newDb = context.getDatabasePath(new)
            if (moveFile(oldDb, newDb)) {
                moved++
                // -wal / -shm hold committed pages not yet checkpointed into the
                // main file. Leaving them behind can lose the newest writes.
                for (suffix in listOf("-wal", "-shm", "-journal")) {
                    moveFile(
                        File(oldDb.parentFile, oldDb.name + suffix),
                        File(newDb.parentFile, newDb.name + suffix),
                    )
                }
            }
        }

        for ((old, new) in FILE_DIRS) {
            if (moveFile(File(context.filesDir, old), File(context.filesDir, new))) moved++
        }

        if (migrateDeviceIdentity(context)) moved++
        if (migrateConfigEnabledKey(context)) moved++
        return moved
    }

    /**
     * `i_device_identity` is an EncryptedSharedPreferences store. Its Tink keyset
     * is bound to the store, so it is copied through the API rather than moved as
     * a file — a rename that the crypto layer does not expect would surface as a
     * decryption failure, which [EncryptedPrefsFactory] would then "repair" by
     * wiping the store.
     */
    private fun migrateDeviceIdentity(context: Context): Boolean {
        val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        if (!File(prefsDir, "i_device_identity.xml").exists()) return false

        return runCatching {
            val old = EncryptedPrefsFactory.safeCreate(context, "i_device_identity")
            val new = EncryptedPrefsFactory.safeCreate(context, "orca_device_identity")
            val editor = new.edit()
            for ((key, value) in old.all) {
                when (value) {
                    is String -> editor.putString(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                }
            }
            editor.apply()
            old.edit().clear().apply()
            File(prefsDir, "i_device_identity.xml").delete()
            Log.i(TAG, "migrated encrypted device identity")
            true
        }.getOrElse {
            // A lost device id regenerates on next use; not worth failing the run.
            Log.w(TAG, "device identity migration skipped: ${it.message}")
            false
        }
    }

    /** The permission flag key inside the (already moved) config prefs. */
    private fun migrateConfigEnabledKey(context: Context): Boolean {
        val prefs = context.getSharedPreferences("orca_config_permission", Context.MODE_PRIVATE)
        if (!prefs.contains("i_config_enabled")) return false
        val value = prefs.getBoolean("i_config_enabled", false)
        prefs.edit()
            .putBoolean("orca_config_enabled", value)
            .remove("i_config_enabled")
            .apply()
        return true
    }

    private fun moveFile(from: File, to: File): Boolean {
        if (!from.exists()) return false
        if (to.exists()) {
            Log.w(TAG, "skip ${from.name}: ${to.name} already exists")
            return false
        }
        val ok = from.renameTo(to)
        if (ok) Log.i(TAG, "moved ${from.name} -> ${to.name}")
        else Log.w(TAG, "failed to move ${from.name} -> ${to.name}")
        return ok
    }
}
