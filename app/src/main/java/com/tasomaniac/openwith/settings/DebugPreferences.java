package com.tasomaniac.openwith.settings;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ShareCompat;
import android.support.v7.preference.Preference;

import com.tasomaniac.openwith.R;

/**
 * Created by tasomaniac on 7/2/17.
 */

class DebugPreferences {

    static void setup(SettingsFragment fragment) {
        fragment.addPreferencesFromResource(R.xml.pref_debug);

        FragmentActivity activity = fragment.getActivity();
        setupDebugPreference(
                activity,
                findPreference(fragment, R.string.pref_key_debug_amazon),
                "http://www.amazon.com/Garmin-Speed-Cadence-Bike-Sensor/dp/B000BFNOT8"
        );
        setupDebugPreference(
                activity,
                findPreference(fragment, R.string.pref_key_debug_maps),
                "http://maps.google.com/maps"
        );
        setupDebugPreference(
                activity,
                findPreference(fragment, R.string.pref_key_debug_hangouts),
                "https://hangouts.google.com/hangouts/_/novoda.com/wormhole?authuser=tahsin@novoda.com"
        );
        setupDebugPreference(
                activity,
                findPreference(fragment, R.string.pref_key_debug_play),
                "https://play.google.com/store/apps/details?id=com.tasomaniac.openwith"
        );
        setupDebugPreference(
                activity,
                findPreference(fragment, R.string.pref_key_debug_redirect),
                "http://forward.immobilienscout24.de/9004STF/expose/78069302"
        );
        setupDebugPreference(
                activity,
                findPreference(fragment, R.string.pref_key_debug_non_http),
                "is24://retargetShowSearchForm"
        );
    }

    private static void setupDebugPreference(Activity activity, Preference debugPreference, String debugPrefUrl) {
        Intent intent = ShareCompat.IntentBuilder.from(activity)
                .setText(debugPrefUrl)
                .setType("text/plain")
                .createChooserIntent();

        debugPreference.setIntent(intent);
        debugPreference.setSummary(debugPrefUrl);
    }

    private static Preference findPreference(SettingsFragment fragment, @StringRes int keyResource) {
        return fragment.findPreference(fragment.getString(keyResource));
    }
}
