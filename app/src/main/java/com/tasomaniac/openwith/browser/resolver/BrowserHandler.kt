package com.tasomaniac.openwith.browser.resolver

import android.content.pm.ResolveInfo
import android.os.Build
import com.tasomaniac.openwith.browser.BrowserPreferences
import com.tasomaniac.openwith.browser.BrowserPreferences.Mode
import com.tasomaniac.openwith.util.componentName
import com.tasomaniac.openwith.util.isEqualTo
import java.util.ArrayList
import javax.inject.Inject

class BrowserHandler(
    private val browserResolver: BrowserResolver,
    private val browserPreferences: BrowserPreferences,
    private val currentResolveList: MutableList<ResolveInfo>
) {

    /**
     * First add all browsers into the list if the device is > [Build.VERSION_CODES.M]
     *
     * Then depending on the browser preference,
     *
     * - Remove all browsers
     * - Only put the selected browser
     *
     * If the selected browser is not found, fallback to [Mode.AlwaysAsk]
     *
     */
    fun handleBrowsers() {
        val browsers = browserResolver.queryBrowsers()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addAllBrowsers(browsers)
        }

        val mode = browserPreferences.mode
        when (mode) {
            is Mode.None -> removeBrowsers(browsers)
            is Mode.Browser -> {
                removeBrowsers(browsers)
                val found = browsers.find { it.activityInfo.componentName() == mode.componentName }
                if (found != null) {
                    currentResolveList.add(found)
                } else {
                    browserPreferences.mode = Mode.AlwaysAsk
                }
            }
        }
    }

    private fun removeBrowsers(browsers: List<ResolveInfo>) {
        val toRemove = currentResolveList.filter { resolve ->
            browsers.find { browser ->
                resolve.activityInfo.isEqualTo(browser.activityInfo)
            } != null
        }
        currentResolveList.removeAll(toRemove)
    }

    private fun addAllBrowsers(browsers: List<ResolveInfo>) {
        val initialList = ArrayList(currentResolveList)

        browsers.forEach { browser ->
            val notFound = initialList.find {
                it.activityInfo.isEqualTo(browser.activityInfo)
            } == null
            if (notFound) {
                currentResolveList.add(browser)
            }
        }
    }

    class Factory @Inject constructor(
        private val browserResolver: BrowserResolver,
        private val browserPreferences: BrowserPreferences
    ) {

        fun create(currentResolveList: MutableList<ResolveInfo>) =
            BrowserHandler(
                browserResolver,
                browserPreferences,
                currentResolveList
            )
    }
}
