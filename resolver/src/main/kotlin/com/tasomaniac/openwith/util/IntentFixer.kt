package com.tasomaniac.openwith.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.tasomaniac.openwith.extensions.extractAmazonASIN

object IntentFixer {
    private val INTENT_FIXERS = arrayOf<Fixer>(
        AmazonFixer()
    )

    @JvmStatic
    fun fixIntents(context: Context, originalIntent: Intent): Intent {
        var intent = originalIntent
        for (intentFixer in INTENT_FIXERS) {
            val fixedIntent = intentFixer.fix(context, intent)
            if (context.packageManager.hasHandler(fixedIntent)) {
                intent = fixedIntent
            }
        }
        return intent
    }

    /**
     * Queries on-device packages for a handler for the supplied [intent].
     */
    private fun PackageManager.hasHandler(intent: Intent) = queryIntentActivities(intent, 0).isNotEmpty()

    internal interface Fixer {
        fun fix(context: Context, intent: Intent): Intent
    }

    private class AmazonFixer : Fixer {

        /**
         * If link contains amazon and it has ASIN in it, create a specific intent to open Amazon App.
         *
         * @param intent Original Intent with amazon link in it.
         * @return Specific Intent for Amazon app.
         */
        override fun fix(context: Context, intent: Intent): Intent {
            val dataString = intent.dataString
            if (dataString != null && dataString.contains("amazon")) {
                val asin = extractAmazonASIN(dataString)
                if (asin != null) {
                    return if ("0000000000" == asin) {
                        context.packageManager.getLaunchIntentForPackage(intent.component!!.packageName)!!
                    } else Intent(Intent.ACTION_VIEW).setDataAndType(
                        Uri.parse("mshop://featured?ASIN=$asin"),
                        "vnd.android.cursor.item/vnd.amazon.mShop.featured"
                    )
                }
            }
            return intent
        }
    }
}
