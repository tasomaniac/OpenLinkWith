package com.tasomaniac.openwith.preferred;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.resolver.DisplayResolveInfo;
import com.tasomaniac.openwith.resolver.ResolveListAdapter;
import com.tasomaniac.openwith.util.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.COMPONENT;
import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.HOST;
import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.PREFERRED;
import static com.tasomaniac.openwith.data.OpenWithProvider.OpenWithHosts.CONTENT_URI;

public class PreferredAppsActivity extends AppCompatActivity {

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferred_apps);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PackageManager mPm = getPackageManager();

        final Cursor cursor = getContentResolver().query(CONTENT_URI, null, PREFERRED + "=1", null, null);

        if (cursor == null) {
            return;
        }

        final List<DisplayResolveInfo> apps = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {

            final String host = cursor.getString(cursor.getColumnIndex(HOST));
            final String componentString = cursor.getString(cursor.getColumnIndex(COMPONENT));
            Intent intent = new Intent();
            intent.setComponent(ComponentName.unflattenFromString(componentString));

            final ResolveInfo ro = mPm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

            CharSequence roLabel = ro.loadLabel(mPm);
            final DisplayResolveInfo info = new DisplayResolveInfo(ro, roLabel, host, null);
            apps.add(info);
        }

        cursor.close();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));
        final PreferredAppsAdapter adapter = new PreferredAppsAdapter(this, apps);
        recyclerView.setAdapter(adapter);

        adapter.setHeader(new ResolveListAdapter.Header());
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

        @Override
        protected void rebuildList() {

        }

        @Override
        protected ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(mInflater
                    .inflate(R.layout.preferred_header, parent, false));
        }

        @Override
        public ViewHolder onCreateItemViewHolder(ViewGroup viewGroup, int i) {
            final ViewHolder viewHolder = super.onCreateItemViewHolder(viewGroup, i);
            viewHolder.itemView.setMinimumHeight(Utils.dpToPx(mContext.getResources(), 72));
            return viewHolder;
        }
    }
}
