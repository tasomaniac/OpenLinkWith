/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tasomaniac.openwith.resolver;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.homescreen.AddToHomeScreenDialogFragment;
import com.tasomaniac.openwith.misc.ItemClickListener;
import com.tasomaniac.openwith.misc.ItemLongClickListener;
import com.tasomaniac.openwith.util.Intents;

import timber.log.Timber;

import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.*;
import static com.tasomaniac.openwith.data.OpenWithProvider.OpenWithHosts.CONTENT_URI;

/**
 * This activity is displayed when the system attempts to start an Intent for
 * which there is more than one matching activity, allowing the user to decide
 * which to go to.  It is not normally used directly by application developers.
 */
public class ResolverActivity extends AppCompatActivity
        implements
        ItemClickListener,
        ItemLongClickListener {

    public static final String EXTRA_PRIORITY_PACKAGES = "EXTRA_PRIORITY_PACKAGES";
    public static final String EXTRA_ADD_TO_HOME_SCREEN = "EXTRA_ADD_TO_HOME_SCREEN";
    private static final String KEY_CHECKED_POS = "KEY_CHECKED_POS";
    public static final String EXTRA_LAST_CHOSEN_COMPONENT = "last_chosen";

    private ResolveListAdapter mAdapter;
    private PackageManager mPm;
    private boolean mAlwaysUseOption;
    private boolean isAddToHomeScreen;

    private RecyclerView mListView;

    private Button mAlwaysButton;
    private Button mOnceButton;
    private int mIconDpi;

    private int mLastSelected = RecyclerView.NO_POSITION;

    private Uri mRequestedUri;
    private ChooserHistory mHistory;

    private ChooserHistory getHistory() {
        if (mHistory == null) {
            mHistory = ChooserHistory.fromSettings(this);
        }
        return mHistory;
    }

    private boolean mRegistered;
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        @Override
        public void onSomePackagesChanged() {
            handlePackagesChanged();
        }
    };

    private Intent makeMyIntent() {
        Intent intent = new Intent(getIntent());
        intent.setComponent(null);
        // The resolver activity is set to be hidden from recent tasks.
        // we don't want this attribute to be propagated to the next activity
        // being launched.  Note that if the original Intent also had this
        // flag set, we are now losing it.  That should be a very rare case
        // and we can live with this.
        intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Intent intent = makeMyIntent();

        setTheme(R.style.BottomSheet_Light);
        super.onCreate(savedInstanceState);

        isAddToHomeScreen = intent.getBooleanExtra(EXTRA_ADD_TO_HOME_SCREEN, false);
        mPm = getPackageManager();

        mRequestedUri = intent.getData();
        if (mRequestedUri == null) {
            finish();
            return;
        }

        mPackageMonitor.register(this, getMainLooper(), false);
        mRegistered = true;

        final ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        mIconDpi = am.getLauncherLargeIconDensity();

        mAdapter = new ResolveListAdapter(
                this,
                getHistory(),
                intent,
                getIntent().getStringExtra(ShareCompat.EXTRA_CALLING_PACKAGE),
                intent.<ComponentName>getParcelableExtra(EXTRA_LAST_CHOSEN_COMPONENT),
                true,
                intent.getStringArrayExtra(EXTRA_PRIORITY_PACKAGES)
        );

        mAlwaysUseOption = !isAddToHomeScreen && !mAdapter.hasFilteredItem();

        int count = mAdapter.mList.size();
        if (count == 0) {
            Toast.makeText(this, getString(R.string.empty_resolver_activity), Toast.LENGTH_LONG).show();
            mPackageMonitor.unregister();
            mRegistered = false;
            finish();
            return;
        }
        if (count == 1 && !isAddToHomeScreen) {
            final DisplayResolveInfo dri = mAdapter.displayResolveInfoForPosition(0, false);
            Toast.makeText(
                    this,
                    getString(
                            R.string.warning_open_link_with_name,
                            dri.displayLabel()
                    ),
                    Toast.LENGTH_SHORT
            ).show();
            Intents.startActivityFixingIntent(this, mAdapter.intentForDisplayResolveInfo(dri));
            mPackageMonitor.unregister();
            mRegistered = false;
            finish();
            return;
        }

        setupListAdapter();

        mListView.setLayoutManager(new LinearLayoutManager(this));

        final ResolverDrawerLayout rdl = (ResolverDrawerLayout) findViewById(R.id.contentPanel);
        if (rdl != null) {
            rdl.setOnDismissedListener(new ResolverDrawerLayout.OnDismissedListener() {
                @Override
                public void onDismissed() {
                    finish();
                }
            });
        }

        setupTitle();

        final ImageView iconView = (ImageView) findViewById(R.id.icon);
        final DisplayResolveInfo iconInfo = mAdapter.getFilteredItem();
        if (iconView != null && iconInfo != null) {
            new LoadIconIntoViewTask(iconView).execute(iconInfo);
        }

        if (mAlwaysUseOption || mAdapter.hasFilteredItem()) {
            final ViewGroup buttonLayout = (ViewGroup) findViewById(R.id.button_bar);
            if (buttonLayout != null) {
                buttonLayout.setVisibility(View.VISIBLE);
                mAlwaysButton = (Button) buttonLayout.findViewById(R.id.button_always);
                mOnceButton = (Button) buttonLayout.findViewById(R.id.button_once);
            } else {
                mAlwaysUseOption = false;
            }
        }

        if (mAdapter.hasFilteredItem()) {
            mAlwaysButton.setEnabled(true);
            mOnceButton.setEnabled(true);
        }
    }

    private void setupListAdapter() {
        boolean useHeader = !isAddToHomeScreen && mAdapter.hasFilteredItem();
        int layoutId = useHeader ? R.layout.resolver_list_with_default : R.layout.resolver_list;

        setContentView(layoutId);
        mListView = (RecyclerView) findViewById(R.id.resolver_list);
        mListView.setAdapter(mAdapter);

        mAdapter.setItemClickListener(this);
        mAdapter.setItemLongClickListener(this);

        if (mAlwaysUseOption) {
            mAdapter.setSelectionEnabled(true);
        }
        if (useHeader) {
            mAdapter.setHeader(new ResolveListAdapter.Header());
        }
    }

    private void setupTitle() {
        final TextView titleView = (TextView) findViewById(R.id.title);
        if (isAddToHomeScreen) {
            titleView.setText(R.string.add_to_homescreen);
        } else {
            titleView.setText(getTitleForAction());
        }
    }

    private CharSequence getTitleForAction() {
        final DisplayResolveInfo item = mAdapter.getFilteredItem();
        return item != null ? getString(R.string.which_view_application_named, item.displayLabel()) :
                getString(R.string.which_view_application);
    }

    private void dismiss() {
        if (!isFinishing()) {
            finish();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!mRegistered) {
            mPackageMonitor.register(this, getMainLooper(), false);
            mRegistered = true;
        }
        handlePackagesChanged();
    }

    private void handlePackagesChanged() {
        mAdapter.handlePackagesChanged();
        if (mAdapter.getItemCount() == 0) {
            // We no longer have any items...  just finish the activity.
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mRegistered) {
            mPackageMonitor.unregister();
            mRegistered = false;
        }
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0) {
            // This resolver is in the unusual situation where it has been
            // launched at the top of a new task.  We don't let it be added
            // to the recent tasks shown to the user, and we need to make sure
            // that each time we are launched we get the correct launching
            // uid (not re-using the same resolver from an old launching uid),
            // so we will now finish ourself since being no longer visible,
            // the user probably can't get back to us.
            if (!isChangingConfigurations()) {
                finish();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_CHECKED_POS, mAdapter.getCheckedItemPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (mAlwaysUseOption) {
            final int checkedPos = savedInstanceState.getInt(KEY_CHECKED_POS);
            final boolean hasValidSelection = checkedPos != ListView.INVALID_POSITION;
            mLastSelected = checkedPos;
            mAlwaysButton.setEnabled(hasValidSelection);
            mOnceButton.setEnabled(hasValidSelection);
            if (hasValidSelection) {
                mAdapter.setItemChecked(checkedPos);
            }
        }
    }

    @Override
    public void onItemClick(View view, final int position, long id) {
        final boolean hasValidSelection = position != RecyclerView.NO_POSITION;
        if (mAlwaysUseOption && (!hasValidSelection || mLastSelected != position)) {
            mAlwaysButton.setEnabled(hasValidSelection);
            mOnceButton.setEnabled(hasValidSelection);
            if (hasValidSelection) {
                mListView.smoothScrollToPosition(position);
            }
            mLastSelected = position;
        } else {
            startSelected(position, false, true);
        }
    }

    public void onButtonClick(View v) {
        final int id = v.getId();
        startSelected(
                getSelectedIntentPosition(),
                id == R.id.button_always,
                mAlwaysUseOption
        );
        dismiss();
    }

    int getSelectedIntentPosition() {
        return mAlwaysUseOption ?
                mAdapter.getCheckedItemPosition() : mAdapter.getFilteredPosition();
    }

    private void startSelected(int which, boolean always, boolean filtered) {
        if (isFinishing()) {
            return;
        }
        DisplayResolveInfo dri = mAdapter.displayResolveInfoForPosition(which, filtered);
        Intent intent = mAdapter.intentForDisplayResolveInfo(dri);
        if (isAddToHomeScreen) {
            AddToHomeScreenDialogFragment
                    .newInstance(dri, intent)
                    .show(getSupportFragmentManager());
        } else {
            onIntentSelected(intent, always);
            finish();
        }
    }

    private void onIntentSelected(Intent intent, boolean alwaysCheck) {
        if (mAlwaysUseOption || mAdapter.hasFilteredItem()) {
            persistSelectedIntent(intent, alwaysCheck);
        }

        Intents.startActivityFixingIntent(this, intent);
    }

    private void persistSelectedIntent(Intent intent, boolean alwaysCheck) {
        if (intent.getComponent() == null) {
            return;
        }
        ContentValues values = new ContentValues(4);
        values.put(HOST, mRequestedUri.getHost());
        values.put(COMPONENT, intent.getComponent().flattenToString());

        if (alwaysCheck) {
            values.put(PREFERRED, true);
        }
        values.put(LAST_CHOSEN, true);
        try {
            getContentResolver().insert(CONTENT_URI, values);
        } catch (Exception e) {
            Timber.e(e, "Error while saving selected Intent");
        }

        final ChooserHistory history = getHistory();
        history.add(intent.getComponent().getPackageName());
        history.save(this);
    }

    @Override
    public boolean onItemLongClick(View view, int position, long id) {
        ResolveInfo ri = mAdapter.displayResolveInfoForPosition(position, true).ri;
        showAppDetails(ri);
        return true;
    }

    @SuppressWarnings("deprecation")
    private void showAppDetails(ResolveInfo ri) {
        Intent in = new Intent().setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", ri.activityInfo.packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivity(in);
    }

    private class LoadIconIntoViewTask extends AsyncTask<DisplayResolveInfo, Void, DisplayResolveInfo> {
        final ImageView mTargetView;

        LoadIconIntoViewTask(ImageView target) {
            mTargetView = target;
        }

        @Override
        protected DisplayResolveInfo doInBackground(DisplayResolveInfo... params) {
            final DisplayResolveInfo info = params[0];
            if (info.displayIcon() == null) {
                info.displayIcon(ResolveListAdapter.loadIconForResolveInfo(mPm, info.ri, mIconDpi));
            }
            return info;
        }

        @Override
        protected void onPostExecute(DisplayResolveInfo info) {
            mTargetView.setImageDrawable(info.displayIcon());
        }
    }
}
