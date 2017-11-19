package com.tasomaniac.openwith.redirect

import com.tasomaniac.openwith.rx.ImmediateScheduling
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

class RedirectFixerTest {

  @Rule @JvmField val server = MockWebServer()
  @Rule @JvmField val mockito = MockitoJUnit.rule()

  private val redirectFixer: RedirectFixer =
      RedirectFixer(OkHttpClient(), ImmediateScheduling(), 1)

  @Test
  fun givenNoRedirectShouldReturnOriginalUrl() {
    server.enqueue(noRedirect())

    test().assertValue(server.url("original"))
  }

  @Test
  fun givenRedirectShouldFollowRedirect() {
    server.enqueue(redirectTo(server.url("redirect")))
    server.enqueue(noRedirect())

    test().assertValue(server.url("redirect"))
  }

  @Test
  fun givenTwoRedirectsShouldReturnTheLastRedirect() {
    server.enqueue(redirectTo(server.url("redirect")))
    server.enqueue(redirectTo(server.url("redirect/2")))
    server.enqueue(noRedirect())

    test().assertValue(server.url("redirect/2"))
  }

  @Test
  fun givenNetworkErrorReturnOriginal() {
    server.enqueue(redirectTo(server.url("redirect")))
    server.shutdown()
    server.enqueue(noRedirect())

    test().assertValue(server.url("original"))
        .assertNoErrors()
  }

  @Test
  fun givenNetworkTimeoutReturnOriginal() {
    server.setDispatcher(object : Dispatcher() {
      override fun dispatch(request: RecordedRequest): MockResponse {
        Thread.sleep(1500)
        return redirectTo(server.url("redirect"))
      }
    })

    test().assertValue(server.url("original"))
        .assertNoErrors()
  }

  @Test
  fun givenWithinNetworkTimeoutLimitReturnRedirect() {
    server.setDispatcher(object : Dispatcher() {
      override fun dispatch(request: RecordedRequest): MockResponse {
        Thread.sleep(500)
        return redirectTo(server.url("redirect"))
      }
    })

    test().assertValue(server.url("redirect"))
        .assertNoErrors()
  }

  @Test
  fun givenNetworkIsInterruptedReturnOriginal() {
    server.setDispatcher(object : Dispatcher() {
      override fun dispatch(request: RecordedRequest): MockResponse {
        throw InterruptedException()
      }
    })

    test().assertValue(server.url("original"))
        .assertNoErrors()
  }

  private fun test(): TestObserver<HttpUrl> {
    return redirectFixer
        .followRedirects(server.url("original"))
        .test()
  }

  private fun noRedirect() = MockResponse()

  private fun redirectTo(redirect: HttpUrl) = MockResponse().addHeader("Location", redirect)
}
