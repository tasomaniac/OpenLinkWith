package com.tasomaniac.openwith.preferred

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tasomaniac.openwith.HeaderAdapter
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.SimpleTextViewHolder
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.data.PreferredApp
import com.tasomaniac.openwith.data.PreferredAppDao
import com.tasomaniac.openwith.resolver.ApplicationViewHolder
import com.tasomaniac.openwith.resolver.DisplayActivityInfo
import com.tasomaniac.openwith.resolver.IconLoader
import com.tasomaniac.openwith.resolver.ItemClickListener
import com.tasomaniac.openwith.rx.SchedulingStrategy
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_preferred_apps.recycler_view as recyclerView

class PreferredAppsActivity : DaggerAppCompatActivity(), ItemClickListener, AppRemoveDialogFragment.Callbacks {

    @Inject lateinit var analytics: Analytics
    @Inject lateinit var appDao: PreferredAppDao
    @Inject lateinit var scheduling: SchedulingStrategy
    @Inject lateinit var iconLoader: IconLoader
    @Inject lateinit var adapter: PreferredAppsAdapter

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferred_apps)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        adapter.itemClickListener = this
        recyclerView.adapter = wrapWithHeader(adapter)

        appDao.allPreferredApps()
            .map(::onLoadFinished)
            .compose(scheduling.forFlowable())
            .subscribe(adapter::submitList)
            .addTo(disposables)

        if (savedInstanceState == null) {
            analytics.sendScreenView("Preferred Apps")
        }
    }

    private fun wrapWithHeader(adapter: PreferredAppsAdapter): RecyclerView.Adapter<RecyclerView.ViewHolder> {
        return HeaderAdapter<ApplicationViewHolder, SimpleTextViewHolder>(
            adapter,
            { viewGroup -> SimpleTextViewHolder.create(viewGroup, R.layout.preferred_header) },
            { setText(if (adapter.itemCount == 0) R.string.desc_preferred_empty else R.string.desc_preferred) }
        )
    }

    override fun onDestroy() {
        adapter.itemClickListener = null
        disposables.clear()
        super.onDestroy()
    }

    override fun onItemClick(activityInfo: DisplayActivityInfo) {
        AppRemoveDialogFragment.newInstance(activityInfo)
            .show(supportFragmentManager, AppRemoveDialogFragment::class.java.simpleName)
    }

    private fun onLoadFinished(preferredApps: List<PreferredApp>): List<DisplayActivityInfo> {
        return preferredApps.mapNotNull { app ->
            val intent = Intent().setComponent(app.componentName)
            packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.let {
                DisplayActivityInfo(
                    it.activityInfo,
                    it.loadLabel(packageManager),
                    app.host,
                    iconLoader.loadFor(it.activityInfo)
                )
            }
        }
    }

    override fun onAppRemoved(info: DisplayActivityInfo) {
        Completable
            .fromAction { appDao.deleteHost(info.extendedInfo.toString()) }
            .compose(scheduling.forCompletable())
            .subscribe {
                notifyHeaderChanged()

                analytics.sendEvent(
                    category = "Preferred",
                    action = "Removed",
                    label = info.displayLabel.toString()
                )
            }
            .addTo(disposables)
    }

    private fun notifyHeaderChanged() {
        recyclerView.postDelayed(delayInMillis = 300) {
            recyclerView.adapter!!.notifyItemChanged(0)
        }
    }
}
