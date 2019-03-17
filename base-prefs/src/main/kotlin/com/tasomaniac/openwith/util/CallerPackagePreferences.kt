package com.tasomaniac.openwith.util

import android.content.SharedPreferences
import javax.inject.Inject

class CallerPackagePreferences @Inject constructor(private val sharedPreferences: SharedPreferences) {

    val isEnabled: Boolean
        get() = sharedPreferences.getBoolean("pref_feature_caller_app", false)
}
