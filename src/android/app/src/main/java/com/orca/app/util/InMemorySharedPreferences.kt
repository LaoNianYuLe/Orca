package com.orca.app.util

import android.content.SharedPreferences
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Volatile [SharedPreferences] used as the last-resort fallback when the
 * encrypted store cannot be opened (see [EncryptedPrefsFactory]).
 *
 * The store this replaces wrote to a plain-text XML file, which meant a
 * Keystore fault silently downgraded API keys and OAuth refresh tokens to
 * cleartext on disk — where `adb backup` and any same-UID process could read
 * them. Nothing here ever touches disk: the process keeps a working read-write
 * store so callers never crash, and the values die with the process.
 *
 * Credentials are still lost in this path — that was already true before, since
 * the encrypted blob is unreadable by then. The difference is that re-entering
 * them does not leave a cleartext copy behind.
 */
class InMemorySharedPreferences : SharedPreferences {

    private val values = LinkedHashMap<String, Any?>()
    private val listeners = CopyOnWriteArraySet<SharedPreferences.OnSharedPreferenceChangeListener>()

    override fun getAll(): MutableMap<String, *> = synchronized(values) { LinkedHashMap(values) }

    override fun getString(key: String?, defValue: String?): String? =
        synchronized(values) { values[key] as? String ?: defValue }

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        synchronized(values) { (values[key] as? Set<String>)?.toMutableSet() ?: defValues }

    override fun getInt(key: String?, defValue: Int): Int =
        synchronized(values) { values[key] as? Int ?: defValue }

    override fun getLong(key: String?, defValue: Long): Long =
        synchronized(values) { values[key] as? Long ?: defValue }

    override fun getFloat(key: String?, defValue: Float): Float =
        synchronized(values) { values[key] as? Float ?: defValue }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean =
        synchronized(values) { values[key] as? Boolean ?: defValue }

    override fun contains(key: String?): Boolean = synchronized(values) { values.containsKey(key) }

    override fun edit(): SharedPreferences.Editor = EditorImpl()

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?,
    ) {
        listener?.let { listeners.add(it) }
    }

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?,
    ) {
        listener?.let { listeners.remove(it) }
    }

    private fun notifyChanged(keys: List<String?>) {
        if (listeners.isEmpty()) return
        for (key in keys) {
            for (l in listeners) l.onSharedPreferenceChanged(this, key)
        }
    }

    private inner class EditorImpl : SharedPreferences.Editor {
        private val pending = LinkedHashMap<String, Any?>()
        private var clearRequested = false

        private fun stage(key: String?, value: Any?): SharedPreferences.Editor = apply {
            if (key != null) synchronized(pending) { pending[key] = value }
        }

        override fun putString(key: String?, value: String?) = stage(key, value)
        override fun putStringSet(key: String?, values: MutableSet<String>?) =
            stage(key, values?.toSet())

        override fun putInt(key: String?, value: Int) = stage(key, value)
        override fun putLong(key: String?, value: Long) = stage(key, value)
        override fun putFloat(key: String?, value: Float) = stage(key, value)
        override fun putBoolean(key: String?, value: Boolean) = stage(key, value)
        override fun remove(key: String?) = stage(key, REMOVED)

        override fun clear(): SharedPreferences.Editor = apply { clearRequested = true }

        override fun commit(): Boolean {
            val changed = mutableListOf<String?>()
            synchronized(values) {
                if (clearRequested) {
                    changed += values.keys.toList()
                    values.clear()
                }
                synchronized(pending) {
                    for ((k, v) in pending) {
                        if (v === REMOVED) values.remove(k) else values[k] = v
                        changed += k
                    }
                    pending.clear()
                }
                clearRequested = false
            }
            notifyChanged(changed)
            return true
        }

        override fun apply() {
            commit()
        }
    }

    private companion object {
        val REMOVED = Any()
    }
}
