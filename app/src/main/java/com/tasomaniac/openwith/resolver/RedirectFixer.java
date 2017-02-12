package com.tasomaniac.openwith.resolver;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.tasomaniac.openwith.rx.SchedulingStrategy;

import javax.inject.Inject;
import java.io.IOException;

import io.reactivex.Single;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.util.concurrent.TimeUnit.SECONDS;

class RedirectFixer {

    private final OkHttpClient client;
    private final SchedulingStrategy scheduling;
    private final int timeoutInSec;

    private Call call;
    private volatile HttpUrl lastUrl;

    @Inject
    RedirectFixer(OkHttpClient client, SchedulingStrategy scheduling) {
        this(client, scheduling, 5);
    }

    RedirectFixer(OkHttpClient client, SchedulingStrategy scheduling, int timeoutInSec) {
        this.client = client.newBuilder()
                .connectTimeout(2, SECONDS)
                .readTimeout(2, SECONDS)
                .writeTimeout(2, SECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
        this.scheduling = scheduling;
        this.timeoutInSec = timeoutInSec;
    }

    Single<Intent> followRedirects(Intent intent) {
        return followRedirects(HttpUrl.parse(intent.getDataString()))
                .map(httpUrl -> intent.setData(Uri.parse(httpUrl.toString())));
    }

    Single<HttpUrl> followRedirects(final HttpUrl url) {
        this.lastUrl = url;
        return Single
                .fromCallable(() -> doFollowRedirects(url))
                .timeout(timeoutInSec, SECONDS)
                .doOnError(throwable -> cancel())
                .onErrorReturn(throwable -> lastUrl)
                .doOnDispose(this::cancel)
                .compose(scheduling.applyToSingle());
    }

    private HttpUrl doFollowRedirects(HttpUrl url) throws IOException {
        String locationHeader = fetchLocationHeader(url);

        HttpUrl redirectUrl = locationHeader == null ? null : HttpUrl.parse(locationHeader);
        if (redirectUrl == null) {
            return url;
        } else {
            lastUrl = redirectUrl;
            return doFollowRedirects(redirectUrl);
        }
    }

    @Nullable
    private String fetchLocationHeader(HttpUrl url) throws IOException {
        call = client.newCall(request(url));
        Response response = call.execute();
        try {
            return response.header("Location");
        } finally {
            response.close();
        }
    }

    private Request request(HttpUrl httpUrl) {
        return new Request.Builder()
                .url(httpUrl)
                .build();
    }

    public void cancel() {
        if (call != null) {
            call.cancel();
        }
    }

}
