package com.tasomaniac.openwith.settings.advanced

import android.os.Bundle
import com.tasomaniac.openwith.R
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.toggle_feature_activity.featureToggleText

class ToggleFeatureActivity : DaggerAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.toggle_feature_activity)
        
        featureToggleText.setText(R.string.pref_state_feature_disabled)
    }

}
