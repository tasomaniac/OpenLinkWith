package com.tasomaniac.openwith.settings

import androidx.core.app.ShareCompat
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.forEach
import com.tasomaniac.openwith.R
import javax.inject.Inject

class DebugSettings @Inject constructor(
    fragment: SettingsFragment
) : Settings(fragment) {

    override fun setup() {
        addPreferencesFromResource(R.xml.pref_debug)
        val debugCategory = findPreference(R.string.pref_key_category_debug) as PreferenceCategory
        debugCategory.forEach {
            it.setupDebugPreference()
        }
    }

    private fun Preference.setupDebugPreference() {
        intent = ShareCompat.IntentBuilder(activity)
            .setText(summary)
            .setType("text/plain")
            .createChooserIntent()
    }
}
