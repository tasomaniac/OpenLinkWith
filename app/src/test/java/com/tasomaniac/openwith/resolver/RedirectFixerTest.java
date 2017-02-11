package com.tasomaniac.openwith.resolver;

import android.support.annotation.Nullable;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.reactivex.Single;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class RedirectFixerTest {

    @Rule public MockWebServer server = new MockWebServer();
    @Rule public MockitoRule mockito = MockitoJUnit.rule();

    private RedirectFixer redirectFixer;

    @Before
    public void setUp() throws Exception {
        redirectFixer = new RedirectFixer(new OkHttpClient(), new ImmediateScheduling());
    }

    @Test
    public void givenNoRedirectShouldReturnOriginalUrl() {
        server.enqueue(noRedirect());

        Single<HttpUrl> single = redirectFixer.followRedirects(server.url("original"));

        single.test().assertValue(server.url("original"));
    }

    @Test
    public void givenRedirectShouldFollowRedirect() {
        server.enqueue(redirectTo(server.url("redirect")));
        server.enqueue(noRedirect());

        Single<HttpUrl> single = redirectFixer.followRedirects(server.url("original"));

        single.test().assertValue(server.url("redirect"));
    }

    @Test
    public void givenTwoRedirectsShouldReturnTheLastRedirect() {
        server.enqueue(redirectTo(server.url("redirect")));
        server.enqueue(redirectTo(server.url("redirect/2")));
        server.enqueue(noRedirect());

        Single<HttpUrl> single = redirectFixer.followRedirects(server.url("original"));

        single.test().assertValue(server.url("redirect/2"));
    }

    @Test
    public void givenNetworkErrorReturnOriginal() throws IOException {
        server.enqueue(redirectTo(server.url("redirect")));
        server.shutdown();
        server.enqueue(noRedirect());

        Single<HttpUrl> single = redirectFixer.followRedirects(server.url("original"));

        single.test().assertValue(server.url("original"));
    }

    private MockResponse noRedirect() {
        return redirectTo(null);
    }

    private MockResponse redirectTo(@Nullable HttpUrl redirect) {
        if (redirect == null) {
            return new MockResponse();
        } else {
            return new MockResponse().addHeader("Location", redirect);
        }
    }
}
