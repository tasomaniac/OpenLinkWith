package com.tasomaniac.openwith.redirect

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.tasomaniac.android.widget.DelayedProgressBar
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.resolver.ResolverActivity
import com.tasomaniac.openwith.rx.SchedulingStrategy
import com.tasomaniac.openwith.util.Urls.fixUrls
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.MaybeTransformer
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import okhttp3.HttpUrl
import javax.inject.Inject

class RedirectFixActivity : DaggerAppCompatActivity() {

    @Inject lateinit var browserIntentChecker: BrowserIntentChecker
    @Inject lateinit var redirectFixer: RedirectFixer
    @Inject lateinit var schedulingStrategy: SchedulingStrategy

    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.resolver_activity)

        val progress = findViewById<DelayedProgressBar>(R.id.resolver_progress)
        progress.show(true)

        val source = intent
        source.component = null
        disposable = Single.just(source)
            .filter { browserIntentChecker.hasOnlyBrowsers(it) }
            .compose(redirectTransformer)
            .map { source.withUrl(it) }
            .defaultIfEmpty(source)
            .compose(schedulingStrategy.forMaybe())
            .subscribe { intent ->
                intent.component = ComponentName(this, ResolverActivity::class.java)
                startActivity(intent)
                finish()
            }
    }

    private val redirectTransformer = MaybeTransformer<Intent, HttpUrl> { source ->
        source
            .map { it.toHttpUrl() }
            .flatMap { httpUrl ->
                redirectFixer
                    .followRedirects(httpUrl)
                    .toMaybe()
            }
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }

    companion object {

        @JvmStatic
        fun createIntent(context: Context, foundUrl: String): Intent {
            return Intent(context, RedirectFixActivity::class.java)
                .setAction(Intent.ACTION_VIEW)
                .setData(Uri.parse(fixUrls(foundUrl)))
        }

        private fun Intent.withUrl(url: HttpUrl) = setData(Uri.parse(url.toString()))

        private fun Intent.toHttpUrl() = HttpUrl.parse(dataString)

    }
}
