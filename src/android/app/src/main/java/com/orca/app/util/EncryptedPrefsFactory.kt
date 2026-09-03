package com.orca.app.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File
import java.security.KeyStore

/**
 * T-android-keystore-aead-fail: self-healing wrapper around
 * [EncryptedSharedPreferences.create].
 *
 * The default flow throws `AEADBadTagException` (wrapped as
 * `GeneralSecurityException`) on launch when the AndroidKeystore master
 * key can no longer decrypt the Tink keyset blob — observed on Samsung
 * One UI / Android 16 after backup-restore or biometric re-enroll. The
 * exception bubbles to the main thread and the app dies in a relaunch
 * loop because every cold start hits the same lazy init.
 *
 * Strategy:
 *  1. Try the normal create.
 *  2. On any crypto error: drop the encrypted XML file + the on-disk
 *     Tink keyset prefs file + the AndroidKeystore alias, then retry
 *     once. The user loses stored credentials (they need to re-paste
 *     their API key / re-login OAuth) but the app boots.
 *  3. If recreate still fails: fall back to an in-memory store so the
 *     rest of the app sees an empty, read-write SharedPreferences and
 *     never crashes.
 *
 * Step 3 used to write a plain-text XML file. That turned a Keystore
 * fault into silent cleartext persistence of API keys and OAuth refresh
 * tokens, readable by `adb backup` and by any same-UID process — a worse
 * outcome than the crash it was avoiding. It is now [InMemorySharedPreferences]:
 * same crash-free contract, but the values never reach disk and die with
 * the process. Callers that persist secrets should treat a fallback store
 * as empty and prompt the user to re-authenticate.
 *
 * Any `_plain_fallback` files left by earlier builds are deleted on first
 * use so old cleartext copies do not linger.
 */
object EncryptedPrefsFactory {
    private const val TAG = "EncryptedPrefsFactory"

    fun safeCreate(context: Context, fileName: String): SharedPreferences {
        purgeLegacyPlaintextFallback(context, fileName)

        runCatching { return build(context, fileName) }
            .onFailure { Log.w(TAG, "first create($fileName) failed: ${it.message}") }

        // First wipe attempt — the encrypted XML + Tink keyset blob +
        // master-key alias all need to go. The Tink keyset lives in its
        // own __androidx_security_crypto_encrypted_prefs__ file keyed
        // by the SP file name; drop both so create() regenerates them.
        wipeEncryptedState(context, fileName)

        runCatching { return build(context, fileName) }
            .onFailure {
                Log.e(TAG, "rebuild($fileName) after wipe failed: ${it.message}", it)
            }

        Log.e(
            TAG,
            "encrypted store for $fileName is unusable — serving an in-memory store. " +
                "Stored credentials are gone and nothing will persist this session; " +
                "the user must re-enter them.",
        )
        return InMemorySharedPreferences()
    }

    /**
     * Earlier builds degraded to a plain-text `<name>_plain_fallback.xml`.
     * Devices that hit that path still have secrets sitting in cleartext, and
     * nothing else ever deletes the file. Remove it whenever this factory runs.
     */
    private fun purgeLegacyPlaintextFallback(context: Context, fileName: String) {
        runCatching {
            val legacy = File(
                File(context.applicationInfo.dataDir, "shared_prefs"),
                "${fileName}_plain_fallback.xml",
            )
            if (legacy.exists() && legacy.delete()) {
                Log.w(TAG, "deleted legacy plain-text fallback for $fileName")
            }
        }.onFailure { Log.w(TAG, "purge legacy fallback failed: ${it.message}") }
    }

    private fun build(context: Context, fileName: String): SharedPreferences {
        val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            fileName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private fun wipeEncryptedState(context: Context, fileName: String) {
        // XML file the SP itself reads/writes.
        runCatching {
            val dir = File(context.applicationInfo.dataDir, "shared_prefs")
            File(dir, "$fileName.xml").delete()
            // Tink keyset blob is stashed in this companion prefs file.
            File(dir, "__androidx_security_crypto_encrypted_prefs__.xml").delete()
        }.onFailure { Log.w(TAG, "wipe prefs files failed: ${it.message}") }

        runCatching {
            val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            if (ks.containsAlias(MasterKey.DEFAULT_MASTER_KEY_ALIAS)) {
                ks.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            }
        }.onFailure { Log.w(TAG, "wipe master-key alias failed: ${it.message}") }
    }
}
