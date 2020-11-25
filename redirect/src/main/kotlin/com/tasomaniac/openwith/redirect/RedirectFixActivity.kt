package com.tasomaniac.openwith.redirect

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.tasomaniac.android.widget.DelayedProgressBar
import com.tasomaniac.openwith.resolver.ResolverActivity
import com.tasomaniac.openwith.rx.SchedulingStrategy
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Maybe
import io.reactivex.MaybeTransformer
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.inject.Inject

class RedirectFixActivity : DaggerAppCompatActivity() {

    @Inject lateinit var browserIntentChecker: BrowserIntentChecker
    @Inject lateinit var redirectFixer: RedirectFixer
    @Inject lateinit var urlFix: UrlFix
    @Inject lateinit var schedulingStrategy: SchedulingStrategy

    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.redirect_activity)

        val progress = findViewById<DelayedProgressBar>(R.id.resolver_progress)
        progress.show(true)

        val source = Intent(intent).apply {
            component = null
        }
        disposable = Single.just(source)
            .filter { browserIntentChecker.hasOnlyBrowsers(it) }
            .map { urlFix.fixUrls(it.dataString!!) }
            .flatMap {
                Maybe.fromCallable<HttpUrl> { it.toHttpUrlOrNull() }
            }
            .compose(redirectTransformer)
            .map(HttpUrl::toString)
            .toSingle(source.dataString!!) // fall-back to original data if anything goes wrong
            .map(urlFix::fixUrls) // fix again after potential redirect
            .map { source.withUrl(it) }
            .compose(schedulingStrategy.forSingle())
            .subscribe { intent ->
                intent.component = ComponentName(this, ResolverActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                startActivity(intent)
                finish()
            }
    }

    private val redirectTransformer = MaybeTransformer<HttpUrl, HttpUrl> { source ->
        source.flatMap { httpUrl ->
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
        fun createIntent(activity: Activity, foundUrl: String): Intent {
            return Intent(activity, RedirectFixActivity::class.java)
                .putExtras(activity.intent)
                .setAction(Intent.ACTION_VIEW)
                .setData(Uri.parse(foundUrl))
        }

        private fun Intent.withUrl(url: String): Intent = setData(Uri.parse(url))
    }
}
