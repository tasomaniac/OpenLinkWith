package com.tasomaniac.openwith.settings.advanced.features

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.os.Build.VERSION_CODES.M
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.text.parseAsHtml
import androidx.preference.Preference
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.toggle_feature_activity.featureDetails
import kotlinx.android.synthetic.main.toggle_feature_activity.featureImage
import kotlinx.android.synthetic.main.toggle_feature_activity.featureToggle
import kotlinx.android.synthetic.main.toggle_feature_activity.toolbar
import javax.inject.Inject

@TargetApi(M)
class ToggleFeatureActivity : DaggerAppCompatActivity() {

    @Inject lateinit var featurePreferences: FeaturePreferences
    @Inject lateinit var featureToggler: FeatureToggler
    @Inject lateinit var analytics: Analytics
    @Inject lateinit var sideEffects: Set<@JvmSuppressWildcards FeatureToggleSideEffect>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.toggle_feature_activity)

        val feature = intent.featureKey.toFeature()
        setupInitialState(feature)
        setupToggle(feature)
        setupTitle(feature)
        setupDetails(feature)

        if (savedInstanceState == null) {
            analytics.sendEvent("FeatureToggle", "Feature", feature.prefKey)
        }
    }

    private fun setupInitialState(feature: Feature) {
        val enabled = featurePreferences.isEnabled(feature)
        featureToggle.isChecked = enabled
        featureToggle.setText(enabled.toSummary())
    }

    private fun setupToggle(feature: Feature) {
        featureToggle.setOnCheckedChangeListener { _, enabled ->
            featurePreferences.setEnabled(feature, enabled)
            featureToggle.setText(enabled.toSummary())
            featureToggler.toggleFeature(feature, enabled)

            sideEffects.forEach { it.featureToggled(feature, enabled) }
        }
    }

    private fun setupTitle(feature: Feature) {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setTitle(feature.titleRes)
    }

    private fun setupDetails(feature: Feature) {
        featureDetails.text = getString(feature.detailsRes).parseAsHtml()
        if (feature.imageRes != null) {
            featureImage.setImageResource(feature.imageRes)
        } else {
            featureImage.visibility = View.GONE
        }
    }

    @StringRes
    private fun Boolean.toSummary() =
        if (this) R.string.pref_state_feature_enabled else R.string.pref_state_feature_disabled

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {

        private const val FEATURE = "FEATURE"

        private var Intent.featureKey: String
            get() = getStringExtra(FEATURE)
            set(value) {
                putExtra(FEATURE, value)
            }

        fun startWith(activity: Activity, preference: Preference) {
            val intent = Intent(activity, ToggleFeatureActivity::class.java).apply {
                this.featureKey = preference.key
            }
            activity.startActivity(intent)
        }
    }
}
