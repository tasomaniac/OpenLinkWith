package com.tasomaniac.openwith.intro;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.tasomaniac.openwith.Analytics;
import com.tasomaniac.openwith.App;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.util.Intents;
import com.tasomaniac.openwith.util.Utils;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class IntroActivity extends AppIntro {

    public static final String EXTRA_FIRST_START = "first_start";

    private boolean showUsageStatsSlide;

    private Analytics analytics;

    @Override
    public void init(@Nullable Bundle savedInstanceState) {

        analytics = App.getApp(this).getAnalytics();
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

        if (SDK_INT >= LOLLIPOP && !Utils.isUsageStatsEnabled(this)) {
            showUsageStatsSlide = true;
            addSlide(new AppIntroFragment.Builder()
                    .title(R.string.title_tutorial_4)
                    .description(R.string.description_tutorial_4)
                    .drawable(R.drawable.tutorial_4).build());

            setDoneText("Give Access");
        }
    }

    @Override
    public void onSkipPressed() {
        finish();
    }

    @Override
    public void onNextPressed() {
    }

    @Override
    public void onDonePressed() {
        if (showUsageStatsSlide && !Utils.isUsageStatsEnabled(this)) {
            boolean success = Intents.maybeStartUsageAccessSettings(this);
            if (!success) {
                finish();
            }
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (showUsageStatsSlide && Utils.isUsageStatsEnabled(this)) {
            setDoneText(getString(R.string.done));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (SDK_INT >= LOLLIPOP && getIntent().getBooleanExtra(EXTRA_FIRST_START, false)) {
            analytics.sendEvent("Usage Access",
                    "Given in first intro",
                    Boolean.toString(Utils.isUsageStatsEnabled(this)));
        }
    }
}
