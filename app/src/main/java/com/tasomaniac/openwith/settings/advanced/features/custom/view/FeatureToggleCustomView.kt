package com.tasomaniac.openwith.settings.advanced.features.custom.view

import android.widget.FrameLayout
import com.tasomaniac.openwith.settings.advanced.features.Feature
import dagger.MapKey

@MapKey
annotation class FeatureToggleCustomViewKey(val value: Feature)

interface FeatureToggleCustomView {
    fun bindCustomContent(customContent: FrameLayout)
}
