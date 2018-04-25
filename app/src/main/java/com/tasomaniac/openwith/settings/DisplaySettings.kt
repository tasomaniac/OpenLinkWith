package com.tasomaniac.openwith.settings

import android.content.SharedPreferences
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import javax.inject.Inject

class DisplaySettings @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val nightModePreferences: NightModePreferences,
    private val analytics: Analytics,
    fragment: SettingsFragment
) : Settings(fragment),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun setup() {
        addPreferencesFromResource(R.xml.pref_display)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val selectedEntry = nightModePreferences.selectedEntry
        findPreference(R.string.pref_key_night_mode).setSummary(selectedEntry)
    }

    override fun release() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key.isKeyEquals(R.string.pref_key_night_mode)) {
            nightModePreferences.updateDefaultNightMode()
            activity.recreate()

            val selectedValue = nightModePreferences.mode.stringVale(context.resources)
            analytics.sendEvent("Preference", "Night Mode", selectedValue)
        }
    }
}
