package com.tasomaniac.openwith.settings

import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import com.tasomaniac.openwith.R
import javax.inject.Inject

class NightModePreferences @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val resources: Resources
) {

    private val key: String = resources.getString(R.string.pref_key_night_mode)

    @get:StringRes
    val selectedEntry: Int
        get() = mode.entry

    val mode: Mode
        get() {
            val value = sharedPreferences.getString(key, null)
            return Mode.fromValue(resources, value) ?: defaultNightMode()
        }

    private fun defaultNightMode() = when {
        SDK_INT >= P -> Mode.SYSTEM
        else -> Mode.OFF
    }

    fun updateDefaultNightMode() {
        AppCompatDelegate.setDefaultNightMode(mode.delegate)
    }

    enum class Mode(
        @StringRes private val value: Int,
        @StringRes val entry: Int,
        val delegate: Int
    ) {
        OFF(R.string.pref_value_night_mode_off, R.string.pref_entry_night_mode_off, AppCompatDelegate.MODE_NIGHT_NO),
        ON(R.string.pref_value_night_mode_on, R.string.pref_entry_night_mode_on, AppCompatDelegate.MODE_NIGHT_YES),
        BATTERY(
            R.string.pref_value_night_mode_battery,
            R.string.pref_entry_night_mode_battery,
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        ),
        SYSTEM(
            R.string.pref_value_night_mode_system,
            R.string.pref_entry_night_mode_system,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        );

        fun stringVale(resources: Resources): String = resources.getString(value)

        companion object {

            internal fun fromValue(resources: Resources, value: String?): Mode? =
                Mode.values().find { it.stringVale(resources) == value }
        }
    }
}
