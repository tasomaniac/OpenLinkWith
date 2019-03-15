package com.tasomaniac.openwith.browser

import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import com.tasomaniac.openwith.HeaderAdapter
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.SimpleTextViewHolder
import com.tasomaniac.openwith.browser.resolver.BrowserResolver
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.resolver.DisplayActivityInfo
import com.tasomaniac.openwith.extensions.componentName
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_preferred_apps.recycler_view as recyclerView

class PreferredBrowserActivity : DaggerAppCompatActivity(), BrowsersAdapter.Listener {

    @Inject lateinit var analytics: Analytics
    @Inject lateinit var viewHolderFactory: BrowserViewHolder.Factory
    @Inject lateinit var browserResolver: BrowserResolver
    @Inject lateinit var browserPreferences: BrowserPreferences

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
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val browsersAdapter = BrowsersAdapter(browsers, browserPreferences.mode, viewHolderFactory, listener = this)
        recyclerView.adapter = HeaderAdapter(
            browsersAdapter,
            { viewGroup -> SimpleTextViewHolder.create(viewGroup, R.layout.preferred_header) },
            { setText(R.string.browser_description) }
        )
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    override fun onBrowserClick(displayResolveInfo: DisplayActivityInfo) {
        val browserMode = BrowserPreferences.Mode.Browser(
            displayResolveInfo.displayLabel.toString(),
            displayResolveInfo.activityInfo.componentName()
        )
        browserPreferences.mode = browserMode
        analytics.sendEvent("Preference", "Browser Mode", browserMode.value)
        analytics.sendEvent("Preference", "Selected Browser", browserMode.componentName.packageName)
        finish()
    }

    override fun onNoneClick() {
        browserPreferences.mode = BrowserPreferences.Mode.None
        analytics.sendEvent("Preference", "Browser Mode", BrowserPreferences.Mode.None.value)
        finish()
    }

    override fun onAlwaysAskClick() {
        browserPreferences.mode = BrowserPreferences.Mode.AlwaysAsk
        analytics.sendEvent("Preference", "Browser Mode", BrowserPreferences.Mode.AlwaysAsk.value)
        finish()
    }
}
