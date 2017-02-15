package com.tasomaniac.openwith.resolver;

import android.support.annotation.Nullable;

import com.tasomaniac.openwith.PerActivity;

import javax.inject.Inject;

@PerActivity
class ViewState {

    @Nullable DisplayResolveInfo lastSelected;
    @Nullable DisplayResolveInfo filteredItem;

    DisplayResolveInfo checkedItem() {
        if (filteredItem != null) {
            return filteredItem;
        }
        if (lastSelected != null) {
            return lastSelected;
        }
        throw new IllegalStateException("Either of the items should be non-null before requesting checked item");
    }

    boolean shouldUseAlwaysOption() {
        return filteredItem == null;
    }

    @Inject
    ViewState() {
    }
}
