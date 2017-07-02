package com.tasomaniac.openwith.browser;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.tasomaniac.openwith.HeaderAdapter;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.resolver.ResolveListAdapter;
import com.tasomaniac.openwith.util.Utils;

class BrowsersAdapter extends HeaderAdapter {

    private final ResolveListAdapter innerAdapter;

    BrowsersAdapter(ResolveListAdapter innerAdapter) {
        super(innerAdapter, R.layout.preferred_header);
        this.innerAdapter = innerAdapter;
        innerAdapter.setDisplayExtendedInfo(true);
    }

    @Override
    protected void onBindHeaderViewHolder(HeaderViewHolder holder) {
        if (innerAdapter.getItemCount() == 0) {
            holder.setText(R.string.desc_preferred_empty);
        } else {
            holder.setText(R.string.desc_preferred);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return holderWithMinHeight(super.onCreateViewHolder(parent, viewType));
    }

    private RecyclerView.ViewHolder holderWithMinHeight(RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setMinimumHeight(Utils.dpToPx(viewHolder.itemView.getResources(), 72));
        return viewHolder;
    }

}
