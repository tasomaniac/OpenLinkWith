package com.tasomaniac.openwith;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

public class HeaderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = Integer.MAX_VALUE;
    private static final int HEADER_COUNT = 1;

    private final RecyclerView.Adapter innerAdapter;
    @LayoutRes private final int headerLayoutRes;

    public HeaderAdapter(RecyclerView.Adapter innerAdapter, int headerLayoutRes) {
        this.innerAdapter = innerAdapter;
        this.headerLayoutRes = headerLayoutRes;
        innerAdapter.registerAdapterDataObserver(new ForwardingDataObserver());
    }

    protected void onBindHeaderViewHolder(SimpleTextViewHolder holder) {
        // no-op
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return SimpleTextViewHolder.Companion.create(parent, headerLayoutRes);
        }
        return innerAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        // the one with the payload is always called.
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        if (position < HEADER_COUNT) {
            onBindHeaderViewHolder((SimpleTextViewHolder) holder);
        } else {
            //noinspection unchecked
            innerAdapter.onBindViewHolder(holder, position - HEADER_COUNT, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return innerAdapter.getItemCount() + HEADER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < HEADER_COUNT) {
            return TYPE_HEADER;
        }
        return super.getItemViewType(position - HEADER_COUNT);
    }

    private class ForwardingDataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            notifyItemRangeChanged(positionStart + 1, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            notifyItemRangeChanged(positionStart + 1, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyItemRangeInserted(positionStart + 1, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyItemRangeRemoved(positionStart + 1, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            if (itemCount == 1) {
                notifyItemMoved(fromPosition + 1, toPosition + 1);
            }
        }
    }
}
