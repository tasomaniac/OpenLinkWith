package com.tasomaniac.openwith.browser

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.resolver.DisplayActivityInfo
import com.tasomaniac.openwith.resolver.ResolveListAdapter
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class PreferredBrowserActivity : DaggerAppCompatActivity(), BrowsersAdapter.Listener {

    @Inject lateinit var analytics: Analytics
    @Inject lateinit var adapter: ResolveListAdapter
    @Inject lateinit var browserResolver: BrowserResolver

    @BindView(R.id.recycler_view) lateinit var recyclerView: RecyclerView

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferred_apps)
        ButterKnife.bind(this)

        analytics.sendScreenView("Browser Apps")

        setupToolbar()
        setupList()

        disposable.add(
            browserResolver.resolve()
                .subscribe { list ->
                    adapter.applications = list
                    recyclerView.adapter.notifyDataSetChanged()
                }
        )
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupList() {
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.adapter = BrowsersAdapter(adapter, this)
    }

    override fun onDestroy() {
        disposable.dispose()
        adapter.itemClickListener = null
        super.onDestroy()
    }

    override fun onBrowserClick(displayResolveInfo: DisplayActivityInfo) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onNoneClick() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAlwaysAskClick() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
