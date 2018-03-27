package com.tasomaniac.openwith.data

import timber.log.Timber

interface Analytics {

    fun sendScreenView(screenName: String)

    fun sendEvent(category: String, action: String, label: String)

    class DebugAnalytics : Analytics {

        override fun sendScreenView(screenName: String) {
            Timber.tag("Analytics").d("Screen: %s", screenName)
        }

        override fun sendEvent(category: String, action: String, label: String) {
            Timber.tag("Analytics").d(
                """\
                    |Event recorded:
                    |    Category: $category
                    |    Action: $action
                    |    Label: $label
                """.trimMargin()
            )
        }
    }
}
