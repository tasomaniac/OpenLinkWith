package com.tasomaniac.openwith.settings.other

import androidx.core.app.ShareCompat
import androidx.preference.Preference
import com.tasomaniac.openwith.BuildConfig
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.settings.Settings
import com.tasomaniac.openwith.settings.SettingsFragment
import javax.inject.Inject

class OtherSettings @Inject constructor(
    fragment: SettingsFragment,
    private val analytics: Analytics
) : Settings(fragment) {

    override fun setup() {
        addPreferencesFromResource(R.xml.pref_others)

        findPreference(R.string.pref_key_open_source).setOnPreferenceClickListener {
            displayLicensesDialogFragment()
            trackItemClick(it)
            true
        }
        findPreference(R.string.pref_key_contact).setOnPreferenceClickListener {
            startContactEmailChooser()
            trackItemClick(it)
            true
        }
        setupVersionPreference()
    }

    private fun trackItemClick(preference: Preference) {
        analytics.sendEvent("Preference", "Item Click", preference.key)
    }

    private fun setupVersionPreference() {
        findPreference(R.string.pref_key_version).apply {
            summary = if (BuildConfig.DEBUG) {
                "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            } else {
                BuildConfig.VERSION_NAME
            }
        }
    }

    private fun displayLicensesDialogFragment() {
        LicensesDialogFragment.newInstance()
            .show(activity.supportFragmentManager, "LicensesDialog")
    }

    private fun startContactEmailChooser() {
        ShareCompat.IntentBuilder.from(activity)
            .addEmailTo("Said Tahsin Dane <tasomaniac+openlinkwith@gmail.com>")
            .setSubject(context.getString(R.string.app_name))
            .setType("message/rfc822")
            .startChooser()
    }

}
