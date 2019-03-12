package com.tasomaniac.openwith.rx

import io.reactivex.CompletableTransformer
import io.reactivex.FlowableTransformer
import io.reactivex.MaybeTransformer
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.SingleTransformer
import io.reactivex.disposables.Disposable

class SchedulingStrategy(private val executor: Scheduler, private val notifier: Scheduler) {

    fun <T> forObservable() = ObservableTransformer<T, T> { observable ->
        observable
            .subscribeOn(executor)
            .observeOn(notifier)
    }

    fun <T> forFlowable() = FlowableTransformer<T, T> { flowable ->
        flowable
            .subscribeOn(executor)
            .observeOn(notifier)
    }

    fun <T> forMaybe() = MaybeTransformer<T, T> { maybe ->
        maybe
            .subscribeOn(executor)
            .observeOn(notifier)
    }

    fun forCompletable() = CompletableTransformer { completable ->
        completable
            .subscribeOn(executor)
            .observeOn(notifier)
    }

    fun <T> forSingle() = SingleTransformer<T, T> { single ->
        single
            .subscribeOn(executor)
            .observeOn(notifier)
    }

    fun runOnNotifier(runnable: Runnable): Disposable {
        return runOnWorker(runnable, notifier.createWorker())
    }

    fun runOnExecutor(runnable: Runnable): Disposable {
        return runOnWorker(runnable, executor.createWorker())
    }

    private fun runOnWorker(runnable: Runnable, worker: Scheduler.Worker): Disposable {
        return worker.schedule {
            try {
                runnable.run()
            } finally {
                worker.dispose()
            }
        }
    }
}
