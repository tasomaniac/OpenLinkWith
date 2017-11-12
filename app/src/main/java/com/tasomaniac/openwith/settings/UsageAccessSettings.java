package com.tasomaniac.openwith.settings;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.data.Analytics;
import com.tasomaniac.openwith.data.prefs.BooleanPreference;
import com.tasomaniac.openwith.data.prefs.UsageAccess;
import com.tasomaniac.openwith.rx.SchedulingStrategy;
import com.tasomaniac.openwith.util.Intents;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class UsageAccessSettings {

    private final SettingsFragment fragment;
    private final BooleanPreference usageAccessPref;
    private final Analytics analytics;
    private final SchedulingStrategy schedulingStrategy;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private PreferenceCategory usageStatsPreferenceCategory;

    @Inject
    UsageAccessSettings(
            SettingsFragment fragment,
            @UsageAccess BooleanPreference usageAccessPref,
            Analytics analytics,
            SchedulingStrategy schedulingStrategy) {
        this.fragment = fragment;
        this.usageAccessPref = usageAccessPref;
        this.analytics = analytics;
        this.schedulingStrategy = schedulingStrategy;
    }

    void setup() {
        boolean usageAccessGiven = UsageStats.isEnabled(getContext());

        if (usageAccessGiven && isUsageAccessRequestAdded()) {
            remove();
        }

        if (!usageAccessGiven && !isUsageAccessRequestAdded()) {
            addUsageAccessRequest();
        }

        if (usageAccessPref.get() != usageAccessGiven) {
            usageAccessPref.set(usageAccessGiven);

            fragment.analytics.sendEvent(
                    "Usage Access",
                    "Access Given",
                    Boolean.toString(usageAccessGiven)
            );
        }
    }

    void release() {
        disposables.clear();
    }

    private void addUsageAccessRequest() {
        fragment.addPreferencesFromResource(R.xml.pref_usage);
        usageStatsPreferenceCategory = (PreferenceCategory) fragment.findPreference(R.string.pref_key_category_usage);

        Preference usageStatsPreference = fragment.findPreference(R.string.pref_key_usage_stats);
        usageStatsPreference.setOnPreferenceClickListener(this::onUsageAccessClick);

        //Set title and summary in red font.
        usageStatsPreference.setTitle(coloredErrorString(R.string.pref_title_usage_stats));
        usageStatsPreference.setSummary(coloredErrorString(R.string.pref_summary_usage_stats));
        usageStatsPreference.setWidgetLayoutResource(R.layout.preference_widget_error);
    }

    private boolean onUsageAccessClick(Preference preference) {
        boolean settingsOpened = UsageStats.maybeStartUsageAccessSettings(fragment.getActivity());

        if (settingsOpened) {
            observeUsageStats();
        } else {
            displayAlert();
            preference.setSummary(R.string.error_usage_access_not_found);
        }

        analytics.sendEvent("Preference", "Item Click", preference.getKey());
        return true;
    }

    private void observeUsageStats() {
        Disposable disposable = UsageStats.observeAccessGiven(getContext())
                .compose(schedulingStrategy.forCompletable())
                .subscribe(() -> Intents.restartSettings(getContext()));
        disposables.add(disposable);
    }

    private void displayAlert() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.error)
                .setMessage(R.string.error_usage_access_not_found)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private Context getContext() {
        return fragment.getContext();
    }

    private boolean isUsageAccessRequestAdded() {
        return usageStatsPreferenceCategory != null;
    }

    private void remove() {
        fragment.getPreferenceScreen().removePreference(usageStatsPreferenceCategory);
        usageStatsPreferenceCategory = null;
    }

    private CharSequence coloredErrorString(@StringRes int stringRes) {
        SpannableString errorSpan = new SpannableString(fragment.getString(stringRes));
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(errorColor());
        errorSpan.setSpan(colorSpan, 0, errorSpan.length(), 0);
        return errorSpan;
    }

    private int errorColor() {
        return ContextCompat.getColor(getContext(), R.color.error_color);
    }
}
