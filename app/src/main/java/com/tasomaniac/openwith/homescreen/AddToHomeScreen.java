package com.tasomaniac.openwith.homescreen;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.widget.Toast;

import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.data.Injector;
import com.tasomaniac.openwith.resolver.ResolverActivity;
import com.tasomaniac.openwith.util.Urls;

import static com.tasomaniac.openwith.util.Urls.fixUrls;

public class AddToHomeScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.obtain(this).analytics().sendScreenView("AddToHomeScreen");

        final ShareCompat.IntentReader reader = ShareCompat.IntentReader.from(this);
        String foundUrl = Urls.extractUrlFrom(getIntent(), reader);

        if (foundUrl != null) {
            Intent intentToHandle = new Intent(Intent.ACTION_VIEW, Uri.parse(fixUrls(foundUrl)))
                    .putExtra(ResolverActivity.EXTRA_ADD_TO_HOME_SCREEN, true);
            startActivity(intentToHandle.setClass(this, ResolverActivity.class));
        } else {
            Toast.makeText(this, R.string.error_invalid_url, Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
