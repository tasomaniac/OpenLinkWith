package com.tasomaniac.openwith.resolver;

import android.view.View;

public interface ItemClickListener {
    void onItemClick(View view, final int position, long id);

    ItemClickListener EMPTY = new ItemClickListener() {
        @Override
        public void onItemClick(View view, int position, long id) {

        }
    };
}
