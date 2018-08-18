package com.tasomaniac.openwith.settings.advanced

import android.content.Intent
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.intro.IntroActivity
import com.tasomaniac.openwith.settings.Settings
import com.tasomaniac.openwith.settings.SettingsFragment
import javax.inject.Inject

class AdvancedSettings @Inject constructor(
    private val analytics: Analytics,
    fragment: SettingsFragment
) : Settings(fragment) {

    override fun setup() {
        addPreferencesFromResource(R.xml.pref_advanced_category)

        findPreference(R.string.pref_key_about).setOnPreferenceClickListener {
            activity.startActivity(Intent(context, IntroActivity::class.java))
            analytics.sendEvent("Preference", "Item Click", it.key)
            true
        }
    }
}
