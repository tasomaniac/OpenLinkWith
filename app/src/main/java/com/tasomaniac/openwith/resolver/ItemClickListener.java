package com.tasomaniac.openwith.resolver;

public interface ItemClickListener {
    void onItemClick(DisplayResolveInfo dri);

    ItemClickListener EMPTY = new ItemClickListener() {
        @Override
        public void onItemClick(DisplayResolveInfo dri) {

        }
    };
}
