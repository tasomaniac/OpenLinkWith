package com.tasomaniac.openwith.redirect;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.tasomaniac.openwith.util.Intents;
import com.tasomaniac.openwith.util.ResolverInfos;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;

class BrowserIntentChecker {

    private final PackageManager packageManager;

    @Inject
    BrowserIntentChecker(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    boolean hasOnlyBrowsers(Intent sourceIntent) {
        if (!Intents.isHttp(sourceIntent)) {
            return false;
        }
        int flag = SDK_INT >= M ? PackageManager.MATCH_ALL : PackageManager.MATCH_DEFAULT_ONLY;
        Set<ComponentName> resolved = toComponents(packageManager.queryIntentActivities(sourceIntent, flag));
        Set<ComponentName> browsers = toComponents(queryBrowsers());

        resolved.removeAll(browsers);
        return resolved.isEmpty();
    }

    private Set<ComponentName> toComponents(List<ResolveInfo> list) {
        Set<ComponentName> components = new HashSet<>();
        for (ResolveInfo ri : list) {
            components.add(ResolverInfos.componentName(ri));
        }
        return components;
    }

    private List<ResolveInfo> queryBrowsers() {
        Intent browserIntent = new Intent()
                .setAction(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.parse("http:"));
        return packageManager.queryIntentActivities(browserIntent, 0);
    }
}
