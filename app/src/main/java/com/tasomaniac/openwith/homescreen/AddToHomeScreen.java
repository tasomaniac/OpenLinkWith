package com.tasomaniac.openwith.homescreen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.widget.Toast;

import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.data.Analytics;
import com.tasomaniac.openwith.redirect.RedirectFixActivity;
import com.tasomaniac.openwith.resolver.ResolverActivity;
import com.tasomaniac.openwith.util.Urls;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

public class AddToHomeScreen extends DaggerAppCompatActivity {

    @Inject Analytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analytics.sendScreenView(this, "AddToHomeScreen");

        final ShareCompat.IntentReader reader = ShareCompat.IntentReader.from(this);
        String foundUrl = Urls.extractUrlFrom(getIntent(), reader);

        if (foundUrl != null) {
            Intent intent = RedirectFixActivity.createIntent(this, foundUrl)
                    .putExtra(ResolverActivity.EXTRA_ADD_TO_HOME_SCREEN, true);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.error_invalid_url, Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
