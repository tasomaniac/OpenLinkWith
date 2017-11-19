package com.tasomaniac.openwith.settings

import android.app.backup.BackupManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

  @JvmSuppressWildcards
  @Inject lateinit var settings: Set<Settings>
  @Inject lateinit var sharedPreferences: SharedPreferences

  override fun onAttach(context: Context) {
    AndroidSupportInjection.inject(this)
    super.onAttach(context)
  }

  override fun onCreatePreferences(bundle: Bundle?, s: String?) {
    settings.forEach { it.setup() }
  }

  override fun onResume() {
    super.onResume()
    sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    settings.forEach { it.resume() }
  }

  override fun onPause() {
    settings.forEach { it.pause() }
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    super.onPause()
  }

  override fun onDestroy() {
    settings.forEach { it.release() }
    super.onDestroy()
  }

  override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
    BackupManager(activity).dataChanged()
  }

  companion object {

    @JvmStatic
    fun newInstance() = SettingsFragment().apply {
      val args = Bundle()
      arguments = args
    }
  }
}
