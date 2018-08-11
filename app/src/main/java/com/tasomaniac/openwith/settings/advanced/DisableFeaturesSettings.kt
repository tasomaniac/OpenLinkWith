package com.tasomaniac.openwith.settings.advanced

import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.settings.Settings
import com.tasomaniac.openwith.settings.SettingsFragment
import javax.inject.Inject

class DisableFeaturesSettings @Inject constructor(fragment: SettingsFragment) : Settings(fragment) {

  override fun setup() {
    addPreferencesFromResource(R.xml.pref_disable_features)
  }
}
