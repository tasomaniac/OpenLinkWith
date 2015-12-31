package com.tasomaniac.openwith.intro;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.tasomaniac.openwith.R;

public class IntroActivity extends AppIntro {

    @Override
    public void init(@Nullable Bundle savedInstanceState) {

        addSlide(AppIntroFragment.newInstance("Welcome", "To Be Implemented", R.drawable.ic_action_done,
                ContextCompat.getColor(this, R.color.theme_primary)));
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
        finish();
    }

    @Override
    public void onSlideChanged() {

    }
}
