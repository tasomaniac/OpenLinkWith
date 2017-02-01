package com.tasomaniac.openwith.resolver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.util.Intents;

import net.simonvt.schematic.Cursors;

import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.COMPONENT;
import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.PREFERRED;
import static com.tasomaniac.openwith.data.OpenWithProvider.OpenWithHosts.withHost;

class PreferredResolver {

    private final PackageManager packageManager;
    private final ContentResolver contentResolver;
    @Nullable private final String callerPackage;

    private Uri uri;
    private boolean isPreferred;
    @Nullable private ComponentName lastChosenComponent;
    @Nullable private ResolveInfo ri;

    public static PreferredResolver createFrom(Activity activity) {
        return new PreferredResolver(activity.getPackageManager(), activity.getContentResolver(), ShareCompat.getCallingPackage(activity));
    }

    PreferredResolver(PackageManager packageManager, ContentResolver contentResolver, @Nullable String callerPackage) {
        this.packageManager = packageManager;
        this.contentResolver = contentResolver;
        this.callerPackage = callerPackage;
    }

    void startPreferred(ResolverActivity activity) {
        startPreferred(activity, preferredIntent(), loadLabel());
    }

    static void startPreferred(ResolverActivity activity, Intent intent, CharSequence appLabel) {
        String message = activity.getString(R.string.warning_open_link_with_name, appLabel);
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        Intents.startActivityFixingIntent(activity, intent);
        activity.finish();
    }

    void resolve(Uri uri) {
        this.uri = uri;
        Cursor query = queryIntentWith(uri.getHost());
        if (query == null) {
            return;
        }
        try {
            if (!query.moveToFirst()) {
                return;
            }
            isPreferred = Cursors.getInt(query, PREFERRED) == 1;
            lastChosenComponent = ComponentName.unflattenFromString(Cursors.getString(query, COMPONENT));
            ri = packageManager.resolveActivity(
                    new Intent().setComponent(lastChosenComponent),
                    PackageManager.MATCH_DEFAULT_ONLY
            );
        } finally {
            query.close();
        }
    }

    @Nullable
    private Cursor queryIntentWith(String host) {
        if (TextUtils.isEmpty(host)) {
            return null;
        }
        return contentResolver.query(withHost(host), null, null, null, null);
    }

    @Nullable
    ComponentName lastChosenComponent() {
        return lastChosenComponent;
    }

    Intent preferredIntent() {
        return new Intent(Intent.ACTION_VIEW, uri)
                .setComponent(lastChosenComponent);
    }

    @Nullable
    CharSequence loadLabel() {
        return ri == null ? null : ri.loadLabel(packageManager);
    }

    boolean shouldStartPreferred() {
        return isPreferred && !isCallerPackagePreferred();
    }

    private boolean isCallerPackagePreferred() {
        return ri != null && ri.activityInfo.packageName.equals(callerPackage);
    }
}
