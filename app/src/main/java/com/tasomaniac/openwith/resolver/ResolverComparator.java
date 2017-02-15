package com.tasomaniac.openwith.resolver;

import android.annotation.TargetApi;
import android.app.usage.UsageStats;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import com.tasomaniac.openwith.util.Intents;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

class ResolverComparator implements Comparator<ResolveInfo> {

    private final PackageManager packageManager;
    private final ChooserHistory history;
    private final Map<String, UsageStats> usageStatsMap;
    private final Map<String, Integer> priorityPackages;
    private final Collator collator;
    private final boolean isHttp;

    ResolverComparator(PackageManager packageManager,
                       ChooserHistory history,
                       Map<String, UsageStats> usageStatsMap,
                       Map<String, Integer> priorityPackages,
                       Intent sourceIntent) {
        this.packageManager = packageManager;
        this.history = history;
        this.usageStatsMap = usageStatsMap;
        this.priorityPackages = priorityPackages;
        this.collator = Collator.getInstance(Locale.getDefault());
        this.isHttp = Intents.isHttp(sourceIntent);
    }

    @Override
    public int compare(ResolveInfo lhs, ResolveInfo rhs) {

        if (isHttp) {
            // Special case: we want filters that match URI paths/schemes to be
            // ordered before others.  This is for the case when opening URIs,
            // to make native apps go above browsers.
            final boolean lhsSpecific = isSpecificUriMatch(lhs.match);
            final boolean rhsSpecific = isSpecificUriMatch(rhs.match);
            if (lhsSpecific != rhsSpecific) {
                return lhsSpecific ? -1 : 1;
            }
        }

        if (history != null) {
            int leftCount = history.get(lhs.activityInfo.packageName);
            int rightCount = history.get(rhs.activityInfo.packageName);
            if (leftCount != rightCount) {
                return rightCount - leftCount;
            }
        }

        if (priorityPackages != null) {
            int leftPriority = getPriority(lhs);
            int rightPriority = getPriority(rhs);
            if (leftPriority != rightPriority) {
                return rightPriority - leftPriority;
            }
        }

        if (usageStatsMap != null) {
            final long timeDiff =
                    getPackageTimeSpent(rhs.activityInfo.packageName) -
                            getPackageTimeSpent(lhs.activityInfo.packageName);

            if (timeDiff != 0) {
                return timeDiff > 0 ? 1 : -1;
            }
        }

        CharSequence sa = lhs.loadLabel(packageManager);
        if (sa == null) {
            sa = lhs.activityInfo.name;
        }
        CharSequence sb = rhs.loadLabel(packageManager);
        if (sb == null) {
            sb = rhs.activityInfo.name;
        }

        return collator.compare(sa.toString(), sb.toString());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private long getPackageTimeSpent(String packageName) {
        if (usageStatsMap != null) {
            final UsageStats stats = usageStatsMap.get(packageName);
            if (stats != null) {
                return stats.getTotalTimeInForeground();
            }

        }
        return 0;
    }

    private int getPriority(ResolveInfo lhs) {
        final Integer integer = priorityPackages.get(lhs.activityInfo.packageName);
        return integer != null ? integer : 0;
    }

    private static boolean isSpecificUriMatch(int match) {
        match = match & IntentFilter.MATCH_CATEGORY_MASK;
        return match >= IntentFilter.MATCH_CATEGORY_HOST
                && match <= IntentFilter.MATCH_CATEGORY_PATH;
    }
}
