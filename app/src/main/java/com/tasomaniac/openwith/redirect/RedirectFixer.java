package com.tasomaniac.openwith.redirect;

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

public class RedirectFixer {

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

    Single<HttpUrl> followRedirects(HttpUrl url) {
        this.lastUrl = url;
        return Single
                .fromCallable(() -> doFollowRedirects(url))
                .timeout(timeoutInSec, SECONDS)
                .doOnError(throwable -> cancel())
                .onErrorReturn(throwable -> lastUrl)
                .doOnDispose(this::cancel)
                .compose(scheduling.forSingle());
    }

    private HttpUrl doFollowRedirects(HttpUrl url) {
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
    private String fetchLocationHeader(HttpUrl url) {
        call = client.newCall(request(url));
        try (Response response = call.execute()) {
            return response.header("Location");
        } catch (IOException e) {
            return url.toString();
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
