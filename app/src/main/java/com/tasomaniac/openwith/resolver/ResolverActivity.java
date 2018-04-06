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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.tasomaniac.openwith.HeaderAdapter;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.homescreen.AddToHomeScreenDialogFragment;
import com.tasomaniac.openwith.util.Intents;
import dagger.android.support.DaggerAppCompatActivity;

import javax.inject.Inject;

/**
 * This activity is displayed when the system attempts to start an Intent for
 * which there is more than one matching activity, allowing the user to decide
 * which to go to. It is not normally used directly by application developers.
 */
public class ResolverActivity extends DaggerAppCompatActivity implements
        ItemClickListener,
        ItemLongClickListener,
        ResolverView {

    public static final String EXTRA_ADD_TO_HOME_SCREEN = "EXTRA_ADD_TO_HOME_SCREEN";
    private static final String KEY_CHECKED_POS = "KEY_CHECKED_POS";

    @Inject ResolverPresenter presenter;

    @BindView(R.id.button_always) Button alwaysButton;
    @BindView(R.id.button_once) Button onceButton;

    private ResolveListAdapter adapter;
    private boolean packageMonitorRegistered;
    private final PackageMonitor packageMonitor = new PackageMonitor() {
        @Override
        public void onSomePackagesChanged() {
            handlePackagesChanged();
        }
    };

    private Listener listener = Listener.EMPTY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerPackageMonitor();
    }

    @Override
    protected void onStart() {
        presenter.bind(this, new ResolverNavigation(this));
        super.onStart();
    }

    @Override
    public void displayData(IntentResolverResult result) {
        setContentView(result.getFilteredItem() != null ? R.layout.resolver_list_with_default : R.layout.resolver_list);
        ButterKnife.bind(this);
        setupList(result, result.getShowExtended());
        setupFilteredItem(result.getFilteredItem());
        ResolverDrawerLayout rdl = findViewById(R.id.contentPanel);
        rdl.setOnDismissedListener(this::finish);
    }

    private void setupList(IntentResolverResult data, boolean shouldDisplayExtendedInfo) {
        RecyclerView recyclerView = findViewById(R.id.resolver_list);

        adapter = new ResolveListAdapter();
        adapter.setApplications(data.getResolved());
        adapter.setItemClickListener(this);
        adapter.setItemLongClickListener(this);
        adapter.setDisplayExtendedInfo(shouldDisplayExtendedInfo);

        if (data.getFilteredItem() != null) {
            recyclerView.setAdapter(new HeaderAdapter(adapter, R.layout.resolver_different_item_header));
        } else {
            recyclerView.setAdapter(adapter);
        }
    }

    private void setupFilteredItem(@Nullable DisplayActivityInfo filteredItem) {
        boolean hasFilteredItem = filteredItem != null;
        ImageView iconView = findViewById(R.id.icon);
        if (iconView != null && hasFilteredItem) {
            iconView.setImageDrawable(filteredItem.displayIcon());
        }
        adapter.setSelectionEnabled(!hasFilteredItem);
    }

    @Override
    public void setTitle(String title) {
        TextView titleView = findViewById(R.id.title);
        titleView.setText(title);
    }

    @Override
    public void setupActionButtons() {
        ViewGroup buttonLayout = findViewById(R.id.button_bar);
        buttonLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void enableActionButtons() {
        alwaysButton.setEnabled(true);
        onceButton.setEnabled(true);
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
        listener.onPackagesChanged();
    }

    @Override
    protected void onStop() {
        presenter.unbind(this);
        if (packageMonitorRegistered) {
            packageMonitor.unregister();
            packageMonitorRegistered = false;
        }
        if (!isChangingConfigurations()) {
            presenter.release();
        }
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CHECKED_POS, adapter.getCheckedItemPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int checkedPos = savedInstanceState.getInt(KEY_CHECKED_POS);
        if (checkedPos != RecyclerView.NO_POSITION) {
            if (alwaysButton != null && onceButton != null) {
                enableActionButtons();
            }
            adapter.setItemChecked(checkedPos);
        }
    }

    @Override
    public void onItemClick(DisplayActivityInfo activityInfo) {
        listener.onItemClick(activityInfo);
    }

    @OnClick(R.id.button_always)
    public void onAlwaysButtonClick() {
        listener.onActionButtonClick(true);
    }

    @OnClick(R.id.button_once)
    public void onOnceButtonClick() {
        listener.onActionButtonClick(false);
    }

    @Override
    public void displayAddToHomeScreenDialog(DisplayActivityInfo activityInfo, Intent intent) {
        AddToHomeScreenDialogFragment
                .newInstance(activityInfo, Intents.fixIntents(this, intent))
                .show(getSupportFragmentManager());
    }

    @Override
    public boolean onItemLongClick(DisplayActivityInfo info) {
        navigateToAppDetails(info.packageName());
        return true;
    }

    private void navigateToAppDetails(String packageName) {
        Intent in = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivity(in);
    }

    @Override
    public void toast(@StringRes int titleRes) {
        Toast.makeText(this, titleRes, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setListener(@Nullable Listener listener) {
        this.listener = listener == null ? Listener.EMPTY : listener;
    }

    @Override
    public void dismiss() {
        if (!isFinishing()) {
            finish();
        }
    }

}
