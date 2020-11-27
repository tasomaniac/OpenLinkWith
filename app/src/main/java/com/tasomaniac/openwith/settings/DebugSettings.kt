package com.tasomaniac.openwith.settings

import androidx.core.app.ShareCompat
import androidx.preference.Preference
import com.tasomaniac.openwith.R
import javax.inject.Inject

class DebugSettings @Inject constructor(
    fragment: SettingsFragment
) : Settings(fragment) {

    override fun setup() {
        addPreferencesFromResource(R.xml.pref_debug)
        DEBUG_URLS.forEach { (key, url) ->
            findPreference(key).setupDebugPreference(url)
        }
    }

    private fun Preference.setupDebugPreference(debugPrefUrl: String) {
        intent = ShareCompat.IntentBuilder.from(activity)
            .setText(debugPrefUrl)
            .setType("text/plain")
            .createChooserIntent()
        summary = debugPrefUrl
    }

    companion object {
        private val DEBUG_URLS = mapOf(
            R.string.pref_key_debug_amazon to "http://www.amazon.com/Garmin-Speed-Cadence-Bike-Sensor/dp/B000BFNOT8",
            R.string.pref_key_debug_maps to "http://maps.google.com/maps",
            R.string.pref_key_debug_instagram to "https://www.instagram.com/tasomaniac/",
            R.string.pref_key_debug_hangouts to
                "https://hangouts.google.com/hangouts/_/novoda.com/wormhole?authuser=tahsin@novoda.com",
            R.string.pref_key_debug_play to
                "https://play.google.com/store/apps/details?id=com.tasomaniac.openwith&utm_source=facebook",
            R.string.pref_key_debug_redirect to "http://forward.immobilienscout24.de/9004STF/expose/78069302",
            R.string.pref_key_debug_non_http to "is24://retargetShowSearchForm",
            R.string.pref_key_debug_missing_http to "www.google.com"
        )
    }
}
