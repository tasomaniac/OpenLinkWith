package com.tasomaniac.openwith.util

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.text.format.DateUtils
import androidx.core.app.ShareCompat
import androidx.core.content.getSystemService
import com.tasomaniac.openwith.BuildConfig
import com.tasomaniac.openwith.ShareToOpenWith
import dagger.Provides

sealed class CallerPackageExtractor {

    abstract fun extract(): String?

    @dagger.Module
    class Module {

        @Provides
        fun callerPackageExtractor(
            shareToOpenWith: ShareToOpenWith,
            callerPackagePreferences: CallerPackagePreferences
        ): CallerPackageExtractor {
            val callerPackage = ShareCompat.getCallingPackage(shareToOpenWith)

            return when {
                callerPackagePreferences.isEnabled.not() -> EmptyExtractor
                callerPackage != null -> SimpleExtractor(callerPackage)
                else -> LollipopExtractor(shareToOpenWith)
            }
        }
    }
}

private object EmptyExtractor : CallerPackageExtractor() {
    override fun extract(): String? = null
}

private class SimpleExtractor(private val callerPackage: String?) : CallerPackageExtractor() {

    override fun extract(): String? {
        return callerPackage
    }
}

private class LollipopExtractor(context: Context) : CallerPackageExtractor() {

    private val usageStatsManager = context.getSystemService<UsageStatsManager>()

    override fun extract() = usageStatsManager?.recentUsageStats()?.mostRecentPackage()

    private fun UsageStatsManager.recentUsageStats(): List<UsageStats>? {
        return try {
            val time = System.currentTimeMillis()
            queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                time - TEN_SECONDS,
                time
            )
        } catch (ignored: Exception) {
            null
        }
    }

    private fun List<UsageStats>.mostRecentPackage() =
        filter {
            it.packageName !in listOf(BuildConfig.APPLICATION_ID, "android")
        }.maxBy {
            it.lastTimeUsed
        }?.packageName

    companion object {
        private const val TEN_SECONDS = 10 * DateUtils.SECOND_IN_MILLIS
    }
}
