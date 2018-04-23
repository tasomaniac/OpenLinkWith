package com.tasomaniac.openwith.settings

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.preference.Preference
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.browser.BrowserPreferences
import com.tasomaniac.openwith.browser.PreferredBrowserActivity
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.intro.IntroActivity
import com.tasomaniac.openwith.preferred.PreferredAppsActivity
import javax.inject.Inject

class GeneralSettings @Inject constructor(
    fragment: SettingsFragment,
    private val sharedPreferences: SharedPreferences,
    private val browserPreferences: BrowserPreferences,
    private val analytics: Analytics
) : Settings(fragment),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun setup() {
        addPreferencesFromResource(R.xml.pref_general)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        findPreference(R.string.pref_key_about).onPreferenceClickListener = onPreferenceClickListener
        findPreference(R.string.pref_key_preferred).onPreferenceClickListener = onPreferenceClickListener

        val browser = findPreference(R.string.pref_key_browser)
        browser.onPreferenceClickListener = onPreferenceClickListener
        browser.summary = browserPreferences.mode.toBrowserSummary()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key.startsWith("pref_browser")) {
            val browser = findPreference(R.string.pref_key_browser)
            browser.summary = browserPreferences.mode.toBrowserSummary()
        }
    }

    private fun BrowserPreferences.Mode.toBrowserSummary() = when (this) {
        BrowserPreferences.Mode.None -> context.getString(R.string.browser_none_description)
        BrowserPreferences.Mode.AlwaysAsk -> context.getString(R.string.browser_always_ask_description)
        is BrowserPreferences.Mode.Browser -> context.getString(R.string.pref_summary_selected_browser, displayLabel)
    }

    private val onPreferenceClickListener = Preference.OnPreferenceClickListener {
        when {
            it.isKeyEquals(R.string.pref_key_about) -> startActivity<IntroActivity>()
            it.isKeyEquals(R.string.pref_key_preferred) -> startActivity<PreferredAppsActivity>()
            it.isKeyEquals(R.string.pref_key_browser) -> startActivity<PreferredBrowserActivity>()
        }

        analytics.sendEvent("Preference", "Item Click", it.key)
        true
    }

    private inline fun <reified T : Activity> startActivity() {
        startActivity(Intent(this.activity, T::class.java))
    }
}
