package com.tasomaniac.openwith.settings.advanced.features

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
interface FeatureToggleModule {

    @Binds
    @IntoSet
    fun featureToggleTracker(featureToggleTracker: FeatureToggleTracker): FeatureToggleSideEffect

    @Binds
    @IntoSet
    fun setDefaultBrowser(setDefaultBrowser: SetDefaultBrowser): FeatureToggleSideEffect
}
