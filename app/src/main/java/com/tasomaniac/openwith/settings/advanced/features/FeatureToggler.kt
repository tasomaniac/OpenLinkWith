package com.tasomaniac.openwith.settings.advanced.features

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build.VERSION_CODES.M
import androidx.annotation.RequiresApi
import javax.inject.Inject

@RequiresApi(M)
class FeatureToggler @Inject constructor(
    private val app: Application,
    private val packageManager: PackageManager
) {

    fun toggleFeature(feature: Feature, enabled: Boolean) {
        if (feature.className == null) return

        packageManager.setComponentEnabledSetting(
            ComponentName(app, feature.className),
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
