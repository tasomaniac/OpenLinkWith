package com.tasomaniac.openwith.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.tasomaniac.openwith.App;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.data.prefs.BooleanPreference;
import com.tasomaniac.openwith.intro.IntroActivity;

public class SettingsActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    BooleanPreference tutorialShown;

    public void onCreate(Bundle savedInstanceState) {
        tutorialShown = new BooleanPreference(
                PreferenceManager.getDefaultSharedPreferences(this),
                "pref_tutorial_shown");
        if (!tutorialShown.get()) {
            startActivity(new Intent(this, IntroActivity.class)
                    .putExtra(IntroActivity.EXTRA_FIRST_START, true));
            tutorialShown.set(true);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        App.getApp(this).getAnalytics().sendScreenView("Settings");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_done);
        toolbar.setNavigationContentDescription(R.string.done);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(getTitle());

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, SettingsFragment.newInstance())
                    .commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
