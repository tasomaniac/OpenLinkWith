package com.tasomaniac.openwith.settings.rating

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AskForRatingCondition(private val prefs: SharedPreferences) {

    @Inject
    constructor(app: Application) : this(app.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE))

    init {
        if (firstLaunchInMillis == -1L) {
            prefs.edit {
                putLong(PREF_KEY_FIRST_LAUNCH, System.currentTimeMillis())
            }
        }
    }

    fun shouldDisplay(): Boolean {
        return alreadyShown.not() &&
                firstLaunchInMillis <= LAUNCH_DAY_THRESHOLD.daysAgo() &&
                launchCount >= LAUNCH_TIMES_THRESHOLD
    }

    var alreadyShown: Boolean
        get() = prefs.getBoolean(PREF_KEY_ALREADY_SHOWN, false)
        set(value) = prefs.edit {
            putBoolean(PREF_KEY_ALREADY_SHOWN, value)
        }

    fun notifyAppLaunch() {
        prefs.edit {
            putInt(PREF_KEY_LAUNCH_COUNT, launchCount + 1)
        }
    }

    private val launchCount: Int
        get() = prefs.getInt(PREF_KEY_LAUNCH_COUNT, 0)

    private val firstLaunchInMillis
        get() = prefs.getLong(PREF_KEY_FIRST_LAUNCH, -1L)

    companion object {

        fun Int.daysAgo() = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(toLong())

        private const val PREF_NAME = "askForRating"
        private const val PREF_KEY_FIRST_LAUNCH = "firstLaunch"
        private const val PREF_KEY_LAUNCH_COUNT = "appLaunchCount"
        private const val PREF_KEY_ALREADY_SHOWN = "alreadyShown"

        private const val LAUNCH_DAY_THRESHOLD = 3
        private const val LAUNCH_TIMES_THRESHOLD = 5L
    }
}
