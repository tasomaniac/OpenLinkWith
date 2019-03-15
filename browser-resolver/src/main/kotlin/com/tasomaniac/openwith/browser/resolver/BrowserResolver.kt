package com.tasomaniac.openwith.browser.resolver

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import com.tasomaniac.openwith.resolver.DisplayActivityInfo
import com.tasomaniac.openwith.resolver.IconLoader
import io.reactivex.Single
import javax.inject.Inject

class BrowserResolver @Inject constructor(
    private val application: Application,
    private val packageManager: PackageManager,
    private val iconLoader: IconLoader
) {

    fun resolve(): Single<List<DisplayActivityInfo>> = Single.fromCallable {
        queryBrowsers().map {
            DisplayActivityInfo(
                it.activityInfo,
                it.loadLabel(packageManager),
                null
            ).apply {
                displayIcon = iconLoader.loadFor(it.activityInfo)
            }
        }
    }

    fun queryBrowsers(): List<ResolveInfo> {
        val flag = if (SDK_INT >= M) PackageManager.MATCH_ALL else 0
        val browserIntent = Intent()
            .setAction(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(Uri.parse("http:"))

        val resolvedBrowsers = packageManager.queryIntentActivities(browserIntent, flag)
        resolvedBrowsers.removeAll {
            it.activityInfo.packageName == application.packageName
        }
        return resolvedBrowsers.distinctBy {
            it.activityInfo.packageName
        }
    }
}
