package com.tasomaniac.openwith.settings

import android.content.Intent
import android.support.annotation.StringRes
import android.support.annotation.XmlRes
import android.support.v7.preference.Preference

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
