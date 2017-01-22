package com.tasomaniac.openwith.homescreen;

import android.annotation.TargetApi;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import timber.log.Timber;

import static android.os.Build.VERSION_CODES.M;

@TargetApi(M)
class TitleFetcher {

    private final OkHttpClient client;

    private Listener listener = Listener.EMPTY;
    private Call call;

    TitleFetcher(OkHttpClient client) {
        this.client = client;
    }

    void setListener(@Nullable Listener listener) {
        if (listener == null) {
            this.listener = Listener.EMPTY;
            if (call != null) {
                call.cancel();
            }
        } else {
            this.listener = listener;
        }
    }

    void fetch(String url) {
        if (call != null) {
            call.cancel();
        }
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            return;
        }
        call = client.newCall(new Request.Builder()
                                      .url(httpUrl)
                                      .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:13.0) Gecko/13.0 Firefox/13.0")
                                      .build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFailure();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Timber.tag("Network")
                            .e("Fail with response: %s", response);
                    listener.onSuccess(null);
                    return;
                }

                try (ResponseBody body = response.body()) {
                    final String title = extractTitle(body);
                    listener.onSuccess(title);
                }
            }
        });
    }

    private static String extractTitle(ResponseBody body) throws IOException {
        BufferedSource source = body.source();

        Pattern pattern = Pattern.compile("(?:<title(?:\\s.*)?>(.+)</title>|<meta\\s.*property=\"og:title\"\\s.*content=\"(.*)\".*>|<meta\\s.*content=\"(.*)\"\\s.*property=\"og:title\".*>)");

        String line;
        //noinspection MethodCallInLoopCondition
        while ((line = source.readUtf8Line()) != null) {
            Matcher matcher = pattern.matcher(line);
            if (!matcher.find()) {
                continue;
            }
            for (int i = 1, size = matcher.groupCount(); i <= size; i++) {
                String match = matcher.group(i);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    interface Listener {

        void onFailure();

        void onSuccess(String title);

        Listener EMPTY = new Listener() {
            @Override
            public void onFailure() {
                // no-op
            }

            @Override
            public void onSuccess(String title) {
                // no-op
            }
        };
    }
}
