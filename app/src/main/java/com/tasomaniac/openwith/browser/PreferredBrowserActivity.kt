package com.tasomaniac.openwith.browser

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.resolver.ApplicationViewHolder
import com.tasomaniac.openwith.resolver.DisplayActivityInfo
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_preferred_apps.recycler_view
import javax.inject.Inject

class PreferredBrowserActivity : DaggerAppCompatActivity(), BrowsersAdapter.Listener {

    @Inject lateinit var analytics: Analytics
    @Inject lateinit var browserResolver: BrowserResolver
    @Inject lateinit var viewHolderFactory: ApplicationViewHolder.Factory

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferred_apps)

        analytics.sendScreenView("Browser Apps")
        setupToolbar()

        browserResolver.resolve()
            .subscribe(::setupList)
            .addTo(disposable)
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupList(browsers: List<DisplayActivityInfo>) {
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
//        recyclerView.adapter = HeaderAdapter(BrowsersAdapter(adapter, this), R.layout.preferred_header) {
//            setText(R.string.browser_description)
//        }
        recycler_view.adapter = BrowsersAdapter(browsers, viewHolderFactory, listener = this)
    }

    override fun onDestroy() {
        disposable.dispose()
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
