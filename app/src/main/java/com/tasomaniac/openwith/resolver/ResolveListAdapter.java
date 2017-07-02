package com.tasomaniac.openwith.resolver;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tasomaniac.openwith.R;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ResolveListAdapter extends RecyclerView.Adapter<ResolveListAdapter.ViewHolder> {

    private final IconLoader iconLoader;

    private List<DisplayResolveInfo> mList = Collections.emptyList();
    private boolean displayExtendedInfo = false;
    private boolean selectionEnabled = false;
    private int checkedItemPosition = RecyclerView.NO_POSITION;

    private ItemClickListener itemClickListener;
    private ItemLongClickListener itemLongClickListener;

    @Inject
    public ResolveListAdapter(IconLoader iconLoader) {
        this.iconLoader = iconLoader;
    }

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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);

        boolean checked = position == checkedItemPosition;
        holder.itemView.setActivated(checked);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final DisplayResolveInfo info = mList.get(position);

        holder.text.setText(info.displayLabel());
        if (holder.text2 != null) {
            if (displayExtendedInfo) {
                holder.text2.setVisibility(View.VISIBLE);
                holder.text2.setText(info.extendedInfo());
            } else {
                holder.text2.setVisibility(View.GONE);
            }
        }
        if (holder.icon != null) {
            if (info.displayIcon() == null) {
                new LoadIconTask().execute(info);
            }
            holder.icon.setImageDrawable(info.displayIcon());
        }

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            itemClickListener.onItemClick(info);
            setItemChecked(adapterPosition);
        });

        holder.itemView.setOnLongClickListener(v -> itemLongClickListener != null
                && itemLongClickListener.onItemLongClick(info));
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

    public void setApplications(List<DisplayResolveInfo> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void remove(DisplayResolveInfo item) {
        int position = mList.indexOf(item);
        mList.remove(item);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text1)
        TextView text;
        @Nullable
        @BindView(R.id.text2)
        TextView text2;
        @Nullable
        @BindView(R.id.icon)
        ImageView icon;

        public static ViewHolder create(ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.resolve_list_item, parent, false);
            return new ViewHolder(view);
        }

        private ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    private class LoadIconTask extends AsyncTask<DisplayResolveInfo, Void, DisplayResolveInfo> {
        @Override
        protected DisplayResolveInfo doInBackground(DisplayResolveInfo... params) {
            final DisplayResolveInfo info = params[0];
            if (info.displayIcon() == null) {
                info.displayIcon(iconLoader.loadFor(info.resolveInfo()));
            }
            return info;
        }

        @Override
        protected void onPostExecute(DisplayResolveInfo info) {
            notifyDataSetChanged();
        }
    }
}
