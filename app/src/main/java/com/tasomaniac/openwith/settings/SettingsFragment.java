package com.tasomaniac.openwith.settings;

import android.annotation.TargetApi;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v14.preference.PreferenceFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
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
import com.tasomaniac.openwith.util.Utils;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class SettingsFragment extends PreferenceFragment
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

        setupVersionPreference();
    }

    private void setupVersionPreference() {
        StringBuilder version = new StringBuilder(BuildConfig.VERSION_NAME);
        if (BuildConfig.DEBUG) {
            version.append(" (")
                    .append(BuildConfig.VERSION_CODE)
                    .append(")");
        }
        findPreference(R.string.pref_key_version).setTitle(getString(R.string.pref_title_version, version));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        if (SDK_INT >= LOLLIPOP) {
            setupUsagePreference();
        }
    }

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

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @TargetApi(LOLLIPOP)
    @Override
    public boolean onPreferenceClick(Preference preference) {

        if (getString(R.string.pref_key_about).equals(preference.getKey())) {
            startActivity(new Intent(getActivity(), IntroActivity.class));
        } else if (getString(R.string.pref_key_preferred).equals(preference.getKey())) {
            startActivity(new Intent(getActivity(), PreferredAppsActivity.class));
        } else if (getString(R.string.pref_key_usage_stats).equals(preference.getKey())) {
            Utils.maybeStartUsageAccessSettings(getActivity());
        } else if (getString(R.string.pref_key_open_source).equals(preference.getKey())) {
            displayLicensesDialogFragment();
        } else if (getString(R.string.pref_key_contact).equals(preference.getKey())) {
            ShareCompat.IntentBuilder.from(getActivity())
                    .addEmailTo("Said Tahsin Dane <tasomaniac+openlinkwith@gmail.com>")
                    .setSubject(getString(R.string.app_name))
                    .setType("message/rfc822")
                    .startChooser();
        }

        analytics.sendEvent(
                "Preference",
                "Item Click",
                preference.getTitle().toString());
        return true;
    }

    private void displayLicensesDialogFragment() {
        LicensesDialogFragment dialog = LicensesDialogFragment.newInstance();
        dialog.show(getActivity().getFragmentManager(), "LicensesDialog");
    }

    public Preference findPreference(@StringRes int keyResource) {
        return findPreference(getString(keyResource));
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

    private static SpannableString getErrorString(final Context context, CharSequence originalString) {
        SpannableString errorSpan = new SpannableString(originalString);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(
                ContextCompat.getColor(context, R.color.error_color));
        errorSpan.setSpan(colorSpan, 0, originalString.length(), 0);
        return errorSpan;
    }
}
