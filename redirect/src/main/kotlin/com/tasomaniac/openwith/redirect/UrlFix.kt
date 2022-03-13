package com.tasomaniac.openwith.redirect

import com.tasomaniac.openwith.extensions.extractAmazonASIN
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.regex.Pattern
import javax.inject.Inject

class UrlFix @Inject constructor(
    private val cleanUrlsPreferences: CleanUrlsPreferences
) {

    fun fixUrls(originalUrl: String): String {
        var url = originalUrl
        for (urlFixer in URL_FIXERS) {
            url = urlFixer.fix(url)
        }
        return if (cleanUrlsPreferences.isEnabled) url.cleanUpTrackingParams() else url
    }

    private fun String.cleanUpTrackingParams(): String {
        val httpUrl = toHttpUrlOrNull() ?: return this
        val urlBuilder = httpUrl.newBuilder()
        httpUrl.queryParameterNames
            .filter { cleanUrlsPreferences.cleanUpRegex.containsMatchIn(it) }
            .forEach { urlBuilder.removeAllQueryParameters(it) }
        return urlBuilder.build().toString()
    }

    private interface Fixer {
        fun fix(url: String): String
    }

    @Suppress("ComplexCondition")
    private class FacebookFixer : Fixer {
        override fun fix(url: String): String {
            if (!url.contains("facebook.com")) {
                return url
            }

            // Skip the links that Facebook supports
            return if (url.contains("facebook.com/permalink.php") ||
                url.contains("facebook.com/story.php") ||
                url.contains("facebook.com/home.php") ||
                url.contains("facebook.com/photo.php") ||
                url.contains("facebook.com/video.php") ||
                url.contains("facebook.com/donate") ||
                url.contains("facebook.com/events") ||
                url.contains("facebook.com/groups") ||
                url.contains("/posts/") ||
                url.contains("/dialog/") ||
                url.contains("/sharer")
            ) {
                url
            } else {
                url.replace("https://facebook.com/", "https://www.facebook.com/")
                    .replace("http://facebook.com/", "http://www.facebook.com/")
                    .replace("?", "&")
                    .replace("facebook.com/", "facebook.com/n/?")
            }
        }
    }

    private class TwitterFixer : Fixer {
        override fun fix(url: String): String {
            return url.replace("//mobile.twitter.com", "//twitter.com")
        }
    }

    private class EbayFixer : Fixer {
        override fun fix(url: String): String {
            val ebayItemId = extractEbayItemId(url)
            return if (ebayItemId != null) {
                "http://pages.ebay.com/link/?nav=item.view&id=$ebayItemId"
            } else url
        }

        companion object {
            @Suppress("MaxLineLength")
            private const val EBAY_PATTERN =
                "(?:(?:http|https)://)?(?:www|m).ebay.(?:com|co\\.uk|com.hk|com.au|at|ca|fr|de|ie|it|com\\.my|nl|ph|pl|com\\.sg|es|ch)/itm/(?:.*/)?(\\d+)(?:\\?.*)?"

            private fun extractEbayItemId(foundUrl: String): String? {
                try {
                    val matcher = Pattern.compile(EBAY_PATTERN).matcher(foundUrl)
                    if (matcher.find()) {
                        return matcher.group(1)
                    }
                } catch (ignored: Exception) {
                }
                return null
            }
        }
    }

    class AmazonFixer : Fixer {

        override fun fix(url: String): String {
            var asin = extractAmazonASIN(url)

            // Use fake ASIN to make Amazon App popup for the Intent.
            val matcher = Pattern.compile(AMAZON_PATTERN).matcher(url)
            if (matcher.matches()) {
                asin = "0000000000"
            }
            if (asin != null) {
                return "http://www.amazon.com/gp/aw/d/$asin/aiv/detailpage/"
            }
            return url
        }

        companion object {
            @Suppress("MaxLineLength")
            private const val AMAZON_PATTERN =
                "((?:http|https)://)?www\\.amazon\\.(?:com|co\\.uk|co\\.jp|com\\.au|com\\.br|ca|cn|fr|de|in|it|com\\.mx|nl|es)/?"
        }
    }

    private class DailyMailFixer : Fixer {
        override fun fix(url: String): String {
            val articleId = extractDailyMailArticleId(url)
            if (articleId != null) {
                return "dailymail://article/$articleId"
            }
            return url
        }

        companion object {
            @Suppress("MaxLineLength")
            private const val DAILY_MAIL_PATTERN =
                "(?:(?:http|https)://)?(?:www|m).dailymail.co.uk/.*/article-(\\d*)?/.*"

            private fun extractDailyMailArticleId(foundUrl: String): String? {
                try {
                    val matcher = Pattern.compile(DAILY_MAIL_PATTERN).matcher(foundUrl)
                    if (matcher.find()) {
                        return matcher.group(1)
                    }
                } catch (ignored: Exception) {
                }
                return null
            }
        }
    }

    private class VkFixer : Fixer {
        override fun fix(url: String): String {
            return url.replace("//m.vk.com", "//vk.com")
        }
    }

    companion object {
        private val URL_FIXERS = setOf(
            FacebookFixer(),
            TwitterFixer(),
            EbayFixer(),
            AmazonFixer(),
            DailyMailFixer(),
            VkFixer()
        )
    }
}
