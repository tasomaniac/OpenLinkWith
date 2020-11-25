package com.tasomaniac.openwith.settings.advanced.features

import com.tasomaniac.openwith.settings.advanced.features.custom.view.CleanUrlsRegexView
import com.tasomaniac.openwith.settings.advanced.features.custom.view.FeatureToggleCustomView
import com.tasomaniac.openwith.settings.advanced.features.custom.view.FeatureToggleCustomViewKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet

@Module
interface FeatureToggleModule {

    @Binds
    @IntoSet
    fun featureToggleTracker(featureToggleTracker: FeatureToggleTracker): FeatureToggleSideEffect

    @Binds
    @IntoSet
    fun setDefaultBrowser(setDefaultBrowser: SetDefaultBrowser): FeatureToggleSideEffect

    @Binds
    @IntoMap
    @FeatureToggleCustomViewKey(Feature.CLEAN_URLS)
    fun cleanUrlsCustomView(cleanUrlsRegexView: CleanUrlsRegexView): FeatureToggleCustomView
}
