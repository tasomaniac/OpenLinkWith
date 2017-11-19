package com.tasomaniac.openwith.settings

import android.support.v4.app.ShareCompat
import android.support.v7.preference.Preference
import com.tasomaniac.openwith.BuildConfig
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import javax.inject.Inject

class OtherSettings @Inject constructor(
    private val fragment: SettingsFragment,
    private val analytics: Analytics
) {

  fun setup() {
    fragment.run {
      addPreferencesFromResource(R.xml.pref_others)

      findPreference(R.string.pref_key_open_source).onPreferenceClickListener = onPreferenceClickListener
      findPreference(R.string.pref_key_contact).onPreferenceClickListener = onPreferenceClickListener
    }
    setupVersionPreference()
  }

  private val onPreferenceClickListener = Preference.OnPreferenceClickListener {
    fragment.run {
      if (isKeyEquals(it, R.string.pref_key_open_source)) {
        displayLicensesDialogFragment()
      } else if (isKeyEquals(it, R.string.pref_key_contact)) {
        startContactEmailChooser()
      }
    }

    analytics.sendEvent("Preference", "Item Click", it.key)
    true
  }

  private fun setupVersionPreference() {
    val version = StringBuilder(BuildConfig.VERSION_NAME)
    if (BuildConfig.DEBUG) {
      version.append(" (")
          .append(BuildConfig.VERSION_CODE)
          .append(")")
    }
    val preference = fragment.findPreference(R.string.pref_key_version)
    preference.summary = version
  }

  private fun displayLicensesDialogFragment() {
    LicensesDialogFragment.newInstance().show(fragment.fragmentManager, "LicensesDialog")
  }

  private fun startContactEmailChooser() {
    ShareCompat.IntentBuilder.from(fragment.activity!!)
        .addEmailTo("Said Tahsin Dane <tasomaniac+openlinkwith@gmail.com>")
        .setSubject(fragment.getString(R.string.app_name))
        .setType("message/rfc822")
        .startChooser()
  }

}
