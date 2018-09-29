package com.tasomaniac.openwith.settings.advanced.features

interface FeatureToggleSideEffect {

    fun featureToggled(feature: Feature, enabled: Boolean)
}
