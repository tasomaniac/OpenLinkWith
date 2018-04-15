package com.tasomaniac.openwith.browser

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import com.tasomaniac.openwith.resolver.DisplayActivityInfo
import com.tasomaniac.openwith.resolver.IconLoader
import io.reactivex.Single
import javax.inject.Inject

class BrowserResolver @Inject constructor(
    private val packageManager: PackageManager,
    private val iconLoader: IconLoader
) {

    fun resolve(): Single<List<DisplayActivityInfo>> = Single.fromCallable {
        queryBrowsers().map {
            DisplayActivityInfo(
                it.activityInfo,
                it.loadLabel(packageManager),
                null,
                iconLoader.loadFor(it.activityInfo)
            )
        }
    }

    private fun queryBrowsers(): List<ResolveInfo> {
        val browserIntent = Intent()
            .setAction(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(Uri.parse("http:"))
        return packageManager.queryIntentActivities(browserIntent, 0)
    }

}
