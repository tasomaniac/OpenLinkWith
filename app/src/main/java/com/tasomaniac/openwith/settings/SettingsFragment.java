package com.tasomaniac.openwith.settings;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
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
import com.tasomaniac.openwith.data.Injector;
import com.tasomaniac.openwith.data.prefs.BooleanPreference;
import com.tasomaniac.openwith.data.prefs.UsageAccess;
import com.tasomaniac.openwith.intro.IntroActivity;
import com.tasomaniac.openwith.preferred.PreferredAppsActivity;
import com.tasomaniac.openwith.util.Intents;
import com.tasomaniac.openwith.util.Utils;

import javax.inject.Inject;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

    @Inject Analytics analytics;
    @Inject @UsageAccess BooleanPreference usageAccessPref;

    private PreferenceCategory usageStatsPreferenceCategory;

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
        Injector.obtain(getContext()).inject(this);
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
            setupDebugPrefCategory();
        }
    }

    private void setupDebugPrefCategory() {
        addPreferencesFromResource(R.xml.pref_debug);

        setupDebugPreference(
                getActivity(),
                findPreference(R.string.pref_key_debug_amazon),
                "http://www.amazon.com/Garmin-Speed-Cadence-Bike-Sensor/dp/B000BFNOT8"
        );
        setupDebugPreference(
                getActivity(),
                findPreference(R.string.pref_key_debug_maps),
                "http://maps.google.com/maps"
        );
        setupDebugPreference(
                getActivity(),
                findPreference(R.string.pref_key_debug_hangouts),
                "https://hangouts.google.com/hangouts/_/novoda.com/wormhole?authuser=tahsin@novoda.com"
        );
        setupDebugPreference(
                getActivity(),
                findPreference(R.string.pref_key_debug_play),
                "https://play.google.com/store/apps/details?id=com.tasomaniac.openwith"
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
                        coloredErrorString(
                                getActivity(),
                                getString(R.string.pref_title_usage_stats)
                        ));
                usageStatsPreference.setSummary(
                        coloredErrorString(
                                getActivity(),
                                getString(R.string.pref_summary_usage_stats)
                        ));
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
                preference.getKey()
        );
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        new BackupManager(getActivity()).dataChanged();
    }

    private Preference findPreference(@StringRes int keyResource) {
        return findPreference(getString(keyResource));
    }

    private static CharSequence coloredErrorString(Context context, CharSequence originalString) {
        SpannableString errorSpan = new SpannableString(originalString);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(
                ContextCompat.getColor(context, R.color.error_color));
        errorSpan.setSpan(colorSpan, 0, originalString.length(), 0);
        return errorSpan;
    }
}
