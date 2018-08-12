package com.tasomaniac.openwith.redirect

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import com.tasomaniac.openwith.browser.resolver.BrowserResolver
import com.tasomaniac.openwith.util.Intents
import com.tasomaniac.openwith.util.componentName
import javax.inject.Inject

class BrowserIntentChecker @Inject constructor(
    private val packageManager: PackageManager,
    private val browserResolver: BrowserResolver
) {

    fun hasOnlyBrowsers(sourceIntent: Intent): Boolean {
        if (!Intents.isHttp(sourceIntent)) {
            return false
        }
        val flag = if (SDK_INT >= M) PackageManager.MATCH_ALL else PackageManager.MATCH_DEFAULT_ONLY
        val resolved = packageManager.queryIntentActivities(sourceIntent, flag).toComponents()
        val browsers = browserResolver.queryBrowsers().toComponents()

        return (resolved - browsers).isEmpty()
    }

    private fun List<ResolveInfo>.toComponents() = map { it.activityInfo.componentName() }

}
