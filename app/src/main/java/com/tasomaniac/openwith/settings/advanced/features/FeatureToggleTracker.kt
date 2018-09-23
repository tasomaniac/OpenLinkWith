package com.tasomaniac.openwith.settings.advanced.features

import com.tasomaniac.openwith.data.Analytics
import javax.inject.Inject

class FeatureToggleTracker @Inject constructor(
    private val analytics: Analytics
) : FeatureToggleSideEffect {

    override fun featureToggled(feature: Feature, enabled: Boolean) {
        analytics.sendEvent("${feature.prefKey} toggled", "enabled", enabled.toString())
    }

}
