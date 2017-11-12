package com.tasomaniac.openwith.intro;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.data.Analytics;
import com.tasomaniac.openwith.rx.SchedulingStrategy;
import com.tasomaniac.openwith.settings.UsageStats;
import com.tasomaniac.openwith.util.Intents;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class IntroActivity extends AppIntro {

    private static final String EXTRA_FIRST_START = "first_start";

    @Inject Analytics analytics;
    @Inject SchedulingStrategy schedulingStrategy;

    private final CompositeDisposable disposables = new CompositeDisposable();

    private boolean usageStatsSlideAdded;

    public static Intent newIntent(Context context) {
        return new Intent(context, IntroActivity.class)
                .putExtra(IntroActivity.EXTRA_FIRST_START, true);
    }

    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        analytics.sendScreenView("App Intro");

        addSlide(new AppIntroFragment.Builder()
                         .title(R.string.title_tutorial_0)
                         .description(R.string.description_tutorial_0)
                         .drawable(R.drawable.tutorial_0).build());

        addSlide(new AppIntroFragment.Builder()
                         .title(R.string.title_tutorial_1)
                         .description(R.string.description_tutorial_1)
                         .drawable(R.drawable.tutorial_1).build());

        addSlide(new AppIntroFragment.Builder()
                         .title(R.string.title_tutorial_2)
                         .description(R.string.description_tutorial_2)
                         .drawable(R.drawable.tutorial_2).build());

        addSlide(new AppIntroFragment.Builder()
                         .title(R.string.title_tutorial_3)
                         .description(R.string.description_tutorial_3)
                         .drawable(R.drawable.tutorial_3).build());

        if (getResources().getBoolean(R.bool.add_to_home_screen_enabled)) {
            addSlide(new AppIntroFragment.Builder()
                             .title(R.string.title_tutorial_4)
                             .description(R.string.description_tutorial_4)
                             .drawable(R.drawable.tutorial_4).build());
        }

        if (SDK_INT >= LOLLIPOP && !UsageStats.isEnabled(this)) {
            addUsageStatsSlide();
        }
    }

    @RequiresApi(LOLLIPOP)
    private void addUsageStatsSlide() {
        usageStatsSlideAdded = true;
        addSlide(new AppIntroFragment.Builder()
                         .title(R.string.title_tutorial_5)
                         .description(R.string.description_tutorial_5)
                         .drawable(R.drawable.tutorial_5).build());

        setDoneText(getString(R.string.usage_access_give_access));
    }

    @Override
    public void onSkipPressed() {
        finish();
    }

    @Override
    public void onNextPressed() {
    }

    @TargetApi(LOLLIPOP)
    @Override
    public void onDonePressed() {
        if (usageStatsSlideAdded && !UsageStats.isEnabled(this)) {
            boolean success = UsageStats.maybeStartUsageAccessSettings(this);
            if (success) {
                observeUsageStats();
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    @RequiresApi(LOLLIPOP)
    private void observeUsageStats() {
        Disposable disposable = UsageStats.observeAccessGiven(this)
                .compose(schedulingStrategy.forCompletable())
                .subscribe(() -> Intents.restartSettings(this));
        disposables.add(disposable);
    }

    @TargetApi(LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();

        if (usageStatsSlideAdded && UsageStats.isEnabled(this)) {
            setDoneText(getString(R.string.done));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (SDK_INT >= LOLLIPOP && shouldTrackUsageAccess()) {
            analytics.sendEvent(
                    "Usage Access",
                    "Given in first intro",
                    Boolean.toString(UsageStats.isEnabled(this))
            );
        }
        disposables.clear();
    }

    private boolean shouldTrackUsageAccess() {
        return getIntent().getBooleanExtra(EXTRA_FIRST_START, false);
    }
}
