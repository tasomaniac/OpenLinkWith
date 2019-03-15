package com.tasomaniac.openwith.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.util.List;

import static com.tasomaniac.openwith.extensions.AmazonASINKt.extractAmazonASIN;

public class IntentFixer {
    private static final Fixer[] INTENT_FIXERS = new Fixer[]{
            new AmazonFixer(),
    };

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

    interface Fixer {
        Intent fix(Context context, Intent intent);
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
                String asin = extractAmazonASIN(intent.getDataString());
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
}
