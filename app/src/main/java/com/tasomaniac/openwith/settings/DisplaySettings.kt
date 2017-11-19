package com.tasomaniac.openwith.settings

import android.content.SharedPreferences
import com.tasomaniac.openwith.R
import javax.inject.Inject

class DisplaySettings @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val fragment: SettingsFragment,
    private val nightModePreferences: NightModePreferences
) : SharedPreferences.OnSharedPreferenceChangeListener {

  fun setup() {
    fragment.addPreferencesFromResource(R.xml.pref_display)
    sharedPreferences.registerOnSharedPreferenceChangeListener(this)

    val selectedEntry = nightModePreferences.selectedEntry
    fragment.findPreference(R.string.pref_key_night_mode).setSummary(selectedEntry)
  }

  fun release() {
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
  }

  override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
    fragment.run {
      if (isKeyEquals(key, R.string.pref_key_night_mode)) {
        nightModePreferences.updateDefaultNightMode()
        activity!!.recreate()

        val selectedValue = nightModePreferences.mode.stringVale(resources)
        analytics.sendEvent("Preference", "Night Mode", selectedValue)
      }
    }
  }
}
