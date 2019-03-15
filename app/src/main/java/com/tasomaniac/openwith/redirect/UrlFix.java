package com.tasomaniac.openwith.redirect;

import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tasomaniac.openwith.extensions.AmazonASINKt.extractAmazonASIN;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

public class UrlFix {
    private static final Set<Fixer> URL_FIXERS = unmodifiableSet(new HashSet<>(asList(
            new FacebookFixer(),
            new TwitterFixer(),
            new EbayFixer(),
            new AmazonFixer(),
            new DailyMailFixer(),
            new VkFixer(),
            new WashingtonPostFixer()
    )));

    public static String fixUrls(String url) {
        for (Fixer urlFixer : URL_FIXERS) {
            url = urlFixer.fix(url);
        }
        return url;
    }

    interface Fixer {
        String fix(String url);
    }

    private static class FacebookFixer implements Fixer {

        @Override
        public String fix(String url) {
            if (!url.contains("facebook.com")) {
                return url;
            }

            // Skip the links that Facebook supports
            if (url.contains("facebook.com/permalink.php")
                    || url.contains("facebook.com/story.php")
                    || url.contains("facebook.com/home.php")
                    || url.contains("facebook.com/photo.php")
                    || url.contains("facebook.com/video.php")
                    || url.contains("facebook.com/donate")
                    || url.contains("facebook.com/events")
                    || url.contains("facebook.com/groups")
                    || url.contains("/posts/")
                    || url.contains("/dialog/")
                    || url.contains("/sharer")) {
                return url;
            }

            return url.replace("https://facebook.com/", "https://www.facebook.com/")
                    .replace("http://facebook.com/", "http://www.facebook.com/")
                    .replace("?", "&")
                    .replace("facebook.com/", "facebook.com/n/?");
        }
    }

    private static class TwitterFixer implements Fixer {
        @Override
        public String fix(String url) {
            return url.replace("//mobile.twitter.com", "//twitter.com");
        }
    }

    private static class EbayFixer implements Fixer {
        @Override
        public String fix(String url) {
            final String ebayItemId = extractEbayItemId(url);
            if (ebayItemId != null) {
                return "http://pages.ebay.com/link/?nav=item.view&id=" + ebayItemId;
            }
            return url;
        }

        @Nullable
        private static String extractEbayItemId(String foundUrl) {
            try {
                final Matcher matcher = Pattern.compile("(?:(?:http|https)://)?(?:www|m).ebay.(?:com|co\\.uk|com.hk|com.au|at|ca|fr|de|ie|it|com\\.my|nl|ph|pl|com\\.sg|es|ch)/itm/(?:.*/)?(\\d+)(?:\\?.*)?")
                        .matcher(foundUrl);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            } catch (Exception ignored) {
            }
            return null;
        }
    }

    public static class AmazonFixer implements Fixer {
        @Override
        public String fix(String url) {
            String asin = extractAmazonASIN(url);

            //Use fake ASIN to make Amazon App popup for the Intent.
            final Matcher matcher = Pattern.compile("((?:http|https)://)?www\\.amazon\\.(?:com|co\\.uk|co\\.jp|com\\.au|com\\.br|ca|cn|fr|de|in|it|com\\.mx|nl|es)/?")
                    .matcher(url);
            if (matcher.matches()) {
                asin = "0000000000";
            }

            if (asin != null) {
                url = "http://www.amazon.com/gp/aw/d/" + asin + "/aiv/detailpage/";
            }
            return url;
        }

    }

    private static class DailyMailFixer implements Fixer {
        @Override
        public String fix(String url) {
            String articleId = extractDailyMailArticleId(url);
            if (articleId != null) {
                url = "dailymail://article/" + articleId;
            }
            return url;
        }

        @Nullable
        private static String extractDailyMailArticleId(String foundUrl) {
            try {
                final Matcher matcher = Pattern.compile("(?:(?:http|https)://)?(?:www|m).dailymail.co.uk/.*/article-(\\d*)?/.*")
                        .matcher(foundUrl);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            } catch (Exception ignored) {
            }
            return null;
        }
    }

    private static class VkFixer implements Fixer {
        @Override
        public String fix(String url) {
            return url.replace("//m.vk.com", "//vk.com");
        }
    }

    private static class WashingtonPostFixer implements Fixer {
        @Override public String fix(String url) {
            return url
                    .replace("http://www.washingtonpost.com", "wp-android://www.washingtonpost.com")
                    .replace("https://www.washingtonpost.com", "wp-android://www.washingtonpost.com");
        }
    }
}
