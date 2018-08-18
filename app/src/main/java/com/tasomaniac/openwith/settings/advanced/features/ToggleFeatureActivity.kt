package com.tasomaniac.openwith.settings.advanced.features

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.core.text.parseAsHtml
import androidx.preference.Preference
import com.tasomaniac.openwith.R
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.toggle_feature_activity.featureDetails
import kotlinx.android.synthetic.main.toggle_feature_activity.featureImage
import kotlinx.android.synthetic.main.toggle_feature_activity.featureToggle
import kotlinx.android.synthetic.main.toggle_feature_activity.toolbar
import javax.inject.Inject

class ToggleFeatureActivity : DaggerAppCompatActivity() {

    @Inject lateinit var featurePreferences: FeaturePreferences
    @Inject lateinit var featureToggler: FeatureToggler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.toggle_feature_activity)

        val feature = intent.featureKey.toFeature()

        featureToggle.setOnCheckedChangeListener { _, enabled ->
            featurePreferences.setEnabled(feature, enabled)
            featureToggle.setText(enabled.toSummary())
            featureToggler.toggleFeature(feature, enabled)
        }

        val enabled = featurePreferences.isEnabled(feature)
        featureToggle.isChecked = enabled
        featureToggle.setText(enabled.toSummary())

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setTitle(feature.titleRes)

        featureDetails.text = getString(feature.detailsRes).parseAsHtml()
        featureImage.setImageResource(feature.imageRes)
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
