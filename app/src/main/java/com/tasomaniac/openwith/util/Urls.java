package com.tasomaniac.openwith.util;

import android.support.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Urls {

    private static final Fixer[] URL_FIXERS = new Fixer[]{
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

    @Nullable
    public static String findFirstUrl(@Nullable CharSequence text) {
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

    private static class TwitterFixer implements Fixer {
        @Override
        public String fix(String url) {
            return url.replace("//mobile.twitter.com", "//twitter.com");
        }
    }

    private static class EbayFixer implements Fixer {
        @Override
        public String fix(String url) {
            final String ebayItemId = Extensions.extractEbayItemId(url);
            if (ebayItemId != null) {
                return "http://pages.ebay.com/link/?nav=item.view&id=" + ebayItemId;
            }
            return url;
        }
    }

    private static class AmazonFixer implements Fixer {
        @Override
        public String fix(String url) {
            String asin = Extensions.extractAmazonASIN(url);

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
    }

    private static class DailyMailFixer implements Fixer {
        @Override
        public String fix(String url) {
            String articleId = Extensions.extractDailyMailArticleId(url);
            if (articleId != null) {
                url = "dailymail://article/" + articleId;
            }
            return url;
        }
    }

    interface Fixer {
        String fix(String url);
    }
}
