package com.tasomaniac.openwith.rx;

import io.reactivex.CompletableTransformer;
import io.reactivex.FlowableTransformer;
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

    public <T> FlowableTransformer<T, T> apply() {
        return flowable -> flowable
                .subscribeOn(executor)
                .observeOn(notifier);
    }

    public CompletableTransformer applyToCompletable() {
        return completable -> completable
                .subscribeOn(executor)
                .observeOn(notifier);
    }

    public <T> SingleTransformer<T, T> applyToSingle() {
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
