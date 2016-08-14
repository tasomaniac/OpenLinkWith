package com.tasomaniac.openwith.util;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Urls {

    private static final Fixer[] URL_FIXERS = new Fixer[]{
            new FacebookFixer(),
            new TwitterFixer(),
            new EbayFixer(),
            new AmazonFixer(),
            new DailyMailFixer(),
    };

    public static String fixUrls(String url) {
        for (Fixer urlFixer : URL_FIXERS) {
            url = urlFixer.fix(url);
        }
        return url;
    }

    public static String extractUrlFrom(Intent intent, ShareCompat.IntentReader reader) {
        CharSequence text = reader.getText();
        if (text == null) {
            text = getExtraSelectedText(intent);
        }
        return findFirstUrl(text);
    }

    @SuppressLint("InlinedApi")
    private static CharSequence getExtraSelectedText(Intent intent) {
        return intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
    }

    @Nullable
    private static String findFirstUrl(@Nullable CharSequence text) {
        if (text == null) {
            return null;
        }
        final Matcher matcher = Pattern.compile("\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))", Pattern.CASE_INSENSITIVE)
                .matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private static class FacebookFixer implements Fixer {

        @Override
        public String fix(String url) {
            return  url
                    .replace("https://facebook.com/", "https://www.facebook.com/")
                    .replace("http://facebook.com/", "http://www.facebook.com/")
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

    static class AmazonFixer implements Fixer {
        @Override
        public String fix(String url) {
            String asin = extractAmazonASIN(url);

            //Use fake ASIN to make Amazon App popup for the Intent.
            final Matcher matcher = Pattern.compile("((?:http|https)://)?www\\.amazon\\.(?:com|co\\.uk|co\\.jp|de)/?")
                    .matcher(url);
            if (matcher.matches()) {
                asin = "0000000000";
            }

            if (asin != null) {
                url = "http://www.amazon.com/gp/aw/d/" + asin + "/aiv/detailpage/";
            }
            return url;
        }

        @Nullable
        static String extractAmazonASIN(String foundUrl) {
            try {
                final Matcher matcher = Pattern.compile(".*//www.amazon.(?:com|co\\.uk|co.jp|de)/gp/aw/d/(\\w{10})/.*", Pattern.CASE_INSENSITIVE)
                        .matcher(foundUrl);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            } catch (Exception ignored) {
            }
            try {
                //http://www.amazon.com/Garmin-Speed-Cadence-Bike-Sensor/dp/B000BFNOT8
                final Matcher matcher = Pattern.compile(".*//www.amazon.(?:com|co\\.uk|co.jp|de)/(?:.+/)?dp/(\\w{10}).*", Pattern.CASE_INSENSITIVE)
                        .matcher(foundUrl);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            } catch (Exception ignored) {
            }
            return null;
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

    interface Fixer {
        String fix(String url);
    }

    private Urls() {
    }
}
