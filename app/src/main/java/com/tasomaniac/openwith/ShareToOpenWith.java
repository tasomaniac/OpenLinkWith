package com.tasomaniac.openwith;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.tasomaniac.openwith.resolver.ResolverActivity;
import com.tasomaniac.openwith.util.Intents;
import com.tasomaniac.openwith.util.Urls;

import java.util.List;

import timber.log.Timber;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.*;
import static com.tasomaniac.openwith.data.OpenWithProvider.OpenWithHosts.withHost;
import static com.tasomaniac.openwith.util.Urls.fixUrls;

public class ShareToOpenWith extends Activity {

    private static final String[] PRIORITY_PACKAGES = new String[]{
            "com.whatsapp",
            "com.twitter.android",
            "com.facebook.katana",
            "com.facebook.orca",
            "com.google.android.youtube",
            "com.google.android.gm",
            "com.google.android.talk",
            "com.google.android.apps.plus",
            "com.google.android.apps.photos",
            "com.pandora.android",
            "com.instagram.android",
            "com.linkedin.android",
            "com.spotify.music",
            "com.pinterest",
            "com.medium.reader",
            "com.ubercab",
            "com.meetup",
            "com.tumblr",
            "com.badoo.mobile",
            "tv.periscope.android",
            "com.skype.raider"
    };

    public static final String EXTRA_FROM_DIRECT_SHARE = "EXTRA_FROM_DIRECT_SHARE";

    private static boolean isFromDirectShare(Intent intent) {
        return intent.getBooleanExtra(EXTRA_FROM_DIRECT_SHARE, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.getApp(this).getAnalytics().sendScreenView("ShareToOpenWith");
        if (isFromDirectShare(getIntent())) {
            App.getApp(this).getAnalytics().sendEvent(
                    "Direct Share",
                    "Clicked",
                    "true"
            );
        }

        final ShareCompat.IntentReader reader = ShareCompat.IntentReader.from(this);
        String foundUrl = Urls.extractUrlFrom(getIntent(), reader);

        if (foundUrl == null) {
            Toast.makeText(this, R.string.error_invalid_url, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        final String callerPackage = getCallerPackage();
        Uri uri = Uri.parse(fixUrls(foundUrl));
        Intent intentToHandle = new Intent(Intent.ACTION_VIEW, uri);

        ComponentName lastChosenComponent = null;
        final Cursor query = queryIntentWith(uri.getHost());
        if (query != null && query.moveToFirst()) {
            try {
                final boolean isPreferred = query.getInt(query.getColumnIndex(PREFERRED)) == 1;
                final boolean isLastChosen = query.getInt(query.getColumnIndex(LAST_CHOSEN)) == 1;

                if (isPreferred || isLastChosen) {
                    final String componentString = query.getString(query.getColumnIndex(COMPONENT));

                    lastChosenComponent = ComponentName.unflattenFromString(componentString);
                    ResolveInfo ri = getPackageManager().resolveActivity(
                            new Intent().setComponent(lastChosenComponent),
                            PackageManager.MATCH_DEFAULT_ONLY
                    );

                    if (isPreferred && ri != null) {
                        boolean isCallerPackagePreferred = ri.activityInfo.packageName.equals(callerPackage);
                        if (!isCallerPackagePreferred) {
                            String warning = getString(R.string.warning_open_link_with_name, ri.loadLabel(getPackageManager()));
                            Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();

                            intentToHandle.setComponent(lastChosenComponent);
                            intentToHandle.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT
                                                            | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                            try {
                                Intents.startActivityFixingIntent(this, intentToHandle);
                                finish();
                                return;
                            } catch (SecurityException e) {
                                Timber.e(e, "Security Exception for %s", lastChosenComponent.flattenToString());
                                getContentResolver().delete(withHost(uri.getHost()), null, null);
                            }
                        }
                    }
                }
            } finally {
                query.close();
            }
        }

        startActivity(intentToHandle
                              .putExtra(ShareCompat.EXTRA_CALLING_PACKAGE, callerPackage)
                              .putExtra(ResolverActivity.EXTRA_PRIORITY_PACKAGES, PRIORITY_PACKAGES)
                              .putExtra(ResolverActivity.EXTRA_LAST_CHOSEN_COMPONENT, lastChosenComponent)
                              .setClass(this, ResolverActivity.class));

        finish();
    }

    @Nullable
    private String getCallerPackage() {
        String callerPackage = getIntent().getStringExtra(ShareCompat.EXTRA_CALLING_PACKAGE);

        if (callerPackage != null) {
            return callerPackage;
        }
        if (SDK_INT < LOLLIPOP) {
            return getCallerPackagerLegacy();
        }
        if (SDK_INT >= LOLLIPOP_MR1) {
            return getCallerPackageLollipop();
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    private String getCallerPackagerLegacy() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(1);
        final ComponentName topActivity = runningTasks.get(0).baseActivity;
        return topActivity.getPackageName();
    }

    @TargetApi(LOLLIPOP_MR1)
    private String getCallerPackageLollipop() {
        UsageStatsManager mUsm = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        // We get usage stats for the last 10 seconds
        List<UsageStats> stats = null;
        try {
            stats = mUsm.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    time - 10 * DateUtils.SECOND_IN_MILLIS,
                    time
            );
        } catch (Exception ignored) {
        }
        if (stats == null) {
            return null;
        }

        UsageStats lastUsage = null;
        for (UsageStats currentUsage : stats) {
            String currentPackage = currentUsage.getPackageName();
            if (BuildConfig.APPLICATION_ID.equals(currentPackage)
                    || "android".equals(currentPackage)) {
                continue;
            }
            if (lastUsage == null ||
                    lastUsage.getLastTimeUsed() < currentUsage.getLastTimeUsed()) {
                lastUsage = currentUsage;
            }
        }
        if (lastUsage != null) {
            return lastUsage.getPackageName();
        }

        return null;
    }

    @Nullable
    private Cursor queryIntentWith(String host) {
        if (TextUtils.isEmpty(host)) {
            return null;
        }
        return getContentResolver().query(withHost(host), null, null, null, null);
    }
}
