package com.tasomaniac.openwith.settings.advanced.usage

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.data.prefs.BooleanPreference
import com.tasomaniac.openwith.data.prefs.UsageAccess
import com.tasomaniac.openwith.extensions.restart
import com.tasomaniac.openwith.rx.SchedulingStrategy
import com.tasomaniac.openwith.settings.Settings
import com.tasomaniac.openwith.settings.SettingsFragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class UsageAccessSettings @Inject constructor(
    @param:UsageAccess private val usageAccessPref: BooleanPreference,
    private val analytics: Analytics,
    private val schedulingStrategy: SchedulingStrategy,
    fragment: SettingsFragment
) : Settings(fragment) {

    private val disposables = CompositeDisposable()

    private var preference: Preference? = null

    private val isUsageAccessRequestAdded get() = preference != null

    override fun resume() {
        val usageAccessGiven = UsageStats.isEnabled(context)

        if (usageAccessGiven && isUsageAccessRequestAdded) {
            remove()
        }

        if (!usageAccessGiven && !isUsageAccessRequestAdded) {
            addUsageAccessRequest()
        }

        if (usageAccessPref.get() != usageAccessGiven) {
            usageAccessPref.set(usageAccessGiven)

            analytics.sendEvent(
                "Usage Access",
                "Access Given",
                usageAccessGiven.toString()
            )
        }
    }

    override fun release() {
        disposables.clear()
    }

    private fun addUsageAccessRequest() {
        addPreferencesFromResource(R.xml.pref_usage)

        preference = findPreference(R.string.pref_key_usage_stats).apply {
            setOnPreferenceClickListener { onUsageAccessClick(it) }

            // Set title and summary in red font.
            title = coloredErrorString(R.string.pref_title_usage_stats)
            summary = coloredErrorString(R.string.pref_summary_usage_stats)
            widgetLayoutResource = R.layout.preference_widget_error
        }
    }

    private fun onUsageAccessClick(preference: Preference): Boolean {
        val settingsOpened = activity.maybeStartUsageAccessSettings()

        if (settingsOpened) {
            observeUsageStats()
        } else {
            displayAlert()
            preference.setSummary(R.string.error_usage_access_not_found)
        }

        analytics.sendEvent("Preference", "Item Click", preference.key)
        return true
    }

    private fun observeUsageStats() {
        UsageStats.observeAccessGiven(context)
            .compose(schedulingStrategy.forCompletable())
            .subscribe { context.restart() }
            .addTo(disposables)
    }

    private fun displayAlert() {
        AlertDialog.Builder(context)
            .setTitle(R.string.error)
            .setMessage(R.string.error_usage_access_not_found)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun remove() {
        removePreference(preference!!)
        preference = null
    }

    private fun coloredErrorString(@StringRes stringRes: Int): CharSequence {
        val errorSpan = SpannableString(context.getString(stringRes))
        val colorSpan = ForegroundColorSpan(errorColor())
        errorSpan.setSpan(colorSpan, 0, errorSpan.length, 0)
        return errorSpan
    }

    private fun errorColor(): Int = ContextCompat.getColor(context, R.color.error_color)
}
