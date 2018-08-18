package com.tasomaniac.openwith.settings.advanced.features

import android.os.Build.VERSION_CODES.M
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.settings.Settings
import javax.inject.Inject

@RequiresApi(M)
class FeaturesListSettings @Inject constructor(
    private val featurePreferences: FeaturePreferences,
    private val fragment: FeaturesListFragment
) : Settings(fragment) {

    override fun setup() {
        addPreferencesFromResource(R.xml.pref_features)
    }

    override fun resume() {
        Feature.values().forEach { feature ->
            val enabled = featurePreferences.isEnabled(feature)
            fragment.findPreference(feature.prefKey)?.setSummary(enabled.toSummary())
        }
    }

    @StringRes
    private fun Boolean.toSummary() =
        if (this) R.string.pref_state_feature_enabled else R.string.pref_state_feature_disabled
}
