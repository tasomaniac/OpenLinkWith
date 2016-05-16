package com.tasomaniac.openwith;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ShareCompat;
import android.widget.Toast;

import com.tasomaniac.openwith.util.Urls;

public abstract class BaseShareActivity extends Activity {

    String extractUrlFrom(ShareCompat.IntentReader reader) {
        CharSequence text = reader.getText();
        if (text == null) {
            text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        }
        return Urls.findFirstUrl(text);
    }

    void showErrorInvalidUrl() {
        Toast.makeText(this, R.string.error_invalid_url, Toast.LENGTH_SHORT).show();
    }
}
