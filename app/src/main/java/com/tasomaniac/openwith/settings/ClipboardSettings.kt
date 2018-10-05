package com.tasomaniac.openwith.settings

import android.content.ClipboardManager
import androidx.preference.PreferenceCategory
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.redirect.RedirectFixActivity
import com.tasomaniac.openwith.util.Urls
import javax.inject.Inject

class ClipboardSettings @Inject constructor(
    fragment: SettingsFragment,
    private val clipboardManager: ClipboardManager,
    private val analytics: Analytics
) : Settings(fragment) {

    private lateinit var clipChangedListener: ClipboardManager.OnPrimaryClipChangedListener
    private var preferenceCategory: PreferenceCategory? = null

    override fun setup() {
        updateClipboard()

        clipChangedListener = ClipboardManager.OnPrimaryClipChangedListener { updateClipboard() }
        clipboardManager.addPrimaryClipChangedListener(clipChangedListener)
    }

    override fun release() {
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
        findPreference(R.string.pref_key_clipboard).apply {
            setOnPreferenceClickListener {
                context.startActivity(RedirectFixActivity.createIntent(activity, clipUrl))
                analytics.sendEvent("Clipboard", "Clicked", "Clicked")
                true
            }
            summary = clipUrl
        }
    }

    private fun clipUrl(): String? {
        return try {
            val primaryClip = clipboardManager.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
            Urls.findFirstUrl(primaryClip)
        } catch (e: Exception) {
            return null
        }
    }

    private fun addClipboardPreference() {
        addPreferencesFromResource(R.xml.pref_clipboard)
        preferenceCategory = findPreference(R.string.pref_key_category_clipboard) as PreferenceCategory
    }

    private fun remove() {
        removePreference(preferenceCategory!!)
        preferenceCategory = null
    }

    private fun isAdded() = preferenceCategory != null
}
