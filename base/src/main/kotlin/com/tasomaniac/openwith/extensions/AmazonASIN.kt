package com.tasomaniac.openwith.extensions

import java.util.regex.Pattern

fun extractAmazonASIN(foundUrl: String): String? {
    try {
        // https://www.amazon.de/gp/product/B01LYYM9I3
        // https://www.amazon.com/gp/aw/d/B001GNBJQO?vs=1
        // http://www.amazon.com/Garmin-Speed-Cadence-Bike-Sensor/dp/B000BFNOT8
        val matcher = Pattern.compile(
            ".*//www.amazon.(?:com|co\\.uk|co.jp|com\\.au|com\\.br|ca|cn|fr|de|in|it|com\\.mx|nl|es)/(?:.+/)?(?:gp/aw/d|gp/product|dp)+/(\\w{10}).*",
            Pattern.CASE_INSENSITIVE
        )
            .matcher(foundUrl)
        if (matcher.find()) {
            return matcher.group(1)
        }
    } catch (ignored: Exception) {
    }

    return null
}
