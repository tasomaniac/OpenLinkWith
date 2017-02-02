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

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tasomaniac.openwith.IconLoader;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.data.Injector;
import com.tasomaniac.openwith.homescreen.AddToHomeScreenDialogFragment;
import com.tasomaniac.openwith.util.Intents;

import javax.inject.Inject;

import java.util.List;

import timber.log.Timber;

import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.*;
import static com.tasomaniac.openwith.data.OpenWithProvider.OpenWithHosts.CONTENT_URI;
import static com.tasomaniac.openwith.data.OpenWithProvider.OpenWithHosts.withHost;

/**
 * This activity is displayed when the system attempts to start an Intent for
 * which there is more than one matching activity, allowing the user to decide
 * which to go to.  It is not normally used directly by application developers.
 */
public class ResolverActivity extends AppCompatActivity implements
        ItemClickListener,
        ItemLongClickListener {

    public static final String EXTRA_ADD_TO_HOME_SCREEN = "EXTRA_ADD_TO_HOME_SCREEN";
    private static final String KEY_CHECKED_POS = "KEY_CHECKED_POS";
    private static final String KEY_CHECKED_ITEM = "KEY_CHECKED_ITEM";

    @Inject IconLoader iconLoader;
    @Inject ChooserHistory history;
    @Inject IntentResolver intentResolver;

    private ResolveListAdapter adapter;
    private boolean shouldUseAlwaysOption;
    private boolean isAddToHomeScreen;

    private Button mAlwaysButton;
    private Button mOnceButton;

    @Nullable private DisplayResolveInfo lastSelected;

    private boolean packageMonitorRegistered;
    private final PackageMonitor packageMonitor = new PackageMonitor() {
        @Override
        public void onSomePackagesChanged() {
            handlePackagesChanged();
        }
    };

    private Intent configureIntent() {
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
        setTheme(R.style.BottomSheet_Light);
        super.onCreate(savedInstanceState);

        final Intent intent = configureIntent();
        Uri uri = intent.getData();
        if (uri == null) {
            finish();
            return;
        }
        PreferredResolver preferredResolver = PreferredResolver.createFrom(this);
        preferredResolver.resolve(uri);

        if (preferredResolver.shouldStartPreferred()) {
            try {
                preferredResolver.startPreferred(this);
                return;
            } catch (SecurityException e) {
                Timber.e(e, "Security Exception for the url %s", uri);
                getContentResolver().delete(withHost(uri.getHost()), null, null);
            }
        }
        component(intent, preferredResolver.lastChosenComponent()).inject(this);

        isAddToHomeScreen = intent.getBooleanExtra(EXTRA_ADD_TO_HOME_SCREEN, false);
        List<DisplayResolveInfo> list = intentResolver.rebuildList();
        adapter = new ResolveListAdapter(iconLoader, intentResolver, intent);
        adapter.mList.addAll(list);
        shouldUseAlwaysOption = !isAddToHomeScreen && !intentResolver.hasFilteredItem();

        int count = adapter.mList.size();
        if (count == 0) {
            Timber.e("No app is found to handle url: %s", intent.getDataString());
            Toast.makeText(this, R.string.empty_resolver_activity, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (count == 1 && !isAddToHomeScreen) {
            DisplayResolveInfo dri = adapter.displayResolveInfoForPosition(0, false);
            PreferredResolver.startPreferred(this, adapter.intentForDisplayResolveInfo(dri), dri.displayLabel());
            return;
        }

        setContentView(shouldDisplayHeader() ? R.layout.resolver_list_with_default : R.layout.resolver_list);
        registerPackageMonitor();
        setupList();
        setupTitle();
        setupFilteredView();
        if (shouldUseAlwaysOption || intentResolver.hasFilteredItem()) {
            setupButtons();
        }
        ResolverDrawerLayout rdl = (ResolverDrawerLayout) findViewById(R.id.contentPanel);
        if (rdl != null) {
            rdl.setOnDismissedListener(new ResolverDrawerLayout.OnDismissedListener() {
                @Override
                public void onDismissed() {
                    finish();
                }
            });
        }
    }

    private ResolverComponent component(Intent sourceIntent, ComponentName lastChosenComponent) {
        return DaggerResolverComponent.builder()
                .appComponent(Injector.obtain(this))
                .resolverModule(new ResolverModule(this, sourceIntent, lastChosenComponent))
                .build();
    }

    private boolean shouldDisplayHeader() {
        return !isAddToHomeScreen && intentResolver.hasFilteredItem();
    }

    private void setupList() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.resolver_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setItemClickListener(this);
        adapter.setItemLongClickListener(this);

        if (shouldUseAlwaysOption) {
            adapter.setSelectionEnabled(true);
        }
        if (shouldDisplayHeader()) {
            adapter.displayHeader();
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
        final DisplayResolveInfo item = intentResolver.getFilteredItem();
        return item != null ? getString(R.string.which_view_application_named, item.displayLabel()) :
                getString(R.string.which_view_application);
    }

    private void setupFilteredView() {
        ImageView iconView = (ImageView) findViewById(R.id.icon);
        DisplayResolveInfo filteredItem = intentResolver.getFilteredItem();
        if (iconView != null && filteredItem != null) {
            new LoadIconIntoViewTask(iconLoader, iconView).execute(filteredItem);
        }
    }

    private void setupButtons() {
        ViewGroup buttonLayout = (ViewGroup) findViewById(R.id.button_bar);
        buttonLayout.setVisibility(View.VISIBLE);
        mAlwaysButton = (Button) buttonLayout.findViewById(R.id.button_always);
        mOnceButton = (Button) buttonLayout.findViewById(R.id.button_once);
    }

    private void dismiss() {
        if (!isFinishing()) {
            finish();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!packageMonitorRegistered) {
            registerPackageMonitor();
        }
        handlePackagesChanged();
    }

    private void registerPackageMonitor() {
        packageMonitor.register(this, getMainLooper(), false);
        packageMonitorRegistered = true;
    }

    private void handlePackagesChanged() {
        adapter.mList.clear();
        adapter.mList.addAll(intentResolver.rebuildList());
        adapter.notifyDataSetChanged();
        if (adapter.getItemCount() == 0) {
            // We no longer have any items...  just finish the activity.
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (packageMonitorRegistered) {
            packageMonitor.unregister();
            packageMonitorRegistered = false;
        }
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0) {
            // This resolver is in the unusual situation where it has been
            // launched at the top of a new task.  We don't let it be added
            // to the recent tasks shown to the user, and we need to make sure
            // that each time we are launched we get the correct launching
            // uid (not re-using the same resolver from an old launching uid),
            // so we will now finish ourselves since being no longer visible,
            // the user probably can't get back to us.
            if (!isChangingConfigurations()) {
                finish();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_CHECKED_POS, adapter.getCheckedItemPosition());
        outState.putParcelable(KEY_CHECKED_ITEM, lastSelected);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (shouldUseAlwaysOption) {
            lastSelected = savedInstanceState.getParcelable(KEY_CHECKED_ITEM);

            int checkedPos = savedInstanceState.getInt(KEY_CHECKED_POS);
            boolean hasValidSelection = checkedPos != RecyclerView.NO_POSITION;
            mAlwaysButton.setEnabled(hasValidSelection);
            mOnceButton.setEnabled(hasValidSelection);
            if (hasValidSelection) {
                adapter.setItemChecked(checkedPos);
            }
        }
    }

    @Override
    public void onItemClick(DisplayResolveInfo dri) {
        if (shouldUseAlwaysOption && !dri.equals(lastSelected)) {
            mAlwaysButton.setEnabled(true);
            mOnceButton.setEnabled(true);
            lastSelected = dri;
        } else {
            startSelected(dri, false);
        }
    }

    public void onButtonClick(View v) {
        startSelected(getSelectedItem(), v.getId() == R.id.button_always);
        dismiss();
    }

    private DisplayResolveInfo getSelectedItem() {
        return shouldUseAlwaysOption ? lastSelected : intentResolver.getFilteredItem();
    }

    private void startSelected(DisplayResolveInfo dri, boolean always) {
        if (isFinishing()) {
            return;
        }
        Intent intent = adapter.intentForDisplayResolveInfo(dri);
        if (isAddToHomeScreen) {
            AddToHomeScreenDialogFragment
                    .newInstance(dri, intent)
                    .show(getSupportFragmentManager());
        } else {
            persistSelectedIntent(intent, always);
            Intents.startActivityFixingIntent(this, intent);
            finish();
        }
    }

    private void persistSelectedIntent(Intent intent, boolean alwaysCheck) {
        if (intent.getComponent() == null) {
            return;
        }
        ContentValues values = new ContentValues(4);
        values.put(HOST, intent.getData().getHost());
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

        history.add(intent.getComponent().getPackageName());
        history.save(this);
    }

    @Override
    public boolean onItemLongClick(View view, int position, long id) {
        ResolveInfo ri = adapter.displayResolveInfoForPosition(position, true).ri;
        showAppDetails(ri);
        return true;
    }

    @SuppressWarnings("deprecation")
    private void showAppDetails(ResolveInfo ri) {
        Intent in = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", ri.activityInfo.packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivity(in);
    }

    private static class LoadIconIntoViewTask extends AsyncTask<DisplayResolveInfo, Void, Drawable> {
        private final ImageView target;
        private final IconLoader iconLoader;

        LoadIconIntoViewTask(IconLoader iconLoader, ImageView target) {
            this.iconLoader = iconLoader;
            this.target = target;
        }

        @Override
        protected Drawable doInBackground(DisplayResolveInfo... params) {
            final DisplayResolveInfo info = params[0];
            if (info.displayIcon() == null) {
                info.displayIcon(iconLoader.loadFor(info.ri));
            }
            return info.displayIcon();
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            target.setImageDrawable(drawable);
        }
    }
}
