package com.tasomaniac.openwith.resolver;

import android.support.annotation.Nullable;

import com.tasomaniac.openwith.rx.ImmediateScheduling;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class RedirectFixerTest {

    @Rule public MockWebServer server = new MockWebServer();
    @Rule public MockitoRule mockito = MockitoJUnit.rule();

    private RedirectFixer redirectFixer;

    @Before
    public void setUp() throws Exception {
        redirectFixer = new RedirectFixer(new OkHttpClient(), new ImmediateScheduling(), 1);
    }

    @Test
    public void givenNoRedirectShouldReturnOriginalUrl() throws InterruptedException {
        server.enqueue(noRedirect());

        redirectFixer.followRedirects(server.url("original"))
                .test().assertValue(server.url("original"));
    }

    @Test
    public void givenRedirectShouldFollowRedirect() throws InterruptedException {
        server.enqueue(redirectTo(server.url("redirect")));
        server.enqueue(noRedirect());

        redirectFixer.followRedirects(server.url("original"))
                .test().assertValue(server.url("redirect"));
    }

    @Test
    public void givenTwoRedirectsShouldReturnTheLastRedirect() throws InterruptedException {
        server.enqueue(redirectTo(server.url("redirect")));
        server.enqueue(redirectTo(server.url("redirect/2")));
        server.enqueue(noRedirect());

        redirectFixer.followRedirects(server.url("original"))
                .test().assertValue(server.url("redirect/2"));
    }

    @Test
    public void givenNetworkErrorReturnOriginal() throws Exception {
        server.enqueue(redirectTo(server.url("redirect")));
        server.shutdown();
        server.enqueue(noRedirect());

        redirectFixer.followRedirects(server.url("original"))
                .test().assertValue(server.url("original"));
    }

    @Test
    public void givenNetworkTimeoutReturnOriginal() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                Thread.sleep(1500);
                return redirectTo(server.url("redirect"));
            }
        });

        redirectFixer.followRedirects(server.url("original"))
                .test().assertValue(server.url("original"));
    }

    @Test
    public void givenWithinNetworkTimeoutLimitReturnRedirect() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                Thread.sleep(500);
                return redirectTo(server.url("redirect"));
            }
        });

        redirectFixer.followRedirects(server.url("original"))
                .test().assertValue(server.url("redirect"));
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
