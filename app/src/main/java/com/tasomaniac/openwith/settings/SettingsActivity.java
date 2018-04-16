package com.tasomaniac.openwith.settings;

import android.app.backup.BackupManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.data.Analytics;
import com.tasomaniac.openwith.data.prefs.BooleanPreference;
import com.tasomaniac.openwith.data.prefs.TutorialShown;
import com.tasomaniac.openwith.intro.IntroActivity;
import dagger.android.support.DaggerAppCompatActivity;

import javax.annotation.Nullable;
import javax.inject.Inject;

public class SettingsActivity extends DaggerAppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbar;

    @Inject @TutorialShown BooleanPreference tutorialShown;
    @Inject Analytics analytics;
    @Inject SharedPreferences sharedPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!tutorialShown.get()) {
            startActivity(IntroActivity.newIntent(this));
            tutorialShown.set(true);
        }

        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        toolbar.setNavigationIcon(R.drawable.ic_action_done);
        toolbar.setNavigationContentDescription(R.string.done);
        setSupportActionBar(toolbar);

        collapsingToolbar.setTitle(getTitle());

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, SettingsFragment.newInstance())
                    .commit();

            analytics.sendScreenView("Settings");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        new BackupManager(this).dataChanged();
    }
}
