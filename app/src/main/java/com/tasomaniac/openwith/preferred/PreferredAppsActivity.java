package com.tasomaniac.openwith.preferred;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.data.Analytics;
import com.tasomaniac.openwith.resolver.DisplayResolveInfo;
import com.tasomaniac.openwith.resolver.ItemClickListener;
import com.tasomaniac.openwith.resolver.ResolveListAdapter;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.simonvt.schematic.Cursors;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerAppCompatActivity;

import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.COMPONENT;
import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.HOST;
import static com.tasomaniac.openwith.data.OpenWithProvider.OpenWithHosts.CONTENT_URI_PREFERRED;
import static com.tasomaniac.openwith.data.OpenWithProvider.OpenWithHosts.withHost;

public class PreferredAppsActivity extends DaggerAppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ItemClickListener,
        AppRemoveDialogFragment.Callbacks {

    @Inject Analytics analytics;
    @Inject ResolveListAdapter adapter;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferred_apps);
        ButterKnife.bind(this);

        analytics.sendScreenView("Preferred Apps");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter.setItemClickListener(this);
        recyclerView.setAdapter(new PreferredAppsAdapter(adapter));

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.setItemClickListener(null);
    }

    @Override
    public void onItemClick(DisplayResolveInfo info) {
        AppRemoveDialogFragment.newInstance(info)
                .show(getSupportFragmentManager(), AppRemoveDialogFragment.class.getSimpleName());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, CONTENT_URI_PREFERRED, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        PackageManager mPm = getPackageManager();

        List<DisplayResolveInfo> apps = new ArrayList<>(data.getCount());
        while (data.moveToNext()) {
            final String host = Cursors.getString(data, HOST);
            final String componentString = Cursors.getString(data, COMPONENT);

            Intent intent = new Intent();
            intent.setComponent(ComponentName.unflattenFromString(componentString));

            final ResolveInfo resolveInfo = mPm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

            if (resolveInfo != null) {
                CharSequence roLabel = resolveInfo.loadLabel(mPm);
                final DisplayResolveInfo info = new DisplayResolveInfo(resolveInfo, roLabel, host);
                apps.add(info);
            }
        }
        adapter.setApplications(apps);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.setApplications(Collections.emptyList());
    }

    @Override
    public void onAppRemoved(DisplayResolveInfo info) {
        getContentResolver().delete(withHost(info.extendedInfo().toString()), null, null);

        notifyItemRemoval(info);

        analytics.sendEvent(
                "Preferred",
                "Removed",
                info.displayLabel().toString()
        );
    }

    private void notifyItemRemoval(DisplayResolveInfo info) {
        recyclerView.postDelayed(() -> {
            adapter.remove(info);

            notifyHeaderChanged();
        }, 300);
    }

    private void notifyHeaderChanged() {
        recyclerView.postDelayed(() -> recyclerView.getAdapter().notifyItemChanged(0), 200);
    }

}
