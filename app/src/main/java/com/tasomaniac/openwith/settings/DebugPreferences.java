package com.tasomaniac.openwith.settings;

import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.v4.app.ShareCompat;
import android.support.v7.preference.Preference;

import com.tasomaniac.openwith.R;

class DebugPreferences {

    private final SettingsFragment fragment;

    DebugPreferences(SettingsFragment fragment) {
        this.fragment = fragment;
    }

    void setup() {
        fragment.addPreferencesFromResource(R.xml.pref_debug);

        setupDebugPreference(
                findPreference(R.string.pref_key_debug_amazon),
                "http://www.amazon.com/Garmin-Speed-Cadence-Bike-Sensor/dp/B000BFNOT8"
        );
        setupDebugPreference(
                findPreference(R.string.pref_key_debug_maps),
                "http://maps.google.com/maps"
        );
        setupDebugPreference(
                findPreference(R.string.pref_key_debug_instagram),
                "https://www.instagram.com/tasomaniac/"
        );
        setupDebugPreference(
                findPreference(R.string.pref_key_debug_hangouts),
                "https://hangouts.google.com/hangouts/_/novoda.com/wormhole?authuser=tahsin@novoda.com"
        );
        setupDebugPreference(
                findPreference(R.string.pref_key_debug_play),
                "https://play.google.com/store/apps/details?id=com.tasomaniac.openwith"
        );
        setupDebugPreference(
                findPreference(R.string.pref_key_debug_redirect),
                "http://forward.immobilienscout24.de/9004STF/expose/78069302"
        );
        setupDebugPreference(
                findPreference(R.string.pref_key_debug_non_http),
                "is24://retargetShowSearchForm"
        );
    }

    private void setupDebugPreference(Preference debugPreference, String debugPrefUrl) {
        Intent intent = ShareCompat.IntentBuilder.from(fragment.getActivity())
                .setText(debugPrefUrl)
                .setType("text/plain")
                .createChooserIntent();

        debugPreference.setIntent(intent);
        debugPreference.setSummary(debugPrefUrl);
    }

    private Preference findPreference(@StringRes int keyResource) {
        return fragment.findPreference(fragment.getString(keyResource));
    }
}
