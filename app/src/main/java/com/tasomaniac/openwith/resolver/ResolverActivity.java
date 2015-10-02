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
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.provider.Settings;
import android.support.v4.app.ShareCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tasomaniac.openwith.R;

import java.util.Iterator;
import java.util.Set;

import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.COMPONENT;
import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.HOST;
import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.LAST_CHOSEN;
import static com.tasomaniac.openwith.data.OpenWithProvider.OpenWithHosts.CONTENT_URI;

/**
 * This activity is displayed when the system attempts to start an Intent for
 * which there is more than one matching activity, allowing the user to decide
 * which to go to.  It is not normally used directly by application developers.
 */
public class ResolverActivity extends Activity
        implements ResolveListAdapter.OnItemClickedListener,
        ResolveListAdapter.OnItemLongClickedListener {

    public static final String EXTRA_PRIORITY_PACKAGES = "EXTRA_PRIORITY_PACKAGES";

    private ResolveListAdapter mAdapter;
    private PackageManager mPm;
    private boolean mAlwaysUseOption;

    private RecyclerView mListView;
    private Button mAlwaysButton;
    private Button mOnceButton;
    private int mIconDpi;
    private int mLastSelected = ListView.INVALID_POSITION;

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
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void onCreate(Bundle savedInstanceState) {
        final Intent intent = makeMyIntent();

        setTheme(R.style.BottomSheet_Light);
        super.onCreate(savedInstanceState);

        mRequestedUri = intent.getData();

        mPm = getPackageManager();

        mPackageMonitor.register(this, getMainLooper(), false);
        mRegistered = true;

        final ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        mIconDpi = am.getLauncherLargeIconDensity();

        ComponentName callerActivity = intent.getParcelableExtra(ShareCompat.EXTRA_CALLING_ACTIVITY);
        mAdapter = new ResolveListAdapter(this, getHistory(), intent, callerActivity, true);
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

        int count = mAdapter.mList.size();
        if (count > 1) {
            setContentView(layoutId);
            mListView = (RecyclerView) findViewById(R.id.resolver_list);
            mListView.setAdapter(mAdapter);
            mAdapter.setOnItemClickedListener(this);
            mAdapter.setOnItemLongClickedListener(this);

            if (mAlwaysUseOption) {
                mAdapter.setSelectable(true);
            }
            if (useHeader) {
                mAdapter.setHeader(new ResolveListAdapter.Header());
            }
        } else if (count == 1) {
            startActivity(mAdapter.intentForPosition(0, false));
            mPackageMonitor.unregister();
            mRegistered = false;
            finish();
            return;
        } else {
            setContentView(R.layout.resolver_list);

            final TextView empty = (TextView) findViewById(R.id.empty);
            empty.setVisibility(View.VISIBLE);

            mListView = (RecyclerView) findViewById(R.id.resolver_list);
            mListView.setVisibility(View.GONE);
        }

        mListView.setNestedScrollingEnabled(true);
        mListView.setLayoutManager(new LinearLayoutManager(this));

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

    protected CharSequence getTitleForAction() {
        final DisplayResolveInfo item = mAdapter.getFilteredItem();
        return item != null ? getString(R.string.whichViewApplicationNamed, item.displayLabel) :
                getString(R.string.whichViewApplication);
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
            final int checkedPos = mAdapter.getCheckedItemPosition();
            final boolean hasValidSelection = checkedPos != ListView.INVALID_POSITION;
            mLastSelected = checkedPos;
            mAlwaysButton.setEnabled(hasValidSelection);
            mOnceButton.setEnabled(hasValidSelection);
            if (hasValidSelection) {
                mAdapter.setSelection(checkedPos);
            }
        }
    }

    @Override
    public void onItemClicked(int position) {
        final int checkedPos = mAdapter.getCheckedItemPosition();
        final boolean hasValidSelection = checkedPos != ListView.INVALID_POSITION;
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

    public void onButtonClick(View v) {
        final int id = v.getId();
        startSelected(mAlwaysUseOption ?
                        mAdapter.getCheckedItemPosition() : mAdapter.getFilteredPosition(),
                id == R.id.button_always,
                mAlwaysUseOption);
        dismiss();
    }

    void startSelected(int which, boolean always, boolean filtered) {
        if (isFinishing()) {
            return;
        }
        ResolveInfo ri = mAdapter.resolveInfoForPosition(which, filtered);
        Intent intent = mAdapter.intentForPosition(which, filtered);
        onIntentSelected(ri, intent, always);
        finish();
    }

    protected void onIntentSelected(ResolveInfo ri, Intent intent, boolean alwaysCheck) {

        final ChooserHistory history = getHistory();

        if ((mAlwaysUseOption || mAdapter.hasFilteredItem()) && mAdapter.mOrigResolveList != null) {
            // Build a reasonable intent filter, based on what matched.
            IntentFilter filter = new IntentFilter();

            if (intent.getAction() != null) {
                filter.addAction(intent.getAction());
            }
            Set<String> categories = intent.getCategories();
            if (categories != null) {
                for (String cat : categories) {
                    filter.addCategory(cat);
                }
            }
            filter.addCategory(Intent.CATEGORY_DEFAULT);

            int cat = ri.match & IntentFilter.MATCH_CATEGORY_MASK;
            Uri data = intent.getData();
            if (cat == IntentFilter.MATCH_CATEGORY_TYPE) {
                String mimeType = intent.resolveType(this);
                if (mimeType != null) {
                    try {
                        filter.addDataType(mimeType);
                    } catch (IntentFilter.MalformedMimeTypeException e) {
                        Log.w("ResolverActivity", e);
                        filter = null;
                    }
                }
            }
            if (filter != null && data != null && data.getScheme() != null) {
                // We need the data specification if there was no type,
                // OR if the scheme is not one of our magical "file:"
                // or "content:" schemes (see IntentFilter for the reason).
                if (cat != IntentFilter.MATCH_CATEGORY_TYPE
                        || (!"file".equals(data.getScheme())
                        && !"content".equals(data.getScheme()))) {
                    filter.addDataScheme(data.getScheme());

                    // Look through the resolved filter to determine which part
                    // of it matched the original Intent.
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        Iterator<PatternMatcher> pIt = ri.filter.schemeSpecificPartsIterator();

                        if (pIt != null) {
                            String ssp = data.getSchemeSpecificPart();
                            while (ssp != null && pIt.hasNext()) {
                                PatternMatcher p = pIt.next();
                                if (p.match(ssp)) {
                                    filter.addDataSchemeSpecificPart(p.getPath(), p.getType());
                                    break;
                                }
                            }
                        }
                    }
                    Iterator<IntentFilter.AuthorityEntry> aIt = ri.filter.authoritiesIterator();
                    if (aIt != null) {
                        while (aIt.hasNext()) {
                            IntentFilter.AuthorityEntry a = aIt.next();
                            if (a.match(data) >= 0) {
                                int port = a.getPort();
                                filter.addDataAuthority(a.getHost(),
                                        port >= 0 ? Integer.toString(port) : null);
                                break;
                            }
                        }
                    }
                    Iterator<PatternMatcher> pIt = ri.filter.pathsIterator();
                    if (pIt != null) {
                        String path = data.getPath();
                        while (path != null && pIt.hasNext()) {
                            PatternMatcher p = pIt.next();
                            if (p.match(path)) {
                                filter.addDataPath(p.getPath(), p.getType());
                                break;
                            }
                        }
                    }
                }
            }

            if (filter != null) {
                if (alwaysCheck) {
                    //TODO handle preferred
//                    getPackageManager().addPreferredActivity(filter, bestMatch, set,
//                            intent.getComponent());
                    history.addPrefered(intent.getComponent().getPackageName());
                } else {
                    history.add(intent.getComponent().getPackageName());

                    ContentValues values = new ContentValues(3);
                    values.put(HOST, mRequestedUri.getHost());
                    values.put(COMPONENT, intent.getComponent().flattenToString());
                    values.put(LAST_CHOSEN, true);
                    getContentResolver().insert(CONTENT_URI, values);
                }
            }
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
    public boolean onItemLongClicked(int position) {
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