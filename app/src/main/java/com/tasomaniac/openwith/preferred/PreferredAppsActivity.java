package com.tasomaniac.openwith.preferred;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tasomaniac.openwith.HeaderAdapter;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.SimpleTextViewHolder;
import com.tasomaniac.openwith.data.Analytics;
import com.tasomaniac.openwith.data.PreferredApp;
import com.tasomaniac.openwith.data.PreferredAppDao;
import com.tasomaniac.openwith.resolver.DisplayActivityInfo;
import com.tasomaniac.openwith.resolver.IconLoader;
import com.tasomaniac.openwith.resolver.ItemClickListener;
import com.tasomaniac.openwith.rx.SchedulingStrategy;
import dagger.android.support.DaggerAppCompatActivity;
import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import kotlin.Unit;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class PreferredAppsActivity extends DaggerAppCompatActivity implements
        ItemClickListener,
        AppRemoveDialogFragment.Callbacks {

    @Inject Analytics analytics;
    @Inject PreferredAppDao appDao;
    @Inject SchedulingStrategy scheduling;
    @Inject PackageManager packageManager;
    @Inject IconLoader iconLoader;
    @Inject PreferredAppsAdapter adapter;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferred_apps);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter.setItemClickListener(this);
        recyclerView.setAdapter(wrapWithHeader(adapter));

        Disposable disposable = appDao.allPreferredApps()
                .map(this::onLoadFinished)
                .compose(scheduling.forFlowable())
                .subscribe(adapter::submitList);
        disposables.add(disposable);

        if (savedInstanceState == null) {
            analytics.sendScreenView("Preferred Apps");
        }
    }

    private RecyclerView.Adapter<RecyclerView.ViewHolder> wrapWithHeader(PreferredAppsAdapter adapter) {
        return new HeaderAdapter<>(
                adapter,
                viewGroup -> SimpleTextViewHolder.create(viewGroup, R.layout.preferred_header),
                headerViewHolder -> {
                    if (adapter.getItemCount() == 0) {
                        headerViewHolder.setText(R.string.desc_preferred_empty);
                    } else {
                        headerViewHolder.setText(R.string.desc_preferred);
                    }
                    return Unit.INSTANCE;
                }
        );
    }

    @Override
    protected void onDestroy() {
        adapter.setItemClickListener(null);
        disposables.clear();
        super.onDestroy();
    }

    @Override
    public void onItemClick(DisplayActivityInfo activityInfo) {
        AppRemoveDialogFragment.newInstance(activityInfo)
                .show(getSupportFragmentManager(), AppRemoveDialogFragment.class.getSimpleName());
    }

    public List<DisplayActivityInfo> onLoadFinished(List<PreferredApp> preferredApps) {
        List<DisplayActivityInfo> apps = new ArrayList<>(preferredApps.size());
        for (PreferredApp app : preferredApps) {
            Intent intent = new Intent();
            intent.setComponent(ComponentName.unflattenFromString(app.getComponent()));
            final ResolveInfo resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

            if (resolveInfo != null) {
                CharSequence roLabel = resolveInfo.loadLabel(packageManager);
                Drawable icon = iconLoader.loadFor(resolveInfo.activityInfo);
                final DisplayActivityInfo info = new DisplayActivityInfo(resolveInfo.activityInfo, roLabel, app.getHost(), icon);
                apps.add(info);
            }
        }
        return apps;
    }

    @Override
    public void onAppRemoved(DisplayActivityInfo info) {
        Disposable disposable = Completable.fromAction(() -> appDao.deleteHost(info.extendedInfo().toString()))
                .compose(scheduling.forCompletable())
                .subscribe(() -> {
                    notifyHeaderChanged();

                    analytics.sendEvent(
                            "Preferred",
                            "Removed",
                            info.displayLabel().toString()
                    );
                });
        disposables.add(disposable);

    }

    private void notifyHeaderChanged() {
        recyclerView.postDelayed(() -> recyclerView.getAdapter().notifyItemChanged(0), 300);
    }

}
