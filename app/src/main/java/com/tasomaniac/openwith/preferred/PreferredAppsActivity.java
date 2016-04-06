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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.tasomaniac.openwith.Analytics;
import com.tasomaniac.openwith.App;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.misc.DividerItemDecoration;
import com.tasomaniac.openwith.misc.ItemClickListener;
import com.tasomaniac.openwith.resolver.DisplayResolveInfo;
import com.tasomaniac.openwith.resolver.ResolveListAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;

import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.COMPONENT;
import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.HOST;
import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.ID;
import static com.tasomaniac.openwith.data.OpenWithProvider.OpenWithHosts.CONTENT_URI_PREFERRED;
import static com.tasomaniac.openwith.data.OpenWithProvider.OpenWithHosts.withId;

public class PreferredAppsActivity
        extends AppCompatActivity
        implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ItemClickListener,
        AppRemoveDialogFragment.Callbacks {

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    private PreferredAppsAdapter adapter;

    private Analytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferred_apps);
        ButterKnife.bind(this);

        analytics = App.getApp(this).getAnalytics();
        analytics.sendScreenView("Preferred Apps");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL_LIST
        ));
        recyclerView.setItemAnimator(new SlideInRightAnimator());
        adapter = new PreferredAppsAdapter(this, new ArrayList<DisplayResolveInfo>());
        adapter.setItemClickListener(this);
        recyclerView.setAdapter(adapter);

        adapter.setHeader(new ResolveListAdapter.Header());

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onItemClick(View view, int position, long id) {

        final DisplayResolveInfo info = adapter.getItem(position);
        if (info == null) {
            return;
        }

        AppRemoveDialogFragment.newInstance(info, position)
                .show(getSupportFragmentManager(), AppRemoveDialogFragment.class.getSimpleName());
    }

    private void notifyItemRemoval(final int position) {
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {

                adapter.remove(adapter.getItem(position));
                adapter.notifyItemRemoved(position);

                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyItemChanged(0);
                    }
                }, 200);
            }
        }, 300);
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

            final int id = data.getInt(data.getColumnIndex(ID));
            final String host = data.getString(data.getColumnIndex(HOST));
            final String componentString = data.getString(data.getColumnIndex(COMPONENT));

            Intent intent = new Intent();
            intent.setComponent(ComponentName.unflattenFromString(componentString));

            final ResolveInfo resolveInfo = mPm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

            if (resolveInfo != null) {
                CharSequence roLabel = resolveInfo.loadLabel(mPm);
                final DisplayResolveInfo info = new DisplayResolveInfo(id, resolveInfo, roLabel, host);
                apps.add(info);
            }
        }

        adapter.setApplications(apps);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.setApplications(new ArrayList<DisplayResolveInfo>());
    }

    @Override
    public void onAppRemoved(DisplayResolveInfo info, int position) {
        getContentResolver().delete(withId(info.getId()), null, null);

        notifyItemRemoval(position);

        analytics.sendEvent(
                "Preferred",
                "Removed",
                info.getDisplayLabel().toString()
        );
    }
}
