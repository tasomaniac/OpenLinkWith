package com.tasomaniac.openwith.preferred;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tasomaniac.openwith.R;
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

public class PreferredAppsActivity extends AppCompatActivity implements ItemClickSupport.OnItemClickListener {

    @Bind(R.id.recycler_view)
    TwoWayView recyclerView;
    private PreferredAppsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferred_apps);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PackageManager mPm = getPackageManager();

        final Cursor cursor = getContentResolver().query(CONTENT_URI_PREFERRED, null, null, null, null);

        if (cursor == null) {
            return;
        }

        List<DisplayResolveInfo> apps = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {

            final int id = cursor.getInt(cursor.getColumnIndex(ID));
            final String host = cursor.getString(cursor.getColumnIndex(HOST));
            final String componentString = cursor.getString(cursor.getColumnIndex(COMPONENT));

            Intent intent = new Intent();
            intent.setComponent(ComponentName.unflattenFromString(componentString));

            final ResolveInfo ro = mPm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

            CharSequence roLabel = ro.loadLabel(mPm);
            final DisplayResolveInfo info = new DisplayResolveInfo(id, ro, roLabel, host, null);
            apps.add(info);
        }

        cursor.close();

        recyclerView.setLayoutManager(new ListLayoutManager(this, VERTICAL));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setItemAnimator(new SlideInRightAnimator());
        adapter = new PreferredAppsAdapter(this, apps);
        recyclerView.setAdapter(adapter);
        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(this);

        adapter.setHeader(new ResolveListAdapter.Header());
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
