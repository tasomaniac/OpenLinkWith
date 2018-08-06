package com.tasomaniac.openwith.settings.advanced

import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.settings.Settings
import javax.inject.Inject

class AdvancedFeaturesSettings @Inject constructor(fragment: AdvancedFeaturesFragment) : Settings(fragment) {
    override fun setup() {
        addPreferencesFromResource(R.xml.pref_features)
    }
}
