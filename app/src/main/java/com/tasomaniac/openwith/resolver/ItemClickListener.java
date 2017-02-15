package com.tasomaniac.openwith.resolver;

public interface ItemClickListener {
    void onItemClick(DisplayResolveInfo dri);

    ItemClickListener EMPTY = dri -> {
    };
}
