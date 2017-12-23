package com.tasomaniac.openwith.settings

import com.tasomaniac.openwith.R
import javax.inject.Inject

class AdvancedSettings @Inject constructor(fragment: SettingsFragment) : Settings(fragment) {

  override fun setup() {
    addPreferencesFromResource(R.xml.pref_advanced)
  }
}
