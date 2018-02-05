package com.tasomaniac.openwith.resolver;

public interface ItemClickListener {
    void onItemClick(DisplayActivityInfo activityInfo);

    ItemClickListener EMPTY = activityInfo -> {
    };
}
