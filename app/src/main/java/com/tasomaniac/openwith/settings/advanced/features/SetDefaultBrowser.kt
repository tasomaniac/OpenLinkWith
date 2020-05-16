package com.tasomaniac.openwith.settings.advanced.features

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.N
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.text.parseAsHtml
import com.tasomaniac.openwith.R
import timber.log.Timber
import javax.inject.Inject

class SetDefaultBrowser @Inject constructor(
    private val activity: ToggleFeatureActivity,
    private val packageManager: PackageManager
) : FeatureToggleSideEffect {

    override fun featureToggled(feature: Feature, enabled: Boolean) {
        if (feature != Feature.BROWSER || !enabled) return

        val intent = intentToSetDefaultBrowser()
        if (packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
            return
        }

        displayWarningToSetDefaultBrowser(intent)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun displayWarningToSetDefaultBrowser(intent: Intent) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.pref_title_feature_browser)
            .setMessage(activity.getString(R.string.feature_default_browser_message).parseAsHtml())
            .setPositiveButton(android.R.string.ok) { _, _ ->
                try {
                    activity.startActivity(intent)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
            .show()
    }

    private fun intentToSetDefaultBrowser(): Intent {
        return if (SDK_INT >= N) {
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        } else {
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        }
    }
}
