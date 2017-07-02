package com.tasomaniac.openwith.redirect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.tasomaniac.android.widget.DelayedProgressBar;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.resolver.ResolverActivity;
import com.tasomaniac.openwith.rx.SchedulingStrategy;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.android.support.DaggerAppCompatActivity;
import io.reactivex.Single;

import static com.tasomaniac.openwith.util.Urls.fixUrls;

public class RedirectFixActivity extends DaggerAppCompatActivity {

    @Inject BrowserIntentChecker browserIntentChecker;
    @Inject RedirectFixer redirectFixer;
    @Inject SchedulingStrategy schedulingStrategy;

    public static Intent createIntent(Context context, String foundUrl) {
        return new Intent(context, RedirectFixActivity.class)
                .setAction(Intent.ACTION_VIEW)
                .setData(Uri.parse(fixUrls(foundUrl)));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resolver_activity);

        DelayedProgressBar progress = ButterKnife.findById(this, R.id.resolver_progress);
        progress.show(true);

        Intent source = getIntent().setComponent(null);
        Single.just(source)
                .filter(browserIntentChecker::hasOnlyBrowsers)
                .flatMap(intent -> redirectFixer.followRedirects(intent).toMaybe())
                .defaultIfEmpty(source)
                .compose(schedulingStrategy.applyToMaybe())
                .subscribe(intent -> {
                    startActivity(intent.setComponent(new ComponentName(this, ResolverActivity.class)));
                    finish();
                });
    }
}
