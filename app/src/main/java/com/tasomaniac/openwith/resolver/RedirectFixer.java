package com.tasomaniac.openwith.resolver;

import android.support.annotation.Nullable;

import com.tasomaniac.openwith.rx.SchedulingStrategy;

import javax.inject.Inject;
import java.io.IOException;

import hugo.weaving.DebugLog;
import io.reactivex.Single;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

import static java.util.concurrent.TimeUnit.SECONDS;

class RedirectFixer {

    private final OkHttpClient client;
    private final SchedulingStrategy scheduling;

    private Call call;
    private HttpUrl lastUrl;

    @Inject
    RedirectFixer(OkHttpClient client, SchedulingStrategy scheduling) {
        this.client = client.newBuilder()
                .connectTimeout(1, SECONDS)
                .readTimeout(1, SECONDS)
                .writeTimeout(1, SECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
        this.scheduling = scheduling;
    }

    Single<HttpUrl> followRedirects(final HttpUrl url) {
        this.lastUrl = url;
        return Single
                .fromCallable(() -> doFollowRedirects(url))
                .timeout(2, SECONDS)
                .onErrorReturnItem(lastUrl)
                .doOnDispose(this::cancel)
                .doOnSuccess(httpUrl -> Timber.e(httpUrl.toString()))
                .compose(scheduling.applyToSingle());
    }

    @DebugLog
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
