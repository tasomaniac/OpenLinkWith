package com.tasomaniac.openwith.rx;

import io.reactivex.CompletableTransformer;
import io.reactivex.FlowableTransformer;
import io.reactivex.MaybeTransformer;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.SingleTransformer;
import io.reactivex.disposables.Disposable;

public class SchedulingStrategy {

    private final Scheduler executor;
    private final Scheduler notifier;

    public SchedulingStrategy(Scheduler executor, Scheduler notifier) {
        this.executor = executor;
        this.notifier = notifier;
    }

    public <T> ObservableTransformer<T, T> forObservable() {
        return observable -> observable
                .subscribeOn(executor)
                .observeOn(notifier);
    }

    public <T> FlowableTransformer<T, T> forFlowable() {
        return flowable -> flowable
                .subscribeOn(executor)
                .observeOn(notifier);
    }

    public <T> MaybeTransformer<T, T> forMaybe() {
        return maybe -> maybe
                .subscribeOn(executor)
                .observeOn(notifier);
    }

    public CompletableTransformer forCompletable() {
        return completable -> completable
                .subscribeOn(executor)
                .observeOn(notifier);
    }

    public <T> SingleTransformer<T, T> forSingle() {
        return single -> single
                .subscribeOn(executor)
                .observeOn(notifier);
    }

    public Disposable runOnNotifier(Runnable runnable) {
        return runOnWorker(runnable, notifier.createWorker());
    }

    public Disposable runOnExecutor(Runnable runnable) {
        return runOnWorker(runnable, executor.createWorker());
    }

    private static Disposable runOnWorker(final Runnable runnable, final Scheduler.Worker worker) {
        return worker.schedule(() -> {
            try {
                runnable.run();
            } finally {
                worker.dispose();
            }
        });
    }
}
