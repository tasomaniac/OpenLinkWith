package com.tasomaniac.openwith.redirect

import com.tasomaniac.openwith.test.testScheduling
import io.reactivex.observers.TestObserver
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Rule
import org.junit.Test
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

class RedirectFixerTest {

    @Rule @JvmField val server = MockWebServer()
    @Rule @JvmField val mockito: MockitoRule = MockitoJUnit.rule()

    private val redirectFixer: RedirectFixer =
        RedirectFixer(OkHttpClient(), testScheduling(), 1)

    @Test
    fun givenNoRedirectShouldReturnOriginalUrl() {
        given {
            enqueue(noRedirect())
        }.then {
            assertUrlWithPath("original")
        }
    }

    @Test
    fun givenRedirectShouldFollowRedirect() {
        given {
            enqueue(redirectTo("redirect"))
            enqueue(noRedirect())
        }.then {
            assertUrlWithPath("redirect")
        }
    }

    @Test
    fun givenTwoRedirectsShouldReturnTheLastRedirect() {
        given {
            enqueue(redirectTo("redirect"))
            enqueue(redirectTo("redirect/2"))
            enqueue(noRedirect())
        }.then {
            assertUrlWithPath("redirect/2")
        }
    }

    @Test
    fun givenNetworkErrorReturnOriginal() {
        given {
            enqueue(redirectTo("redirect"))
            shutdown()
            enqueue(noRedirect())
        }.then {
            assertUrlWithPath("original")
                .assertNoErrors()
        }
    }

    @Test
    fun givenNetworkTimeoutReturnOriginal() {
        given {
            setDispatcher(object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    Thread.sleep(1500)
                    return redirectTo("redirect")
                }
            })
        }.then {
            assertUrlWithPath("original")
                .assertNoErrors()
        }
    }

    @Test
    fun givenWithinNetworkTimeoutLimitReturnRedirect() {
        given {
            setDispatcher(object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    Thread.sleep(500)
                    return redirectTo("redirect")
                }
            })
        }.then {
            assertUrlWithPath("redirect")
                .assertNoErrors()
        }
    }

    @Test
    fun givenNetworkIsInterruptedReturnOriginal() {
        given {
            setDispatcher(object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    throw InterruptedException()
                }
            })
        }.then {
            assertUrlWithPath("original")
                .assertNoErrors()
        }
    }

    private fun TestObserver<HttpUrl>.assertUrlWithPath(path: String) = apply { assertValue(server.url(path)) }

    private fun noRedirect() = MockResponse()

    private fun redirectTo(path: String) = MockResponse().addHeader("Location", server.url(path))

    private infix fun given(given: MockWebServer.() -> Unit): Then {
        server.given()
        return Then()
    }

    inner class Then {

        infix fun then(assert: TestObserver<HttpUrl>.() -> Unit) {
            test().assert()
        }

        private fun test(): TestObserver<HttpUrl> {
            return redirectFixer
                .followRedirects(server.url("original"))
                .test()
        }
    }
}
