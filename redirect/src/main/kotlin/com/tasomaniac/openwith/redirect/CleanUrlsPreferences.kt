package com.tasomaniac.openwith.redirect

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject

class CleanUrlsPreferences @Inject constructor(private val sharedPreferences: SharedPreferences) {

    val isEnabled: Boolean
        get() = sharedPreferences.getBoolean("pref_feature_clean_urls", false)

    var cleanUpRegex: Regex
        get() = sharedPreferences.getString("pref_feature_clean_urls_regex", DEFAULT_REGEX)!!.toRegex()
        set(value) = sharedPreferences.edit {
            putString("pref_feature_clean_urls_regex", value.pattern)
        }

    companion object {
        private const val DEFAULT_REGEX = "ref|utm|source|clickform"
    }
}
