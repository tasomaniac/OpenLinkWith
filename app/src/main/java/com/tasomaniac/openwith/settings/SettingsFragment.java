package com.tasomaniac.openwith.settings;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.tasomaniac.openwith.BuildConfig;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.data.Analytics;
import com.tasomaniac.openwith.intro.IntroActivity;
import com.tasomaniac.openwith.preferred.PreferredAppsActivity;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

    @Inject Analytics analytics;
    @Inject SharedPreferences sharedPreferences;
    @Inject ClipboardSettings clipboardSettings;
    @Inject UsageAccessSettings usageAccessSettings;
    @Inject DisplaySettings displaySettings;
    @Inject OtherSettings otherSettings;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_general);

        findPreference(R.string.pref_key_about).setOnPreferenceClickListener(this);
        findPreference(R.string.pref_key_preferred).setOnPreferenceClickListener(this);

        if (BuildConfig.DEBUG) {
            new DebugPreferences(this).setup();
        }
        clipboardSettings.setup();
        displaySettings.setup();
        otherSettings.setup();
        if (SDK_INT >= LOLLIPOP) {
            usageAccessSettings.setup();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        if (SDK_INT >= LOLLIPOP) {
            usageAccessSettings.update();
        }
    }

    @Override
    public void onPause() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (SDK_INT >= LOLLIPOP) {
            usageAccessSettings.release();
        }
        clipboardSettings.release();
        displaySettings.release();
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (isKeyEquals(preference, R.string.pref_key_about)) {
            startActivity(new Intent(getActivity(), IntroActivity.class));
        } else if (isKeyEquals(preference, R.string.pref_key_preferred)) {
            startActivity(new Intent(getActivity(), PreferredAppsActivity.class));
        }

        analytics.sendEvent("Preference", "Item Click", preference.getKey());
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        new BackupManager(getActivity()).dataChanged();
    }

    Preference findPreference(@StringRes int keyResource) {
        return findPreference(getString(keyResource));
    }

    boolean isKeyEquals(Preference preference, @StringRes int keyRes) {
        return isKeyEquals(preference.getKey(), keyRes);
    }

    boolean isKeyEquals(String key, @StringRes int keyRes) {
        return getString(keyRes).equals(key);
    }
}
