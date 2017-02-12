package com.tasomaniac.openwith.rx;

import static io.reactivex.schedulers.Schedulers.trampoline;

public class ImmediateScheduling extends SchedulingStrategy {

    public ImmediateScheduling() {
        super(trampoline(), trampoline());
    }
}
