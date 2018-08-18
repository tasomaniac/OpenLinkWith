package com.tasomaniac.openwith.settings.advanced.features

import android.content.ComponentName
import android.content.pm.PackageManager
import com.tasomaniac.openwith.App
import javax.inject.Inject

class FeatureToggler @Inject constructor(
    private val app: App,
    private val packageManager: PackageManager
) {

    fun toggleFeature(feature: Feature, enabled: Boolean) {
        packageManager.setComponentEnabledSetting(
            ComponentName(app, feature.clazz),
            enabled.toState(),
            PackageManager.DONT_KILL_APP
        )
    }

    private fun Boolean.toState(): Int {
        return if (this)
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        else
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    }
}
