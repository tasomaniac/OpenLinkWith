package com.tasomaniac.openwith.intro;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.util.Utils;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class IntroActivity extends AppIntro {

    private static final int REQUEST_CODE_USAGE_STATS = 909;
    private boolean showUsageStatsSlide;

    @Override
    public void init(@Nullable Bundle savedInstanceState) {

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDonePressed() {
        if (showUsageStatsSlide && !Utils.isUsageStatsEnabled(this)) {
            startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), REQUEST_CODE_USAGE_STATS);
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Utils.isUsageStatsEnabled(this)) {
            setDoneText(getString(R.string.done));
        }
    }
}
