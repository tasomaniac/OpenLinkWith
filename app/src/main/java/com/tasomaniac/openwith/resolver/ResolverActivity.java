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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ShareCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tasomaniac.openwith.BuildConfig;
import com.tasomaniac.openwith.R;

import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.ItemSelectionSupport;
import org.lucasr.twowayview.widget.ListLayoutManager;

import java.util.List;

import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.COMPONENT;
import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.HOST;
import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.LAST_CHOSEN;
import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.PREFERRED;
import static com.tasomaniac.openwith.data.OpenWithProvider.OpenWithHosts.CONTENT_URI;
import static com.tasomaniac.openwith.data.OpenWithProvider.OpenWithHosts.withHost;
import static org.lucasr.twowayview.TwoWayLayoutManager.Orientation.VERTICAL;

/**
 * This activity is displayed when the system attempts to start an Intent for
 * which there is more than one matching activity, allowing the user to decide
 * which to go to.  It is not normally used directly by application developers.
 */
public class ResolverActivity extends Activity
        implements ItemClickSupport.OnItemClickListener,
        ItemClickSupport.OnItemLongClickListener {

    public static final String EXTRA_PRIORITY_PACKAGES = "EXTRA_PRIORITY_PACKAGES";

    private ResolveListAdapter mAdapter;
    private PackageManager mPm;
    private boolean mAlwaysUseOption;

    private RecyclerView mListView;
    private ItemSelectionSupport selectionSupport;

    private Button mAlwaysButton;
    private Button mOnceButton;
    private int mIconDpi;

    private int mLastSelected = ItemSelectionSupport.INVALID_POSITION;

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

        mPm = getPackageManager();

        mRequestedUri = intent.getData();

        boolean isCallerPackagePreferred = false;
        final String callerPackage = getCallerPackage();

        ResolveInfo lastChosen = null;
        final Cursor query =
                getContentResolver().query(withHost(intent.getData().getHost()), null, null, null, null);

        if (query != null && query.moveToFirst()) {

            final boolean isPreferred = query.getInt(query.getColumnIndex(PREFERRED)) == 1;
            final boolean isLastChosen = query.getInt(query.getColumnIndex(LAST_CHOSEN)) == 1;

            if (isPreferred || isLastChosen) {
                final String componentString = query.getString(query.getColumnIndex(COMPONENT));

                final Intent lastChosenIntent = new Intent();
                final ComponentName lastChosenComponent = ComponentName.unflattenFromString(componentString);
                lastChosenIntent.setComponent(lastChosenComponent);
                ResolveInfo ri = mPm.resolveActivity(lastChosenIntent, PackageManager.MATCH_DEFAULT_ONLY);

                if (isPreferred && ri != null) {
                    isCallerPackagePreferred = ri.activityInfo.packageName.equals(callerPackage);
                    if (!isCallerPackagePreferred) {
                        Toast.makeText(this, getString(R.string.warning_open_link_with_name,
                                        ri.loadLabel(mPm)),
                                Toast.LENGTH_SHORT).show();
                        intent.setComponent(lastChosenComponent);
                        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT
                                | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }

                lastChosen = ri;
            }
            query.close();
        }

        mPackageMonitor.register(this, getMainLooper(), false);
        mRegistered = true;

        final ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        mIconDpi = am.getLauncherLargeIconDensity();

        mAdapter = new ResolveListAdapter(this, getHistory(), intent, callerPackage, lastChosen, true);
        mAdapter.setPriorityItems(intent.getStringArrayExtra(EXTRA_PRIORITY_PACKAGES));

        mAlwaysUseOption = true;
        final int layoutId;
        final boolean useHeader;
        if (mAdapter.hasFilteredItem()) {
            layoutId = R.layout.resolver_list_with_default;
            mAlwaysUseOption = false;
            useHeader = true;
        } else {
            useHeader = false;
            layoutId = R.layout.resolver_list;
        }

        //If the caller is already the preferred, don't change it.
        if (isCallerPackagePreferred) {
            mAlwaysUseOption = false;
        }

        int count = mAdapter.mList.size();
        if (count > 1) {
            setContentView(layoutId);
            mListView = (RecyclerView) findViewById(R.id.resolver_list);
            mListView.setAdapter(mAdapter);

            selectionSupport = ItemSelectionSupport.addTo(mListView);

            final ItemClickSupport itemClickSupport = ItemClickSupport.addTo(mListView);
            itemClickSupport.setOnItemClickListener(this);
            itemClickSupport.setOnItemLongClickListener(this);

            if (mAlwaysUseOption) {
                selectionSupport.setChoiceMode(ItemSelectionSupport.ChoiceMode.SINGLE);
            }
            if (useHeader) {
                mAdapter.setHeader(new ResolveListAdapter.Header());
            }
        } else if (count == 1) {
            final DisplayResolveInfo dri = mAdapter.displayResolveInfoForPosition(0, false);
            Toast.makeText(this, getString(R.string.warning_open_link_with_name,
                        dri.getDisplayLabel()),
                    Toast.LENGTH_SHORT).show();
            startActivity(mAdapter.intentForPosition(0, false));
            mPackageMonitor.unregister();
            mRegistered = false;
            finish();
            return;
        } else {
            Toast.makeText(this, getString(R.string.empty_resolver_activity,
                            mRequestedUri.toString()),
                    Toast.LENGTH_LONG).show();
            mPackageMonitor.unregister();
            mRegistered = false;
            finish();
            return;
        }

        mListView.setLayoutManager(new ListLayoutManager(this, VERTICAL));

        // Prevent the Resolver window from becoming the top fullscreen window and thus from taking
        // control of the system bars.
        getWindow().clearFlags(FLAG_LAYOUT_IN_SCREEN | FLAG_LAYOUT_INSET_DECOR);

        final ResolverDrawerLayout rdl = (ResolverDrawerLayout) findViewById(R.id.contentPanel);
        if (rdl != null) {
            rdl.setOnDismissedListener(new ResolverDrawerLayout.OnDismissedListener() {
                @Override
                public void onDismissed() {
                    finish();
                }
            });
        }

        CharSequence title = getTitleForAction();
        if (!TextUtils.isEmpty(title)) {
            final TextView titleView = (TextView) findViewById(R.id.title);
            if (titleView != null) {
                titleView.setText(title);
            }
            setTitle(title);
        }

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

    private String getCallerPackage() {
        String callerPackage = getIntent().getStringExtra(ShareCompat.EXTRA_CALLING_PACKAGE);

        if (callerPackage != null) {
            return callerPackage;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return getCallerPackagerLegacy();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return getCallerPackageLollipop();
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    private String getCallerPackagerLegacy() {
        String callerPackage;ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(1);
        final ComponentName topActivity = runningTasks.get(0).baseActivity;
        callerPackage = topActivity.getPackageName();
        return callerPackage;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private String getCallerPackageLollipop() {
        UsageStatsManager mUsm = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        // We get usage stats for the last 10 seconds
        List<UsageStats> stats = mUsm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                time - 10 * DateUtils.SECOND_IN_MILLIS, time);
        if (stats == null) {
            return null;
        }

        UsageStats lastUsage = null;
        for (UsageStats currentUsage : stats) {
            String currentPackage = currentUsage.getPackageName();
            if (BuildConfig.APPLICATION_ID.equals(currentPackage)
                    || "android".equals(currentPackage)) {
                continue;
            }
            if (lastUsage == null ||
                    lastUsage.getLastTimeUsed() < currentUsage.getLastTimeUsed()) {
                lastUsage = currentUsage;
            }
        }
        if (lastUsage != null) {
            return lastUsage.getPackageName();
        }

        return null;
    }

    protected CharSequence getTitleForAction() {
        final DisplayResolveInfo item = mAdapter.getFilteredItem();
        return item != null ? getString(R.string.which_view_application_named, item.displayLabel) :
                getString(R.string.which_view_application);
    }

    void dismiss() {
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
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (mAlwaysUseOption) {
            final int checkedPos = selectionSupport.getCheckedItemPosition();
            final boolean hasValidSelection = checkedPos != ListView.INVALID_POSITION;
            mLastSelected = checkedPos;
            mAlwaysButton.setEnabled(hasValidSelection);
            mOnceButton.setEnabled(hasValidSelection);
            if (hasValidSelection) {
                selectionSupport.setItemChecked(checkedPos, true);
            }
        }
    }

    @Override
    public void onItemClick(RecyclerView parent, View view, final int position, long id) {
        mListView.post(new Runnable() {
            @Override
            public void run() {
                final int checkedPos = selectionSupport.getCheckedItemPosition();
                final boolean hasValidSelection = checkedPos != ItemSelectionSupport.INVALID_POSITION;
                if (mAlwaysUseOption && (!hasValidSelection || mLastSelected != checkedPos)) {
                    mAlwaysButton.setEnabled(hasValidSelection);
                    mOnceButton.setEnabled(hasValidSelection);
                    if (hasValidSelection) {
                        mListView.smoothScrollToPosition(checkedPos);
                    }
                    mLastSelected = checkedPos;
                } else {
                    startSelected(position, false, true);
                }
            }
        });
    }

    public void onButtonClick(View v) {
        final int id = v.getId();
        startSelected(mAlwaysUseOption ?
                        selectionSupport.getCheckedItemPosition()  : mAdapter.getFilteredPosition(),
                id == R.id.button_always,
                mAlwaysUseOption);
        dismiss();
    }

    void startSelected(int which, boolean always, boolean filtered) {
        if (isFinishing()) {
            return;
        }
        Intent intent = mAdapter.intentForPosition(which, filtered);
        onIntentSelected(intent, always);
        finish();
    }

    protected void onIntentSelected(Intent intent, boolean alwaysCheck) {

        final ChooserHistory history = getHistory();

        if (mAlwaysUseOption || mAdapter.hasFilteredItem()) {
            ContentValues values = new ContentValues(3);
            values.put(HOST, mRequestedUri.getHost());
            values.put(COMPONENT, intent.getComponent().flattenToString());

            if (alwaysCheck) {
                values.put(PREFERRED, true);
            }
            values.put(LAST_CHOSEN, true);
            getContentResolver().insert(CONTENT_URI, values);

            history.add(intent.getComponent().getPackageName());
        }

        if (intent != null) {
            startActivity(intent);
        }
        history.save(this);
    }

    @SuppressWarnings("deprecation")
    void showAppDetails(ResolveInfo ri) {
        Intent in = new Intent().setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", ri.activityInfo.packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivity(in);
    }

    @Override
    public boolean onItemLongClick(RecyclerView parent, View view, int position, long id) {
        ResolveInfo ri = mAdapter.resolveInfoForPosition(position, true);
        showAppDetails(ri);
        return true;
    }

    class LoadIconIntoViewTask extends AsyncTask<DisplayResolveInfo, Void, DisplayResolveInfo> {
        final ImageView mTargetView;

        public LoadIconIntoViewTask(ImageView target) {
            mTargetView = target;
        }

        @Override
        protected DisplayResolveInfo doInBackground(DisplayResolveInfo... params) {
            final DisplayResolveInfo info = params[0];
            if (info.displayIcon == null) {
                info.displayIcon = ResolveListAdapter.loadIconForResolveInfo(mPm, info.ri, mIconDpi);
            }
            return info;
        }

        @Override
        protected void onPostExecute(DisplayResolveInfo info) {
            mTargetView.setImageDrawable(info.displayIcon);
        }
    }
}