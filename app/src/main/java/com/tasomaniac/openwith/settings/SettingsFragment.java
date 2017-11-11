package com.tasomaniac.openwith.settings;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.ShareCompat;
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
    @Inject NightModePreferences nightModePreferences;
    @Inject UsageAccessSettings usageAccessSettings;

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
        addPreferencesFromResource(R.xml.pref_display);
        addPreferencesFromResource(R.xml.pref_others);

        findPreference(R.string.pref_key_about).setOnPreferenceClickListener(this);
        findPreference(R.string.pref_key_preferred).setOnPreferenceClickListener(this);
        findPreference(R.string.pref_key_open_source).setOnPreferenceClickListener(this);
        findPreference(R.string.pref_key_contact).setOnPreferenceClickListener(this);

        if (BuildConfig.DEBUG) {
            new DebugPreferences(this).setup();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        if (SDK_INT >= LOLLIPOP) {
            usageAccessSettings.setup();
        }
        setupVersionPreference();
        setupNightModePreference();
    }

    @Override
    public void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setupVersionPreference() {
        StringBuilder version = new StringBuilder(BuildConfig.VERSION_NAME);
        if (BuildConfig.DEBUG) {
            version.append(" (")
                    .append(BuildConfig.VERSION_CODE)
                    .append(")");
        }
        Preference preference = findPreference(R.string.pref_key_version);
        preference.setSummary(version);
    }

    private void setupNightModePreference() {
        int selectedEntry = nightModePreferences.getSelectedEntry();
        findPreference(R.string.pref_key_night_mode).setSummary(selectedEntry);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        if (isKeyEquals(preference, R.string.pref_key_about)) {
            startActivity(new Intent(getActivity(), IntroActivity.class));
        } else if (isKeyEquals(preference, R.string.pref_key_preferred)) {
            startActivity(new Intent(getActivity(), PreferredAppsActivity.class));
        } else if (isKeyEquals(preference, R.string.pref_key_open_source)) {
            displayLicensesDialogFragment();
        } else if (isKeyEquals(preference, R.string.pref_key_contact)) {
            startContactEmailChooser();
        }

        analytics.sendEvent( "Preference", "Item Click", preference.getKey());
        return true;
    }

    private boolean isKeyEquals(Preference preference, @StringRes int keyRes) {
        return isKeyEquals(preference.getKey(), keyRes);
    }

    private boolean isKeyEquals(String key, @StringRes int keyRes) {
        return getString(keyRes).equals(key);
    }

    private void displayLicensesDialogFragment() {
        LicensesDialogFragment.newInstance().show(getFragmentManager(), "LicensesDialog");
    }

    private void startContactEmailChooser() {
        ShareCompat.IntentBuilder.from(getActivity())
                .addEmailTo("Said Tahsin Dane <tasomaniac+openlinkwith@gmail.com>")
                .setSubject(getString(R.string.app_name))
                .setType("message/rfc822")
                .startChooser();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        new BackupManager(getActivity()).dataChanged();

        if (isKeyEquals(key, R.string.pref_key_night_mode)) {
            nightModePreferences.updateDefaultNightMode();
            getActivity().recreate();
        }
    }

    Preference findPreference(@StringRes int keyResource) {
        return findPreference(getString(keyResource));
    }
}
