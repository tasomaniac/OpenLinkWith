package com.tasomaniac.openwith.settings;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import com.tasomaniac.openwith.BuildConfig;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.data.Analytics;
import com.tasomaniac.openwith.data.prefs.BooleanPreference;
import com.tasomaniac.openwith.data.prefs.UsageAccess;
import com.tasomaniac.openwith.intro.IntroActivity;
import com.tasomaniac.openwith.preferred.PreferredAppsActivity;
import com.tasomaniac.openwith.util.Intents;
import com.tasomaniac.openwith.util.Utils;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

    @Inject Analytics analytics;
    @Inject SharedPreferences sharedPreferences;
    @Inject
    @UsageAccess
    BooleanPreference usageAccessPref;
    @Inject NightModePreferences nightModePreferences;

    private PreferenceCategory usageStatsPreferenceCategory;

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
            setupUsagePreference();
        }
        setupVersionPreference();
        setupNightModePreference();
    }

    @Override
    public void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupUsagePreference() {
        boolean usageAccessGiven = Utils.isUsageStatsEnabled(getActivity());

        if (usageAccessGiven) {
            if (usageStatsPreferenceCategory != null) {
                getPreferenceScreen().removePreference(usageStatsPreferenceCategory);
                usageStatsPreferenceCategory = null;
            }
        } else {
            if (usageStatsPreferenceCategory == null) {
                addPreferencesFromResource(R.xml.pref_usage);
                usageStatsPreferenceCategory = (PreferenceCategory) findPreference(R.string.pref_key_category_usage);

                Preference usageStatsPreference = findPreference(R.string.pref_key_usage_stats);
                usageStatsPreference.setOnPreferenceClickListener(this);

                //Set title and summary in red font.
                usageStatsPreference.setTitle(coloredErrorString(R.string.pref_title_usage_stats));
                usageStatsPreference.setSummary(coloredErrorString(R.string.pref_summary_usage_stats));
                usageStatsPreference.setWidgetLayoutResource(R.layout.preference_widget_error);
            }
        }

        if (usageAccessPref.get() != usageAccessGiven) {
            usageAccessPref.set(usageAccessGiven);

            analytics.sendEvent(
                    "Usage Access",
                    "Access Given",
                    Boolean.toString(usageAccessGiven)
            );
        }
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
        } else if (isKeyEquals(preference, R.string.pref_key_usage_stats)) {
            onUsageAccessClick(preference);
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

    private void onUsageAccessClick(Preference preference) {
        boolean settingsOpened = Intents.maybeStartUsageAccessSettings(getActivity());

        if (!settingsOpened) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.error)
                    .setMessage(R.string.error_usage_access_not_found)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();

            preference.setSummary(R.string.error_usage_access_not_found);
        }
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

    private Preference findPreference(@StringRes int keyResource) {
        return findPreference(getString(keyResource));
    }

    private CharSequence coloredErrorString(@StringRes int stringRes) {
        SpannableString errorSpan = new SpannableString(getString(stringRes));
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(
                ContextCompat.getColor(getContext(), R.color.error_color));
        errorSpan.setSpan(colorSpan, 0, errorSpan.length(), 0);
        return errorSpan;
    }
}
