package com.tasomaniac.openwith.intro;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.tasomaniac.openwith.intro.lib.R;
import dagger.android.support.DaggerAppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public abstract class AppIntro extends DaggerAppCompatActivity {

    private PagerAdapter mPagerAdapter;
    private ViewPager2 pager;
    private List<Fragment> fragments = new ArrayList<>();
    private int numberOfPages;
    private CircularIndicatorView mController;
    private View nextButton;
    private View doneButton;
    private int savedCurrentItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_layout);

        findViewById(R.id.skip).setOnClickListener(v -> onSkipPressed());
        nextButton = findViewById(R.id.next);
        nextButton.setOnClickListener(v -> {
            pager.setCurrentItem(pager.getCurrentItem() + 1);
            onNextPressed();
        });
        doneButton = findViewById(R.id.done);
        doneButton.setOnClickListener(v -> onDonePressed());

        setupPager(savedInstanceState);

        init(savedInstanceState);
        numberOfPages = fragments.size();

        if (numberOfPages == 1) {
            updateProgressButton();
        } else {
            initController();
        }
    }

    private void setupPager(@Nullable Bundle savedInstanceState) {
        mPagerAdapter = new PagerAdapter(this, fragments);
        pager = findViewById(R.id.view_pager);
        pager.setAdapter(mPagerAdapter);

        if (savedInstanceState != null) {
            savedCurrentItem = savedInstanceState.getInt("currentItem");
        }
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                if (numberOfPages > 1) {
                    mController.selectPosition(position);
                }

                updateProgressButton();
            }
        });
        pager.setCurrentItem(savedCurrentItem); //required for triggering onPageSelected for first page
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentItem", pager.getCurrentItem());
    }

    private void initController() {
        mController = findViewById(R.id.indicator);
        mController.initialize(numberOfPages);
    }

    public void addPage(Fragment fragment) {
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

    private void updateProgressButton() {
        if (pager.getCurrentItem() == numberOfPages - 1) {
            nextButton.setVisibility(View.GONE);
            doneButton.setVisibility(View.VISIBLE);
        } else {
            nextButton.setVisibility(View.VISIBLE);
            doneButton.setVisibility(View.GONE);
        }
    }

    void setDoneText(@Nullable final String text) {
        TextView doneText = findViewById(R.id.done);
        doneText.setText(text);
    }

    private static class PagerAdapter extends FragmentStateAdapter {
        List<Fragment> fragments;

        PagerAdapter(FragmentActivity activity, List<Fragment> fragments) {
            super(activity);
            this.fragments = fragments;
        }

        @NonNull @Override public Fragment createFragment(int position) {
            return this.fragments.get(position);
        }

        @Override public int getItemCount() {
            return fragments.size();
        }
    }
}
