package com.tasomaniac.openwith.settings

import android.content.Context
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat() {

  @JvmSuppressWildcards
  @Inject lateinit var settings: Set<Settings>

  override fun onAttach(context: Context) {
    AndroidSupportInjection.inject(this)
    super.onAttach(context)
  }

  override fun onCreatePreferences(bundle: Bundle?, s: String?) {
    settings.forEach { it.setup() }
  }

  override fun onResume() {
    super.onResume()
    settings.forEach { it.resume() }
  }

  override fun onPause() {
    settings.forEach { it.pause() }
    super.onPause()
  }

  override fun onDestroy() {
    settings.forEach { it.release() }
    super.onDestroy()
  }

  companion object {

    @JvmStatic
    fun newInstance() = SettingsFragment().apply {
      val args = Bundle()
      arguments = args
    }
  }
}
