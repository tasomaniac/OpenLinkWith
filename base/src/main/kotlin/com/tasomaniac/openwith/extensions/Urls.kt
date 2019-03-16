package com.tasomaniac.openwith.extensions

import android.annotation.SuppressLint
import android.content.Intent
import androidx.core.app.ShareCompat
import java.util.regex.Pattern

private val URL_PATTERN = Pattern.compile(
    "\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))",
    Pattern.CASE_INSENSITIVE
)

fun Intent.extractUrlFrom(reader: ShareCompat.IntentReader): String? {
    var text: CharSequence? = dataString
    if (text == null) {
        text = reader.text
    }
    if (text == null) {
        text = getExtraSelectedText(this)
    }
    val firstUrl = text.findFirstUrl()
    return fixHttpPrefix(firstUrl)
}

fun CharSequence?.findFirstUrl(): String? {
    if (this == null) {
        return null
    }
    val matcher = URL_PATTERN.matcher(this)
    if (!matcher.find()) {
        return null
    }
    val url = matcher.group()
    return if (url.startsWith("content://") || url.startsWith("file://")) {
        null
    } else {
        url
    }
}

@SuppressLint("InlinedApi")
private fun getExtraSelectedText(intent: Intent): CharSequence {
    return intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
}

private fun fixHttpPrefix(url: String?): String? {
    if (url == null) {
        return null
    }
    return if (url.startsWith("http")) {
        url
    } else {
        "https://$url"
    }
}
