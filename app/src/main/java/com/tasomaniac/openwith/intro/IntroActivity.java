package com.tasomaniac.openwith.intro;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.util.Utils;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class IntroActivity extends AppIntro {

    private static final int REQUEST_CODE_USAGE_STATS = 909;
    private boolean showUsageStatsSlide;

    @Override
    public void init(@Nullable Bundle savedInstanceState) {

        int bgColor = ContextCompat.getColor(this, R.color.theme_primary);

        addSlide(AppIntroFragment.newInstance("Welcome",
                "I rescue you when you stuck in browsers. I help you to open them in native apps.",
                R.mipmap.ic_launcher,
                bgColor));

        addSlide(AppIntroFragment.newInstance("Did it ever happen to you?",
                "You click a short-link and somehow you end up being stuck in web page instead of watching the video directly.",
                R.drawable.tutorial_1,
                bgColor));

        addSlide(AppIntroFragment.newInstance("Share it!",
                "Just share it with me and I will switch apps for you.",
                R.drawable.tutorial_2,
                bgColor));

        addSlide(AppIntroFragment.newInstance("Preferred Apps",
                "You can also select \"Always\" to tell me to open that link always with a specific app.",
                R.drawable.tutorial_3,
                bgColor));

        if (SDK_INT >= LOLLIPOP && !Utils.isUsageStatsEnabled(this)) {
            showUsageStatsSlide = true;
            addSlide(AppIntroFragment.newInstance("Usage Stats",
                    "Give me access to \"Usage Stats\" and I will work better.\nI will detect the browser you are sharing from to act accordingly.",
                    R.drawable.tutorial_4,
                    bgColor));

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
    public void onSlideChanged() {

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Utils.isUsageStatsEnabled(this)) {
            setDoneText(getString(R.string.done));
        }
    }
}
