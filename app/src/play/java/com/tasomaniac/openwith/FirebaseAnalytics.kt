package com.tasomaniac.openwith

import android.app.Application
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.tasomaniac.openwith.data.Analytics

internal class FirebaseAnalytics(application: Application) : Analytics {

    private val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(application)

    override fun sendScreenView(screenName: String) = Unit

    override fun sendEvent(category: String, action: String, label: String) {
        analytics.logEvent(category, bundleOf(action to label))
    }
}
