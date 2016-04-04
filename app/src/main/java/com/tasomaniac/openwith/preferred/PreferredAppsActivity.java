package com.tasomaniac.openwith.preferred;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tasomaniac.openwith.Analytics;
import com.tasomaniac.openwith.App;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.misc.DividerItemDecoration;
import com.tasomaniac.openwith.resolver.DisplayResolveInfo;
import com.tasomaniac.openwith.resolver.ResolveListAdapter;
import com.tasomaniac.openwith.util.Utils;

import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.widget.ListLayoutManager;
import org.lucasr.twowayview.widget.TwoWayView;

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
import static org.lucasr.twowayview.TwoWayLayoutManager.Orientation.VERTICAL;

public class PreferredAppsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ItemClickSupport.OnItemClickListener {

    @Bind(R.id.recycler_view)
    TwoWayView recyclerView;
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

        recyclerView.setLayoutManager(new ListLayoutManager(this, VERTICAL));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setItemAnimator(new SlideInRightAnimator());
        adapter = new PreferredAppsAdapter(this, new ArrayList<DisplayResolveInfo>());
        recyclerView.setAdapter(adapter);
        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(this);

        adapter.setHeader(new ResolveListAdapter.Header());

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onItemClick(RecyclerView parent, View view, final int position, long id) {

        final DisplayResolveInfo info = adapter.getItem(position);
        if (info == null) {
            return;
        }

        final String message = getString(R.string.message_remove_preferred,
                info.getDisplayLabel(),
                info.getExtendedInfo(),
                info.getExtendedInfo());
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_remove_preferred)
                .setMessage(Html.fromHtml(message))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        getContentResolver().delete(withId(info.getId()), null, null);
                        notifyItemRemoval(position);

                        analytics.sendEvent("Preferred",
                                "Removed",
                                info.getDisplayLabel().toString());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
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

            final ResolveInfo ro = mPm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

            CharSequence roLabel = ro.loadLabel(mPm);
            final DisplayResolveInfo info = new DisplayResolveInfo(id, ro, roLabel, host);
            apps.add(info);
        }

        adapter.setApplications(apps);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.setApplications(new ArrayList<DisplayResolveInfo>());
    }

    private static class PreferredAppsAdapter extends ResolveListAdapter {

        private Context mContext;
        private LayoutInflater mInflater;

        public PreferredAppsAdapter(Context context, List<DisplayResolveInfo> apps) {
            super(context, null, null, null, null, false);

            mContext = context;
            mInflater = LayoutInflater.from(context);

            mList = apps;
            mShowExtended = true;
        }

        public void remove(DisplayResolveInfo item) {
            mList.remove(item);
        }

        void setApplications(List<DisplayResolveInfo> apps) {
            mList = apps;
            notifyDataSetChanged();
        }

        @Override
        protected void rebuildList() {
        }

        @Override
        protected ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(mInflater
                    .inflate(R.layout.preferred_header, parent, false));
        }

        @Override
        protected void onBindHeaderViewHolder(ViewHolder holder, int position) {
            if (getItemCount() - getHeaderViewsCount() == 0) {
                holder.text.setText(R.string.desc_preferred_empty);
            } else {
                holder.text.setText(R.string.desc_preferred);
            }
        }

        @Override
        public ViewHolder onCreateItemViewHolder(ViewGroup viewGroup, int i) {
            final ViewHolder viewHolder = super.onCreateItemViewHolder(viewGroup, i);
            viewHolder.itemView.setMinimumHeight(Utils.dpToPx(mContext.getResources(), 72));
            return viewHolder;
        }
    }
}
