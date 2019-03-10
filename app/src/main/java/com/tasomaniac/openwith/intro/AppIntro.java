package com.tasomaniac.openwith.intro;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.tasomaniac.openwith.R;
import dagger.android.support.DaggerAppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public abstract class AppIntro extends DaggerAppCompatActivity {

    private PagerAdapter mPagerAdapter;
    private AppIntroViewPager pager;
    private List<Fragment> fragments = new ArrayList<>();
    private int slidesNumber;
    private CircularIndicatorView mController;
    private boolean skipButtonEnabled = true;
    private boolean baseProgressButtonEnabled = true;
    private boolean progressButtonEnabled = true;
    private View skipButton;
    private View nextButton;
    private View doneButton;
    private int savedCurrentItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.intro_layout);

        skipButton = findViewById(R.id.skip);
        nextButton = findViewById(R.id.next);
        doneButton = findViewById(R.id.done);
        mPagerAdapter = new PagerAdapter(super.getSupportFragmentManager(), fragments);
        pager = findViewById(R.id.view_pager);
        pager.setAdapter(this.mPagerAdapter);

        if (savedInstanceState != null) {
            restoreLockingState(savedInstanceState);
        }

        skipButton.setOnClickListener(v -> onSkipPressed());

        nextButton.setOnClickListener(v -> {
            pager.setCurrentItem(pager.getCurrentItem() + 1);
            onNextPressed();
        });

        doneButton.setOnClickListener(v -> onDonePressed());

        mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), fragments);
        pager = findViewById(R.id.view_pager);

        pager.setAdapter(this.mPagerAdapter);

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                if (slidesNumber > 1) {
                    mController.selectPosition(position);
                }

                // Allow the swipe to be re-enabled if a user swipes to a previous slide. Restore
                // state of progress button depending on global progress button setting
                if (!pager.isNextPagingEnabled()) {
                    if (pager.getCurrentItem() != pager.getLockPage()) {
                        setProgressButtonEnabled(baseProgressButtonEnabled);
                        pager.setNextPagingEnabled(true);
                    } else {
                        setProgressButtonEnabled(progressButtonEnabled);
                    }
                } else {
                    setProgressButtonEnabled(progressButtonEnabled);
                }
                skipButton.setVisibility(skipButtonEnabled ? View.VISIBLE : View.GONE);
            }
        });
        pager.setCurrentItem(savedCurrentItem); //required for triggering onPageSelected for first page

        init(savedInstanceState);
        slidesNumber = fragments.size();

        if (slidesNumber == 1) {
            setProgressButtonEnabled(progressButtonEnabled);
        } else {
            initController();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("baseProgressButtonEnabled", baseProgressButtonEnabled);
        outState.putBoolean("progressButtonEnabled", progressButtonEnabled);
        outState.putBoolean("skipButtonEnabled", skipButtonEnabled);
        outState.putBoolean("nextEnabled", pager.isPagingEnabled());
        outState.putBoolean("nextPagingEnabled", pager.isNextPagingEnabled());
        outState.putInt("lockPage", pager.getLockPage());
        outState.putInt("currentItem", pager.getCurrentItem());
    }

    private void restoreLockingState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.baseProgressButtonEnabled = savedInstanceState.getBoolean("baseProgressButtonEnabled");
        this.progressButtonEnabled = savedInstanceState.getBoolean("progressButtonEnabled");
        this.skipButtonEnabled = savedInstanceState.getBoolean("skipButtonEnabled");
        this.savedCurrentItem = savedInstanceState.getInt("currentItem");
        pager.setPagingEnabled(savedInstanceState.getBoolean("nextEnabled"));
        pager.setNextPagingEnabled(savedInstanceState.getBoolean("nextPagingEnabled"));
        pager.setLockPage(savedInstanceState.getInt("lockPage"));
    }

    private void initController() {
        mController = findViewById(R.id.indicator);
        mController.initialize(slidesNumber);
    }

    public void addSlide(Fragment fragment) {
        fragments.add(fragment);
        mPagerAdapter.notifyDataSetChanged();
    }

    public abstract void init(@Nullable Bundle savedInstanceState);

    public abstract void onSkipPressed();

    public abstract void onNextPressed();

    public abstract void onDonePressed();

    @Override
    public boolean onKeyDown(int code, KeyEvent event) {
        if (code == KeyEvent.KEYCODE_ENTER || code == KeyEvent.KEYCODE_BUTTON_A || code == KeyEvent.KEYCODE_DPAD_CENTER) {
            ViewPager vp = this.findViewById(R.id.view_pager);
            if (vp.getCurrentItem() == vp.getAdapter().getCount() - 1) {
                onDonePressed();
            } else {
                vp.setCurrentItem(vp.getCurrentItem() + 1);
            }
            return false;
        }
        return super.onKeyDown(code, event);
    }

    /**
     * Setting to to display or hide the Next or Done button. This is a static setting and
     * button state is maintained across slides until explicitly changed.
     *
     * @param progressButtonEnabled Set true to display. False to hide.
     */
    private void setProgressButtonEnabled(boolean progressButtonEnabled) {
        this.progressButtonEnabled = progressButtonEnabled;
        if (progressButtonEnabled) {
            if (pager.getCurrentItem() == slidesNumber - 1) {
                nextButton.setVisibility(View.GONE);
                doneButton.setVisibility(View.VISIBLE);
            } else {
                nextButton.setVisibility(View.VISIBLE);
                doneButton.setVisibility(View.GONE);
            }
        } else {
            nextButton.setVisibility(View.GONE);
            doneButton.setVisibility(View.GONE);
        }
    }

    void setDoneText(@Nullable final String text) {
        TextView doneText = findViewById(R.id.done);
        doneText.setText(text);
    }

    private static class PagerAdapter extends FragmentStateAdapter {
        List<Fragment> fragments;

        PagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override public int getItemCount() {
            return fragments.size();
        }
    }
}
