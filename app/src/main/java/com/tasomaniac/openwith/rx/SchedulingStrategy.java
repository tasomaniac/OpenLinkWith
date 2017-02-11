package com.tasomaniac.openwith.rx;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.CompletableTransformer;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleSource;
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
        return new FlowableTransformer<T, T>() {
            @Override
            public Flowable<T> apply(Flowable<T> observable) {
                return observable
                        .subscribeOn(executor)
                        .observeOn(notifier);
            }
        };
    }

    public CompletableTransformer applyToCompletable() {
        return new CompletableTransformer() {
            @Override
            public CompletableSource apply(Completable completable) {
                return completable
                        .subscribeOn(executor)
                        .observeOn(notifier);
            }
        };
    }

    public <T> SingleTransformer<T, T> applyToSingle() {
        return new SingleTransformer<T, T>() {
            @Override
            public SingleSource<T> apply(Single<T> single) {
                return single
                        .subscribeOn(executor)
                        .observeOn(notifier);
            }
        };
    }

    public Disposable runOnNotifier(Runnable runnable) {
        return runOnWorker(runnable, notifier.createWorker());
    }

    public Disposable runOnExecutor(Runnable runnable) {
        return runOnWorker(runnable, executor.createWorker());
    }

    private static Disposable runOnWorker(final Runnable runnable, final Scheduler.Worker worker) {
        return worker.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {
                    worker.dispose();
                }
            }
        });
    }
}
