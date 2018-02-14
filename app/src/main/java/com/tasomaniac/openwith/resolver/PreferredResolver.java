package com.tasomaniac.openwith.resolver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.text.TextUtils;
import com.tasomaniac.openwith.data.PreferredAppDao;
import com.tasomaniac.openwith.rx.SchedulingStrategy;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

class PreferredResolver {

    private final PackageManager packageManager;
    private final PreferredAppDao appDao;
    private final SchedulingStrategy scheduling;
    @Nullable
    private final String callerPackage;

    private Uri uri;
    private boolean isPreferred;
    @Nullable
    private ComponentName lastChosenComponent;
    @Nullable
    private ResolveInfo ri;

    public static PreferredResolver createFrom(Activity activity, PreferredAppDao appDao, SchedulingStrategy scheduling) {
        return new PreferredResolver(activity.getPackageManager(), appDao, scheduling, ShareCompat.getCallingPackage(activity));
    }

    PreferredResolver(
            PackageManager packageManager,
            PreferredAppDao appDao,
            SchedulingStrategy scheduling,
            @Nullable String callerPackage
    ) {
        this.packageManager = packageManager;
        this.appDao = appDao;
        this.scheduling = scheduling;
        this.callerPackage = callerPackage;
    }

    /**
     * Starts the preferred Activity if necessary.
     * It is started when preferred Activity is available for this host and it is not the same as caller.
     * <p>
     * Can throw SecurityException because we store the component name by String.
     * With app updates, public Activities may become private.
     *
     * @return true if the Activity start is successful.
     */
    boolean startPreferred(ResolverActivity activity) {
        if (shouldStartPreferred()) {
            try {
                new ResolverNavigation(activity).startPreferred(preferredIntent(), loadLabel());
                return true;
            } catch (Exception e) {
                Timber.e(e, "Security Exception for the url %s", uri);

                Completable.fromAction(() -> appDao.deleteHost(uri.getHost()))
                        .compose(scheduling.forCompletable())
                        .subscribe();
            }
        }
        return false;
    }

    void resolve(Uri uri) {
        this.uri = uri;
        String host = uri.getHost();
        if (TextUtils.isEmpty(host)) {
            return;
        }
        Disposable disposable = appDao.preferredAppByHost(host)
                .compose(scheduling.forMaybe())
                .subscribe(preferredApp -> {
                    isPreferred = preferredApp.getPreferred();
                    lastChosenComponent = ComponentName.unflattenFromString(preferredApp.getComponent());
                    ri = packageManager.resolveActivity(
                            new Intent().setComponent(lastChosenComponent),
                            PackageManager.MATCH_DEFAULT_ONLY
                    );
                });
    }

    @Nullable
    ComponentName lastChosenComponent() {
        return lastChosenComponent;
    }

    private Intent preferredIntent() {
        return new Intent(Intent.ACTION_VIEW, uri)
                .setComponent(lastChosenComponent);
    }

    @Nullable
    private CharSequence loadLabel() {
        return ri == null ? null : ri.loadLabel(packageManager);
    }

    private boolean shouldStartPreferred() {
        return isPreferred && !isCallerPackagePreferred();
    }

    private boolean isCallerPackagePreferred() {
        return ri != null && ri.activityInfo.packageName.equals(callerPackage);
    }
}
