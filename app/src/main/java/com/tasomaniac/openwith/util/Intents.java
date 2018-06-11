package com.tasomaniac.openwith.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import com.tasomaniac.openwith.settings.SettingsActivity;

import java.util.List;

public class Intents {

    private static final Intents.Fixer[] INTENT_FIXERS = new Intents.Fixer[]{
            new AmazonFixer(),
    };

    public static void restartSettings(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static Intent homeScreenIntent() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return homeIntent;
    }

    public static boolean isHttp(Intent intent) {
        String scheme = intent.getScheme();
        return "http".equals(scheme) || "https".equals(scheme);
    }

    public static void startActivityFixingIntent(Context context, Intent intent)
            throws SecurityException, ActivityNotFoundException {
        context.startActivity(fixIntents(context, intent));
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
        @Override
        public Intent fix(Context context, Intent intent) {
            if (intent.getDataString() != null && intent.getDataString().contains("amazon")) {
                String asin = Urls.AmazonFixer.extractAmazonASIN(intent.getDataString());
                if (asin != null) {
                    if ("0000000000".equals(asin)) {
                        return context.getPackageManager()
                                .getLaunchIntentForPackage(intent.getComponent().getPackageName());
                    }
                    return new Intent(Intent.ACTION_VIEW).setDataAndType(
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
        //no instance
    }
}
