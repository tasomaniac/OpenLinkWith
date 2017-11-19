package com.tasomaniac.openwith.settings

import android.support.v4.app.ShareCompat
import android.support.v7.preference.Preference
import com.tasomaniac.openwith.R
import javax.inject.Inject

class DebugSettings @Inject constructor(
    fragment: SettingsFragment
) : Settings(fragment) {

  override fun setup() {
    addPreferencesFromResource(R.xml.pref_debug)

    findPreference(R.string.pref_key_debug_amazon).setupDebugPreference(
        "http://www.amazon.com/Garmin-Speed-Cadence-Bike-Sensor/dp/B000BFNOT8"
    )
    findPreference(R.string.pref_key_debug_maps).setupDebugPreference(
        "http://maps.google.com/maps"
    )
    findPreference(R.string.pref_key_debug_instagram).setupDebugPreference(
        "https://www.instagram.com/tasomaniac/"
    )
    findPreference(R.string.pref_key_debug_hangouts).setupDebugPreference(
        "https://hangouts.google.com/hangouts/_/novoda.com/wormhole?authuser=tahsin@novoda.com"
    )
    findPreference(R.string.pref_key_debug_play).setupDebugPreference(
        "https://play.google.com/store/apps/details?id=com.tasomaniac.openwith"
    )
    findPreference(R.string.pref_key_debug_redirect).setupDebugPreference(
        "http://forward.immobilienscout24.de/9004STF/expose/78069302"
    )
    findPreference(R.string.pref_key_debug_non_http).setupDebugPreference(
        "is24://retargetShowSearchForm"
    )
  }

  private fun Preference.setupDebugPreference(debugPrefUrl: String) {
    intent = ShareCompat.IntentBuilder.from(activity)
        .setText(debugPrefUrl)
        .setType("text/plain")
        .createChooserIntent()
    summary = debugPrefUrl
  }
}
