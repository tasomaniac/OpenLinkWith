package com.tasomaniac.openwith.settings

import android.content.ClipboardManager
import android.support.v7.preference.PreferenceCategory
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.redirect.RedirectFixActivity
import com.tasomaniac.openwith.util.Urls
import javax.inject.Inject

class ClipboardSettings @Inject constructor(
    private val fragment: SettingsFragment,
    private val clipboardManager: ClipboardManager,
    private val analytics: Analytics
) {

  private val context get() = fragment.context

  private lateinit var clipChangedListener: ClipboardManager.OnPrimaryClipChangedListener
  private var preferenceCategory: PreferenceCategory? = null

  fun setup() {
    updateClipboard()

    clipChangedListener = ClipboardManager.OnPrimaryClipChangedListener { updateClipboard() }
    clipboardManager.addPrimaryClipChangedListener(clipChangedListener)
  }

  fun release() {
    clipboardManager.removePrimaryClipChangedListener(clipChangedListener)
  }

  private fun updateClipboard() {
    val clipUrl = clipUrl()

    if (clipUrl == null && isAdded()) {
      remove()
    }

    if (clipUrl != null) {
      if (!isAdded()) {
        addClipboardPreference()
        analytics.sendEvent("Clipboard", "Added", "New")
      }

      updateClipUrl(clipUrl)
    }
  }

  private fun updateClipUrl(clipUrl: String) {
    fragment.findPreference(R.string.pref_key_clipboard).apply {
      setOnPreferenceClickListener {
        context.startActivity(RedirectFixActivity.createIntent(context, clipUrl))
        analytics.sendEvent("Clipboard", "Clicked", "Clicked")
        true
      }
      summary = clipUrl
    }
  }

  private fun clipUrl(): String? {
    if (clipboardManager.hasPrimaryClip()) {
      val primaryClip = clipboardManager.primaryClip.getItemAt(0).coerceToText(context)
      return Urls.findFirstUrl(primaryClip.toString())
    }
    return null
  }

  private fun addClipboardPreference() {
    fragment.addPreferencesFromResource(R.xml.pref_clipboard)
    preferenceCategory = fragment.findPreference(R.string.pref_key_category_clipboard) as PreferenceCategory
  }

  private fun remove() {
    fragment.preferenceScreen.removePreference(preferenceCategory)
    preferenceCategory = null
  }

  private fun isAdded() = preferenceCategory != null

}
