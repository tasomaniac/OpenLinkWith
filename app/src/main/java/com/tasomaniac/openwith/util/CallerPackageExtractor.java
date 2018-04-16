package com.tasomaniac.openwith.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ShareCompat;
import android.text.format.DateUtils;
import com.tasomaniac.openwith.BuildConfig;

import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;

public abstract class CallerPackageExtractor {

    public static CallerPackageExtractor from(Activity activity) {
        String callerPackage = ShareCompat.getCallingPackage(activity);

        if (callerPackage != null) {
            return new SimpleExtractor(callerPackage);
        }
        if (SDK_INT < LOLLIPOP) {
            return new LegacyExtractor(activity);
        }
        if (SDK_INT >= LOLLIPOP_MR1) {
            return new LollipopExtractor(activity);
        }

        return new SimpleExtractor(null);
    }

    @Nullable
    public abstract String extract();

    private static class SimpleExtractor extends CallerPackageExtractor {

        @Nullable
        private final String callerPackage;

        SimpleExtractor(@Nullable String callerPackage) {
            this.callerPackage = callerPackage;
        }

        @Override
        @Nullable
        public String extract() {
            return callerPackage;
        }
    }

    private static class LegacyExtractor extends CallerPackageExtractor {

        private ActivityManager activityManager;

        LegacyExtractor(Context context) {
            activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        }

        @Override
        @SuppressWarnings("deprecation")
        public String extract() {
            final List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(1);
            final ComponentName topActivity = runningTasks.get(0).baseActivity;
            return topActivity.getPackageName();
        }
    }

    @RequiresApi(LOLLIPOP_MR1)
    private static class LollipopExtractor extends CallerPackageExtractor {

        private UsageStatsManager usageStatsManager;

        LollipopExtractor(Context context) {
            usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        }

        @Override
        @Nullable
        public String extract() {
            List<UsageStats> stats = recentUsageStats(usageStatsManager);
            if (stats == null) {
                return null;
            }

            return mostRecentPackage(stats);
        }

        /**
         * Returns recently used apps for the last 10 seconds
         */
        @Nullable
        private static List<UsageStats> recentUsageStats(UsageStatsManager mUsm) {
            try {
                long time = System.currentTimeMillis();
                return mUsm.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        time - 10 * DateUtils.SECOND_IN_MILLIS,
                        time
                );
            } catch (Exception ignored) {
            }
            return null; // queryUsageStats also returns null
        }

        @Nullable
        private static String mostRecentPackage(List<UsageStats> stats) {
            UsageStats recentUsage = null;
            for (UsageStats currentUsage : stats) {
                String currentPackage = currentUsage.getPackageName();
                if (shouldIgnore(currentPackage)) {
                    continue;
                }
                if (recentUsage == null ||
                        recentUsage.getLastTimeUsed() < currentUsage.getLastTimeUsed()) {
                    recentUsage = currentUsage;
                }
            }
            return recentUsage == null ? null : recentUsage.getPackageName();
        }

        private static boolean shouldIgnore(String packageName) {
            return BuildConfig.APPLICATION_ID.equals(packageName)
                    || "android".equals(packageName);
        }
    }
}
