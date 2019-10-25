package com.tasomaniac.openwith.homescreen

import android.annotation.TargetApi
import android.os.Build.VERSION_CODES.M
import com.tasomaniac.openwith.PerActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.IOException
import java.util.regex.Pattern
import javax.inject.Inject

@TargetApi(M)
@PerActivity
class TitleFetcher @Inject constructor(private val client: OkHttpClient) {

    private var call: Call? = null

    fun cancel() {
        call?.cancel()
    }

    fun fetch(url: String, onSuccess: (title: String?) -> Unit, onFailure: () -> Unit) {
        call?.cancel()
        val httpUrl = url.toHttpUrlOrNull() ?: return
        call = client.newCall(request(httpUrl)).apply {
            enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) = onFailure()

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        Timber.tag("Network").e("Fail with response: %s", response)
                        onFailure()
                    } else {
                        response.body!!.use { body -> onSuccess(body.extractTitle()) }
                    }
                }
            })
        }
    }

    private fun request(httpUrl: HttpUrl) =
        Request.Builder()
            .url(httpUrl)
            .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:13.0) Gecko/13.0 Firefox/13.0")
            .build()

    @Suppress("NestedBlockDepth")
    private fun ResponseBody.extractTitle(): String? {
        val source = source()
        val pattern = Pattern.compile(TITLE_PATTERN)

        var line = source.readUtf8Line()
        while (line != null) {
            val matcher = pattern.matcher(line)
            if (!matcher.find()) {
                continue
            }

            for (i in 1..matcher.groupCount()) {
                val match = matcher.group(i)
                if (match != null) return match
            }
            line = source.readUtf8Line()
        }
        return null
    }

    companion object {
        @Suppress("MaxLineLength")
        private const val TITLE_PATTERN =
            "<title(?:\\s.*)?>(.+)</title>|<meta\\s*property=\"og:title\"\\s*content=\"(.*)\".*>|<meta\\s*content=\"(.*)\"\\s*property=\"og:title\".*>"
    }
}
