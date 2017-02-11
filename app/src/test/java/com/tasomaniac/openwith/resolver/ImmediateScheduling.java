package com.tasomaniac.openwith.resolver;

import com.tasomaniac.openwith.rx.SchedulingStrategy;

import io.reactivex.schedulers.Schedulers;

public class ImmediateScheduling extends SchedulingStrategy {

    public ImmediateScheduling() {
        super(Schedulers.trampoline(), Schedulers.trampoline());
    }
}
