package com.tasomaniac.openwith.redirect

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import com.tasomaniac.openwith.util.Intents
import com.tasomaniac.openwith.util.componentName
import javax.inject.Inject

class BrowserIntentChecker @Inject constructor(private val packageManager: PackageManager) {

    fun hasOnlyBrowsers(sourceIntent: Intent): Boolean {
        if (!Intents.isHttp(sourceIntent)) {
            return false
        }
        val flag = if (SDK_INT >= M) PackageManager.MATCH_ALL else PackageManager.MATCH_DEFAULT_ONLY
        val resolved = toComponents(packageManager.queryIntentActivities(sourceIntent, flag)).toMutableSet()
        val browsers = toComponents(queryBrowsers())

        resolved.removeAll(browsers)
        return resolved.isEmpty()
    }

    private fun toComponents(list: List<ResolveInfo>): List<ComponentName> {
        return list
            .map(ResolveInfo::activityInfo)
            .map { it.componentName() }
    }

    private fun queryBrowsers(): List<ResolveInfo> {
        val browserIntent = Intent()
            .setAction(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(Uri.parse("http:"))
        return packageManager.queryIntentActivities(browserIntent, 0)
    }
}
