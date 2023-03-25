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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.tasomaniac.openwith.HeaderAdapter;
import com.tasomaniac.openwith.SimpleTextViewHolder;
import com.tasomaniac.openwith.util.IntentFixer;

import javax.annotation.Nullable;
import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

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
    public static final String RESULT_EXTRA_INFO = "RESULT_EXTRA_INFO";
    public static final String RESULT_EXTRA_INTENT = "RESULT_EXTRA_INTENT";

    @Inject ResolverPresenter presenter;
    @Inject ResolveListAdapter adapter;

    private boolean packageMonitorRegistered;
    private final PackageMonitor packageMonitor = new PackageMonitor() {
        @Override
        public void onSomePackagesChanged() {
            handlePackagesChanged();
        }
    };

    private Listener listener = Listener.EMPTY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerPackageMonitor();
    }

    @Override
    protected void onStart() {
        presenter.bind(this, new ResolverNavigation(this));
        super.onStart();
    }

    @SuppressLint("ClickableViewAccessibility") @Override
    public void displayData(IntentResolverResult result) {
        setContentView(result.getFilteredItem() != null ? R.layout.resolver_list_with_default : R.layout.resolver_list);
        setupList(result, result.getShowExtended());
        setupFilteredItem(result.getFilteredItem());
        ResolverDrawerLayout rdl = findViewById(R.id.contentPanel);
        rdl.setOnDismissedListener(this::finish);
        findViewById(R.id.button_always).setOnClickListener(v -> listener.onActionButtonClick(true));
        findViewById(R.id.button_once).setOnClickListener(v -> listener.onActionButtonClick(false));

        if(result.getFilteredItem() != null){
            GestureDetector gDetector = new GestureDetector(getBaseContext(), new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    listener.onActionButtonClick(false);
                    return true;
                }
            });

            findViewById(R.id.preferred_item).setOnTouchListener((v, event) -> gDetector.onTouchEvent(event));
        }
    }

    private void setupList(IntentResolverResult data, boolean shouldDisplayExtendedInfo) {
        RecyclerView recyclerView = findViewById(R.id.resolver_list);

        adapter.submitList(data.getResolved());
        adapter.setItemClickListener(this);
        adapter.setItemLongClickListener(this);
        adapter.setDisplayExtendedInfo(shouldDisplayExtendedInfo);

        if (data.getFilteredItem() != null) {
            recyclerView.setAdapter(new HeaderAdapter<>(
                    adapter,
                    parent -> SimpleTextViewHolder.create(parent, R.layout.resolver_different_item_header)
            ));
        } else {
            recyclerView.setAdapter(adapter);
        }
    }

    private void setupFilteredItem(@Nullable DisplayActivityInfo filteredItem) {
        boolean hasFilteredItem = filteredItem != null;
        ImageView iconView = findViewById(R.id.icon);
        if (iconView != null && hasFilteredItem) {
            iconView.setImageDrawable(filteredItem.getDisplayIcon());
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
        View alwaysButton = findViewById(R.id.button_always);
        if (alwaysButton != null) alwaysButton.setEnabled(true);
        View onceButton = findViewById(R.id.button_once);
        if (onceButton != null) onceButton.setEnabled(true);
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
            enableActionButtons();
            adapter.setCheckedItemPosition(checkedPos);
        }
    }

    @Override
    public void onItemClick(DisplayActivityInfo activityInfo) {
        listener.onItemClick(activityInfo);
    }

    @Override
    public void displayAddToHomeScreenDialog(DisplayActivityInfo activityInfo, Intent intent) {
        Intent data = new Intent();
        data.putExtra(RESULT_EXTRA_INFO, activityInfo);
        data.putExtra(RESULT_EXTRA_INTENT, IntentFixer.fixIntents(this, intent));
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public boolean onItemLongClick(DisplayActivityInfo info) {
        navigateToAppDetails(info.packageName());
        return true;
    }

    private void navigateToAppDetails(String packageName) {
        Intent in = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
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
