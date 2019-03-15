package com.tasomaniac.openwith.homescreen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.core.app.ShareCompat;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.data.Analytics;
import com.tasomaniac.openwith.redirect.RedirectFixActivity;
import com.tasomaniac.openwith.resolver.DisplayActivityInfo;
import com.tasomaniac.openwith.resolver.ResolverActivity;
import com.tasomaniac.openwith.util.Urls;
import dagger.android.support.DaggerAppCompatActivity;

import javax.annotation.Nullable;
import javax.inject.Inject;

public class AddToHomeScreen extends DaggerAppCompatActivity {

    private static final int REQUEST_CODE = 100;

    @Inject Analytics analytics;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analytics.sendScreenView("AddToHomeScreen");

        final ShareCompat.IntentReader reader = ShareCompat.IntentReader.from(this);
        String foundUrl = Urls.extractUrlFrom(getIntent(), reader);

        if (foundUrl != null) {
            Intent intent = RedirectFixActivity.createIntent(this, foundUrl)
                    .putExtra(ResolverActivity.EXTRA_ADD_TO_HOME_SCREEN, true);
            startActivityForResult(intent, REQUEST_CODE);
        } else {
            Toast.makeText(this, R.string.error_invalid_url, Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE || data == null) {
            finish();
            return;
        }

        DisplayActivityInfo activityInfo = data.getParcelableExtra(ResolverActivity.RESULT_EXTRA_INFO);
        Intent intent = data.getParcelableExtra(ResolverActivity.RESULT_EXTRA_INTENT);

        AddToHomeScreenDialogFragment
                .newInstance(activityInfo, intent)
                .show(getSupportFragmentManager());
    }
}
