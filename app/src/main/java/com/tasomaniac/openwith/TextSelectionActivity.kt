package com.tasomaniac.openwith

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle

class TextSelectionActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(intent)
        intent.component = ComponentName(this, ShareToOpenWith::class.java)
        startActivity(intent)
        finish()
    }
}
