package com.tasomaniac.openwith.resolver

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import com.tasomaniac.openwith.browser.resolver.BrowserHandler
import com.tasomaniac.openwith.rx.SchedulingStrategy
import com.tasomaniac.openwith.util.Intents
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import java.util.ArrayList
import javax.inject.Inject

internal class IntentResolver @Inject constructor(
    private val packageManager: PackageManager,
    private val schedulingStrategy: SchedulingStrategy,
    private val callerPackage: CallerPackage,
    private val resolveListGrouper: ResolveListGrouper,
    private val browserHandlerFactory: BrowserHandler.Factory,
    private val resolveListDuplicateRemover: ResolveListDuplicateRemover,
    val sourceIntent: Intent
) {

    private var result: IntentResolverResult? = null
    private var listener = Listener.NO_OP
    private var disposable: Disposable = Disposables.empty()

    var lastChosenComponent: ComponentName? = null

    fun bind(listener: Listener) {
        this.listener = listener

        if (result == null) {
            resolve()
        } else {
            listener.onIntentResolved(result!!)
        }
    }

    fun unbind() {
        this.listener = Listener.NO_OP
    }

    fun resolve() {
        disposable = Observable
            .fromCallable { doResolve() }
            .compose(schedulingStrategy.forObservable())
            .subscribe { data ->
                result = data
                listener.onIntentResolved(data)
            }
    }

    fun release() {
        disposable.dispose()
    }

    private fun doResolve(): IntentResolverResult {
        val flag = if (SDK_INT >= M) PackageManager.MATCH_ALL else PackageManager.MATCH_DEFAULT_ONLY
        val currentResolveList = ArrayList(packageManager.queryIntentActivities(sourceIntent, flag))

        if (Intents.isHttp(sourceIntent)) {
            browserHandlerFactory.create(currentResolveList).handleBrowsers()
        }

        resolveListDuplicateRemover.process(currentResolveList)
        callerPackage.removeFrom(currentResolveList)

        val resolved = groupResolveList(currentResolveList)
        return IntentResolverResult(resolved, resolveListGrouper.filteredItem, resolveListGrouper.showExtended)
    }

    private fun groupResolveList(currentResolveList: List<ResolveInfo>): List<DisplayActivityInfo> {
        return if (currentResolveList.isEmpty()) {
            emptyList()
        } else {
            resolveListGrouper.groupResolveList(currentResolveList, lastChosenComponent)
        }
    }

    interface Listener {

        fun onIntentResolved(result: IntentResolverResult)

        companion object {

            val NO_OP = object : Listener {
                override fun onIntentResolved(result: IntentResolverResult) = Unit
            }
        }

    }
}
