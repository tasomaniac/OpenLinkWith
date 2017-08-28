package com.tasomaniac.openwith.browser

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.resolver.DisplayResolveInfo
import com.tasomaniac.openwith.resolver.ItemClickListener
import com.tasomaniac.openwith.resolver.ResolveListAdapter
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import javax.inject.Inject

class PreferredBrowserActivity : DaggerAppCompatActivity(), ItemClickListener {

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

    setSupportActionBar(findViewById(R.id.toolbar))
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    adapter.itemClickListener = this
    recyclerView.adapter = BrowsersAdapter(adapter)

    disposable.add(browserResolver.resolve()
        .subscribe(Consumer { adapter.applications = it }))
  }

  override fun onDestroy() {
    disposable.dispose()
    adapter.itemClickListener = null
    super.onDestroy()
  }

  override fun onItemClick(dri: DisplayResolveInfo) {
    TODO("not implemented")
  }
}
