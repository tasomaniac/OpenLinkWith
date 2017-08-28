package com.tasomaniac.openwith.resolver;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

public class ResolveListAdapter extends RecyclerView.Adapter<ApplicationViewHolder> {

    private List<DisplayActivityInfo> mList = Collections.emptyList();
    private boolean displayExtendedInfo = false;
    private boolean selectionEnabled = false;
    private int checkedItemPosition = RecyclerView.NO_POSITION;

    private ItemClickListener itemClickListener;
    private ItemLongClickListener itemLongClickListener;

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setDisplayExtendedInfo(boolean displayExtendedInfo) {
        this.displayExtendedInfo = displayExtendedInfo;
    }

    @NonNull @Override
    public ApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ApplicationViewHolder.Companion.create(parent, iconLoader, displayExtendedInfo);
    }

    @Override
    public void onBindViewHolder(ApplicationViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);

        boolean checked = position == checkedItemPosition;
        holder.itemView.setActivated(checked);
    }

    @Override
    public void onViewRecycled(ApplicationViewHolder holder) {
        holder.unbind();
    }

    @Override
    public void onBindViewHolder(ApplicationViewHolder holder, int position) {
        holder.bind(mList.get(position), dri -> {
            itemClickListener.onItemClick(dri);
            setItemChecked(holder.getAdapterPosition());
        }, itemLongClickListener);
    }

    public void setItemClickListener(@Nullable ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener == null ? ItemClickListener.EMPTY : itemClickListener;
    }

    void setItemLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    int getCheckedItemPosition() {
        return checkedItemPosition;
    }

    void setSelectionEnabled(boolean selectionEnabled) {
        this.selectionEnabled = selectionEnabled;
    }

    void setItemChecked(int position) {
        if (!selectionEnabled) {
            return;
        }

        notifyItemChanged(position, true);
        notifyItemChanged(checkedItemPosition, false);

        checkedItemPosition = position;
    }

    public void setApplications(List<DisplayActivityInfo> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void remove(DisplayActivityInfo item) {
        int position = mList.indexOf(item);
        mList.remove(item);
        notifyItemRemoved(position);
    }
}
