package com.tasomaniac.openwith.browser

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import com.tasomaniac.openwith.resolver.DisplayResolveInfo
import com.tasomaniac.openwith.rx.SchedulingStrategy
import com.tasomaniac.openwith.util.ResolverInfos
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.*
import javax.inject.Inject

internal class BrowserResolver @Inject
constructor(private val packageManager: PackageManager,
            private val schedulingStrategy: SchedulingStrategy) {

    private var data: Data? = null
    private var listener = Listener.NO_OP
    private var disposable: Disposable? = null

    fun bind(listener: Listener) {
        this.listener = listener

        val data = data
        if (data != null) {
            listener.onIntentResolved(data)
        } else {
            resolve()
        }
    }

    fun unbind() {
        this.listener = Listener.NO_OP
    }

    fun resolve() {
        disposable = Observable.fromCallable<Data>({ this.doResolve() })
                .compose(schedulingStrategy.apply<Data>())
                .subscribe { data ->
                    this.data = data
                    listener.onIntentResolved(data)
                }
    }

    fun release() {
        disposable?.dispose()
    }

    fun doResolve(): Data {
        val flag = if (SDK_INT >= M) PackageManager.MATCH_ALL else PackageManager.MATCH_DEFAULT_ONLY
        val currentResolveList = ArrayList<ResolveInfo>()
        //                new ArrayList<>(packageManager.queryIntentActivities(sourceIntent, flag));
        addBrowsersToList(currentResolveList)

        val resolved = groupResolveList(currentResolveList)
        return Data(resolved)
    }

    private fun groupResolveList(currentResolveList: List<ResolveInfo>): List<DisplayResolveInfo> {
        if (currentResolveList.isEmpty()) {
            return emptyList()
        }
        return currentResolveList.map { DisplayResolveInfo(it, "", "") }
    }

    private fun addBrowsersToList(list: MutableList<ResolveInfo>) {
        val initialSize = list.size

        for (browser in queryBrowsers()) {
            var browserFound = false

            for (i in 0..initialSize - 1) {
                val info = list[i]

                if (ResolverInfos.equals(info, browser)) {
                    browserFound = true
                    break
                }
            }

            if (!browserFound) {
                list.add(browser)
            }
        }
    }

    private fun queryBrowsers(): List<ResolveInfo> {
        val browserIntent = Intent()
                .setAction(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.parse("http:"))
        return packageManager.queryIntentActivities(browserIntent, 0)
    }

    data class Data(val resolved: List<DisplayResolveInfo>) {

        val isEmpty: Boolean
            get() = totalCount() == 0

        fun totalCount(): Int {
            return resolved.size
        }
    }

    interface Listener {

        fun onIntentResolved(data: Data)

        companion object {

            val NO_OP = object : Listener {
                override fun onIntentResolved(data: Data) {
                    // no-op
                }
            }
        }
    }

}
