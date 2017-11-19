package com.tasomaniac.openwith.settings

import android.os.Build
import android.support.annotation.RequiresApi
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceCategory
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.data.prefs.BooleanPreference
import com.tasomaniac.openwith.data.prefs.UsageAccess
import com.tasomaniac.openwith.rx.SchedulingStrategy
import com.tasomaniac.openwith.util.Intents
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class UsageAccessSettings @Inject constructor(
    @param:UsageAccess private val usageAccessPref: BooleanPreference,
    private val analytics: Analytics,
    private val schedulingStrategy: SchedulingStrategy,
    fragment: SettingsFragment
) : Settings(fragment) {

  private val disposables = CompositeDisposable()

  private var preferenceCategory: PreferenceCategory? = null

  private val isUsageAccessRequestAdded get() = preferenceCategory != null

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
          java.lang.Boolean.toString(usageAccessGiven)
      )
    }
  }

  override fun release() {
    disposables.clear()
  }

  private fun addUsageAccessRequest() {
    addPreferencesFromResource(R.xml.pref_usage)
    preferenceCategory = findPreference(R.string.pref_key_category_usage) as PreferenceCategory

    findPreference(R.string.pref_key_usage_stats).apply {
      setOnPreferenceClickListener({ onUsageAccessClick(it) })

      //Set title and summary in red font.
      title = coloredErrorString(R.string.pref_title_usage_stats)
      summary = coloredErrorString(R.string.pref_summary_usage_stats)
      widgetLayoutResource = R.layout.preference_widget_error
    }
  }

  private fun onUsageAccessClick(preference: Preference): Boolean {
    val settingsOpened = UsageStats.maybeStartUsageAccessSettings(activity)

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
    val disposable = UsageStats.observeAccessGiven(context)
        .compose(schedulingStrategy.forCompletable())
        .subscribe { Intents.restartSettings(context) }
    disposables.add(disposable)
  }

  private fun displayAlert() {
    AlertDialog.Builder(context)
        .setTitle(R.string.error)
        .setMessage(R.string.error_usage_access_not_found)
        .setPositiveButton(android.R.string.ok, null)
        .show()
  }

  private fun remove() {
    removePreference(preferenceCategory!!)
    preferenceCategory = null
  }

  private fun coloredErrorString(@StringRes stringRes: Int): CharSequence {
    val errorSpan = SpannableString(context.getString(stringRes))
    val colorSpan = ForegroundColorSpan(errorColor())
    errorSpan.setSpan(colorSpan, 0, errorSpan.length, 0)
    return errorSpan
  }

  private fun errorColor(): Int = ContextCompat.getColor(context, R.color.error_color)
}
