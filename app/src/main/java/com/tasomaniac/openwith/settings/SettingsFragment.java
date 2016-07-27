package com.tasomaniac.openwith.settings;

import android.annotation.TargetApi;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import com.tasomaniac.openwith.Analytics;
import com.tasomaniac.openwith.App;
import com.tasomaniac.openwith.BuildConfig;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.data.prefs.BooleanPreference;
import com.tasomaniac.openwith.intro.IntroActivity;
import com.tasomaniac.openwith.preferred.PreferredAppsActivity;
import com.tasomaniac.openwith.util.Intents;
import com.tasomaniac.openwith.util.Utils;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private PreferenceCategory usageStatsPreferenceCategory;
    private Analytics analytics;
    private BooleanPreference usageAccessPref;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analytics = App.getApp(getActivity()).getAnalytics();
        usageAccessPref = new BooleanPreference(
                PreferenceManager.getDefaultSharedPreferences(getActivity()),
                "usage_access");
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_general);
        addPreferencesFromResource(R.xml.pref_others);

        findPreference(R.string.pref_key_about).setOnPreferenceClickListener(this);
        findPreference(R.string.pref_key_preferred).setOnPreferenceClickListener(this);
        findPreference(R.string.pref_key_open_source).setOnPreferenceClickListener(this);
        findPreference(R.string.pref_key_contact).setOnPreferenceClickListener(this);

        if (BuildConfig.DEBUG) {
            addPreferencesFromResource(R.xml.pref_debug);

            setupDebugPreference(R.string.pref_key_debug_amazon,
                                 "http://www.amazon.com/Garmin-Speed-Cadence-Bike-Sensor/dp/B000BFNOT8");
        }
    }

    private void setupDebugPreference(int debugPrefKey, String debugPrefUrl) {
        Intent intent = ShareCompat.IntentBuilder.from(getActivity())
                .setText(debugPrefUrl)
                .setType("text/plain")
                .createChooserIntent();

        Preference debugPreference = findPreference(debugPrefKey);
        debugPreference.setIntent(intent);
        debugPreference.setSummary(debugPrefUrl);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        if (SDK_INT >= LOLLIPOP) {
            setupUsagePreference();
        }
        setupVersionPreference();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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
                usageStatsPreference.setTitle(
                        getErrorString(getActivity(), getString(R.string.pref_title_usage_stats)));
                usageStatsPreference.setSummary(
                        getErrorString(getActivity(), getString(R.string.pref_summary_usage_stats)));
                usageStatsPreference.setWidgetLayoutResource(R.layout.preference_widget_error);
            }
        }

        if (usageAccessPref.get() != usageAccessGiven) {
            usageAccessPref.set(usageAccessGiven);

            analytics.sendEvent(
                    "Usage Access",
                    "Access Given",
                    Boolean.toString(usageAccessGiven));
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

    @TargetApi(LOLLIPOP)
    @Override
    public boolean onPreferenceClick(Preference preference) {

        if (getString(R.string.pref_key_about).equals(preference.getKey())) {
            startActivity(new Intent(getActivity(), IntroActivity.class));
        } else if (getString(R.string.pref_key_preferred).equals(preference.getKey())) {
            startActivity(new Intent(getActivity(), PreferredAppsActivity.class));
        } else if (getString(R.string.pref_key_usage_stats).equals(preference.getKey())) {
            onUsageAccessClick(preference);
        } else if (getString(R.string.pref_key_open_source).equals(preference.getKey())) {
            displayLicensesDialogFragment();
        } else if (getString(R.string.pref_key_contact).equals(preference.getKey())) {
            startContactEmailChooser();
        }

        analytics.sendEvent(
                "Preference",
                "Item Click",
                preference.getTitle().toString());
        return true;
    }

    private void onUsageAccessClick(Preference preference) {
        boolean settingsOpened = Intents.maybeStartUsageAccessSettings(getActivity());

        if (!settingsOpened) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.error)
                    .setMessage(R.string.error_usage_access_not_found)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();

            preference.setSummary(R.string.error_usage_access_not_found);
        }
    }

    private void displayLicensesDialogFragment() {
        LicensesDialogFragment dialog = LicensesDialogFragment.newInstance();
        dialog.show(getFragmentManager(), "LicensesDialog");
    }

    private void startContactEmailChooser() {
        ShareCompat.IntentBuilder.from(getActivity())
                .addEmailTo("Said Tahsin Dane <tasomaniac+openlinkwith@gmail.com>")
                .setSubject(getString(R.string.app_name))
                .setType("message/rfc822")
                .startChooser();
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public View getView() {
        return super.getView();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        new BackupManager(getActivity()).dataChanged();
    }

    private Preference findPreference(@StringRes int keyResource) {
        return findPreference(getString(keyResource));
    }

    private static SpannableString getErrorString(final Context context, CharSequence originalString) {
        SpannableString errorSpan = new SpannableString(originalString);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(
                ContextCompat.getColor(context, R.color.error_color));
        errorSpan.setSpan(colorSpan, 0, originalString.length(), 0);
        return errorSpan;
    }
}
