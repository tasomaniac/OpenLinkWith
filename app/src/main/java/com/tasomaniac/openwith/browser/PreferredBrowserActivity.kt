package com.tasomaniac.openwith.browser

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.resolver.DisplayResolveInfo
import com.tasomaniac.openwith.resolver.ItemClickListener
import com.tasomaniac.openwith.resolver.ResolveListAdapter
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class PreferredBrowserActivity : DaggerAppCompatActivity(), ItemClickListener {

    @Inject lateinit var analytics: Analytics
    @Inject lateinit var adapter: ResolveListAdapter
    @Inject internal lateinit var browserResolver: BrowserResolver

    @BindView(R.id.recycler_view) lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferred_apps)
        ButterKnife.bind(this)

        analytics.sendScreenView("Browser Apps")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar.setDisplayHomeAsUpEnabled(true)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        adapter.setItemClickListener(this)
        recyclerView.adapter = BrowsersAdapter(adapter)

        adapter.setApplications(browserResolver.doResolve().resolved)
    }

    override fun onDestroy() {
        adapter.setItemClickListener(null)
        super.onDestroy()
    }
    
    override fun onItemClick(dri: DisplayResolveInfo) {
        TODO("not implemented")
    }

    override fun getSupportActionBar(): ActionBar {
        return super.getSupportActionBar()!!
    }
}
