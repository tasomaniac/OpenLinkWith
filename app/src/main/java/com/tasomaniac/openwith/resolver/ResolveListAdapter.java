package com.tasomaniac.openwith.resolver;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tasomaniac.openwith.IconLoader;
import com.tasomaniac.openwith.R;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ResolveListAdapter extends RecyclerView.Adapter<ResolveListAdapter.ViewHolder> {

    private static final int TYPE_ITEM = 2;
    private static final int TYPE_HEADER = 1;

    private final IconLoader iconLoader;
    private final Intent sourceIntent;

    protected final List<DisplayResolveInfo> mList = new ArrayList<>();

    private boolean displayExtendedInfo = false;
    private boolean hasHeader = false;
    private boolean selectionEnabled = false;
    private int checkedItemPosition = RecyclerView.NO_POSITION;

    private ItemClickListener itemClickListener;
    private ItemLongClickListener itemLongClickListener;

    public ResolveListAdapter(IconLoader iconLoader) {
        this(iconLoader, null);
    }

    @Inject
    public ResolveListAdapter(IconLoader iconLoader, Intent sourceIntent) {
        this.iconLoader = iconLoader;
        this.sourceIntent = sourceIntent;
    }

    @Override
    public int getItemCount() {
        int result = mList.size();
        result += getHeadersCount();
        return result;
    }

    public DisplayResolveInfo getItem(int position) {
        position -= getHeadersCount();
        if (position < 0) {
            position = 0;
        }
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    protected int getHeadersCount() {
        return hasHeader ? 1 : 0;
    }

    public void setDisplayExtendedInfo(boolean displayExtendedInfo) {
        this.displayExtendedInfo = displayExtendedInfo;
    }

    public void setDisplayHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    @Override
    public int getItemViewType(int position) {
        if (hasHeader && position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    protected ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType) {
        View headerView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.resolver_different_item_header, parent, false);
        return new ViewHolder(headerView);
    }

    protected void onBindHeaderViewHolder(ViewHolder holder) {
        // no-op
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (TYPE_HEADER == viewType) {
            return onCreateHeaderViewHolder(parent, viewType);
        }
        if (TYPE_ITEM == viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.resolve_list_item, parent, false);
            return new ViewHolder(itemView);
        }
        throw new IllegalStateException("Unknown viewType: + " + viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);

        boolean checked = position == checkedItemPosition;
        holder.itemView.setActivated(checked);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (TYPE_HEADER == getItemViewType(position)) {
            onBindHeaderViewHolder(holder);
            return;
        }

        final DisplayResolveInfo info = getItem(position);

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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                itemClickListener.onItemClick(getItem(adapterPosition));
                setItemChecked(adapterPosition);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return itemLongClickListener != null
                        && itemLongClickListener.onItemLongClick(v, holder.getAdapterPosition(), holder.getItemId());
            }
        });
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text1)
        public TextView text;
        @Nullable
        @BindView(R.id.text2)
        TextView text2;
        @Nullable
        @BindView(R.id.icon)
        ImageView icon;

        public ViewHolder(View view) {
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
