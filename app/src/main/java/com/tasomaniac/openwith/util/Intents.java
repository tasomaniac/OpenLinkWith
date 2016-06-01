package com.tasomaniac.openwith.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;

import com.tasomaniac.openwith.App;

import java.util.List;

import timber.log.Timber;

public class Intents {

    private static final Intents.Fixer[] INTENT_FIXERS = new Intents.Fixer[]{
            new AmazonFixer(),
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean maybeStartUsageAccessSettings(final Activity activity) {
        try {
            activity.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            return true;
        } catch (Exception e) {
            Timber.e(e, "Usage Access Open");

            App.getApp(activity).getAnalytics().sendEvent("Usage Access", "Not Found", null);
        }

        return false;
    }

    public static Intent fixIntents(Context context, Intent intent) {

        for (Fixer intentFixer : INTENT_FIXERS) {
            Intent fixedIntent = intentFixer.fix(context, intent);

            if (hasHandler(context, fixedIntent)) {
                intent = fixedIntent;
            }
        }
        return intent;
    }

    /**
     * Queries on-device packages for a handler for the supplied {@link Intent}.
     */
    private static boolean hasHandler(Context context, Intent intent) {
        List<ResolveInfo> handlers = context.getPackageManager().queryIntentActivities(intent, 0);
        return !handlers.isEmpty();
    }

    private static class AmazonFixer implements Fixer {

        /**
         * If link contains amazon and it has ASIN in it, create a specific intent to open Amazon App.
         *
         * @param intent Original Intent with amazon link in it.
         * @return Specific Intent for Amazon app.
         */
        @NonNull
        @Override
        public Intent fix(Context context, Intent intent) {
            if (intent.getDataString() != null && intent.getDataString().contains("amazon")) {
                String asin = Urls.AmazonFixer.extractAmazonASIN(intent.getDataString());
                if (asin != null) {
                    if ("0000000000".equals(asin)) {
                        return context.getPackageManager()
                                .getLaunchIntentForPackage(intent.getComponent().getPackageName());
                    }
                    return new Intent("android.intent.action.VIEW")
                            .setDataAndType(
                                    Uri.parse("mshop://featured?ASIN=" + asin),
                                    "vnd.android.cursor.item/vnd.amazon.mShop.featured"
                            );
                }
            }
            return intent;
        }
    }

    interface Fixer {
        Intent fix(Context context, Intent intent);
    }

    private Intents() {
    }
}
