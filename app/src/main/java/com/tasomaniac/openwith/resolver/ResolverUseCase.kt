package com.tasomaniac.openwith.resolver

import android.content.Intent
import android.net.Uri
import com.tasomaniac.openwith.data.PreferredApp
import com.tasomaniac.openwith.data.PreferredAppDao
import com.tasomaniac.openwith.resolver.preferred.PreferredResolver
import com.tasomaniac.openwith.rx.SchedulingStrategy
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

// TODO wrap this into ViewModel to retain over config change
internal class ResolverUseCase @Inject constructor(
    private val sourceIntent: Intent,
    private val preferredResolver: PreferredResolver,
    private val intentResolver: IntentResolver,
    private val history: ChooserHistory,
    private val dao: PreferredAppDao,
    private val scheduling: SchedulingStrategy
) {

    private var disposable: Disposable? = null
    private var listener: Listener? = null

    fun bind(listener: Listener) {
        this.listener = listener
        val uri = sourceIntent.data!!
        disposable = preferredResolver.resolve(uri)
            .compose(scheduling.forMaybe())
            .subscribeBy(
                onSuccess = { (app, info) ->
                    intentResolver.lastChosenComponent = app.componentName
                    listener.onPreferredResolved(uri, app, info)
                    intentResolver.bind(listener)
                },
                onError = Timber::e,
                onComplete = { intentResolver.bind(listener) }
            )
    }

    fun unbind() {
        intentResolver.unbind()
    }

    fun release() {
        intentResolver.release()
        disposable?.dispose()
    }

    fun resolve() {
        intentResolver.resolve()
    }

    fun persistSelectedIntent(intent: Intent, alwaysCheck: Boolean) {
        val component = intent.component ?: return

        val preferredApp = PreferredApp(
            host = intent.data!!.host!!,
            component = component.flattenToString(),
            preferred = alwaysCheck
        )
        Completable
            .fromAction { dao.insert(preferredApp) }
            .compose(scheduling.forCompletable())
            .subscribe()

        history.add(component.packageName)
        history.save()
    }

    fun deleteFailedHost(uri: Uri) {
        Completable.fromAction { dao.deleteHost(uri.host!!) }
            .compose(scheduling.forCompletable())
            .subscribe()
    }

    interface Listener : IntentResolver.Listener {
        fun onPreferredResolved(
            uri: Uri,
            preferredApp: PreferredApp,
            displayActivityInfo: DisplayActivityInfo
        )
    }
}
