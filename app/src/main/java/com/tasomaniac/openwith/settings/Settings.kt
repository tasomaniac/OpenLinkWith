package com.tasomaniac.openwith.settings

import android.content.Intent
import androidx.annotation.StringRes
import androidx.annotation.XmlRes
import androidx.preference.Preference

abstract class Settings(
    private val fragment: SettingsFragment
) : SettingsView {

  val context get() = fragment.context!!
  val activity get() = fragment.activity!!

  fun addPreferencesFromResource(@XmlRes resId: Int) = fragment.addPreferencesFromResource(resId)

  fun removePreference(preference: Preference) = fragment.preferenceScreen.removePreference(preference)

  fun startActivity(intent: Intent) = fragment.startActivity(intent)

  fun findPreference(@StringRes keyResource: Int): Preference = fragment.run {
    findPreference(getString(keyResource))
  }

  fun Preference.isKeyEquals(@StringRes keyRes: Int) = key.isKeyEquals(keyRes)

  fun String.isKeyEquals(@StringRes keyRes: Int) = fragment.getString(keyRes) == this
}
