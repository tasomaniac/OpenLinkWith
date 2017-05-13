package com.tasomaniac.openwith.rx;

import static io.reactivex.schedulers.Schedulers.trampoline;

public final class ImmediateScheduling extends SchedulingStrategy {

    public ImmediateScheduling() {
        super(trampoline(), trampoline());
    }
}
