package com.tasomaniac.openwith.resolver;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.util.Intents;
import timber.log.Timber;

import javax.inject.Inject;

class ResolverNavigation implements ResolverView.Navigation {

    private final Activity activity;

    @Inject ResolverNavigation(ResolverActivity activity) {
        this.activity = activity;
    }

    @Override
    public void startSelected(Intent intent) {
        if (activity.isFinishing()) {
            return;
        }
        try {
            Intents.startActivityFixingIntent(activity, intent);
            dismiss();
        } catch (Exception e) {
            Timber.e(e);
            Toast.makeText(activity, R.string.error_cannot_start_activity, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void startPreferred(Intent intent, CharSequence appLabel) {
        String message = activity.getString(R.string.warning_open_link_with_name, appLabel);
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        Intents.startActivityFixingIntent(activity, intent);
    }

    @Override
    public void dismiss() {
        if (!activity.isFinishing()) {
            activity.finish();
        }
    }
}
