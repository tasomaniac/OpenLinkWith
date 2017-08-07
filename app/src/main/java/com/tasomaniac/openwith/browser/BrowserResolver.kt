package com.tasomaniac.openwith.browser

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import com.tasomaniac.openwith.resolver.DisplayResolveInfo
import io.reactivex.Single
import javax.inject.Inject


class BrowserResolver @Inject
constructor(private val packageManager: PackageManager) {

    fun resolve(): Single<List<DisplayResolveInfo>> =
            Single.fromCallable<List<DisplayResolveInfo>> {
                queryBrowsers().map {
                    DisplayResolveInfo(it, it.loadLabel(packageManager), null)
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
