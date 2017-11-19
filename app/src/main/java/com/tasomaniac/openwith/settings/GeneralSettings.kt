package com.tasomaniac.openwith.settings

import android.content.Intent
import android.support.v7.preference.Preference
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.intro.IntroActivity
import com.tasomaniac.openwith.preferred.PreferredAppsActivity
import javax.inject.Inject

class GeneralSettings @Inject constructor(
    fragment: SettingsFragment,
    private val analytics: Analytics
) : Settings(fragment) {

  override fun setup() {
    addPreferencesFromResource(R.xml.pref_general)

    findPreference(R.string.pref_key_about).onPreferenceClickListener = onPreferenceClickListener
    findPreference(R.string.pref_key_preferred).onPreferenceClickListener = onPreferenceClickListener
  }

  private val onPreferenceClickListener = Preference.OnPreferenceClickListener {
    when {
      it.isKeyEquals(R.string.pref_key_about) -> {
        startActivity(Intent(activity, IntroActivity::class.java))
      }
      it.isKeyEquals(R.string.pref_key_preferred) -> {
        startActivity(Intent(activity, PreferredAppsActivity::class.java))
      }
    }

    analytics.sendEvent("Preference", "Item Click", it.key)
    true
  }
}
