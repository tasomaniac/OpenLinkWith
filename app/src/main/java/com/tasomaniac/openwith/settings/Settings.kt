package com.tasomaniac.openwith.settings

import androidx.annotation.StringRes
import androidx.annotation.XmlRes
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

abstract class Settings(
    val fragment: PreferenceFragmentCompat
) : SettingsView {

    val context get() = fragment.context!!
    val activity get() = fragment.activity!!

    fun addPreferencesFromResource(@XmlRes resId: Int) = fragment.addPreferencesFromResource(resId)

    fun removePreference(preference: Preference) = fragment.preferenceScreen.removePreference(preference)

    fun findPreference(@StringRes keyResource: Int): Preference = fragment.run {
        findPreference(getString(keyResource))
    }

    fun Preference.isKeyEquals(@StringRes keyRes: Int) = key.isKeyEquals(keyRes)

    fun String.isKeyEquals(@StringRes keyRes: Int) = fragment.getString(keyRes) == this
}
