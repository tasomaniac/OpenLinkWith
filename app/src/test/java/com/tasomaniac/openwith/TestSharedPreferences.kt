package com.tasomaniac.openwith

import android.content.SharedPreferences
import java.util.concurrent.ConcurrentHashMap

class TestSharedPreferences : SharedPreferences {

    private val map = ConcurrentHashMap<String, Any>()

    override fun contains(key: String) = map.containsKey(key)

    override fun getAll() = HashMap(map)

    override fun getBoolean(key: String, defValue: Boolean) = map[key] as? Boolean ?: defValue

    override fun getInt(key: String, defValue: Int) = map[key] as? Int ?: defValue

    override fun getLong(key: String, defValue: Long) = map[key] as? Long ?: defValue

    override fun getFloat(key: String, defValue: Float) = map[key] as? Float ?: defValue

    override fun getString(key: String, defValue: String?) = map[key] as? String ?: defValue

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String, defValues: Set<String>?) = map[key] as? Set<String> ?: defValues

    override fun edit() = Editor()

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) = Unit

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) = Unit

    inner class Editor : SharedPreferences.Editor {
        override fun clear() = apply { map.clear() }

        override fun remove(key: String) = apply { map.remove(key) }

        override fun putLong(key: String, value: Long) = apply { map[key] = value }

        override fun putInt(key: String, value: Int) = apply { map[key] = value }

        override fun putBoolean(key: String, value: Boolean) = apply { map[key] = value }

        override fun putFloat(key: String, value: Float) = apply { map[key] = value }

        override fun putString(key: String, value: String?) = apply {
            if (value == null) {
                remove(key)
            } else {
                map[key] = value
            }
        }

        override fun putStringSet(key: String, values: MutableSet<String>?) = apply {
            if (values == null) {
                remove(key)
            } else {
                map[key] = values
            }
        }

        override fun commit() = true
        override fun apply() = Unit
    }

}
