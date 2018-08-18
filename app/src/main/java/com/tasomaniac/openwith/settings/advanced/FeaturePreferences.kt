package com.tasomaniac.openwith.settings.advanced

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject

class FeaturePreferences @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    fun isEnabled(feature: Feature) = sharedPreferences.getBoolean(feature.prefKey, true)

    fun setEnabled(feature: Feature, enabled: Boolean) = sharedPreferences.edit {
        putBoolean(feature.prefKey, enabled)
    }

}
