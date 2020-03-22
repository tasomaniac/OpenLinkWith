package com.tasomaniac.openwith

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

internal class CrashReportingTree : Timber.Tree() {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }
        crashlytics.log("$tag: $message")
        if (throwable != null && priority >= Log.WARN) {
            crashlytics.recordException(throwable)
        }
    }
}
