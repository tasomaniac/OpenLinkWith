package com.tasomaniac.openwith.resolver;

import com.tasomaniac.openwith.PerActivity;

import javax.inject.Inject;

@PerActivity
class LastSelectedHolder {

    DisplayResolveInfo lastSelected;

    @Inject
    LastSelectedHolder() {
    }
}
