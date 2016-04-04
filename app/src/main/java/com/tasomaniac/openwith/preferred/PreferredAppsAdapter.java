package com.tasomaniac.openwith.preferred;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.resolver.DisplayResolveInfo;
import com.tasomaniac.openwith.resolver.ResolveListAdapter;
import com.tasomaniac.openwith.util.Utils;

import java.util.List;

class PreferredAppsAdapter extends ResolveListAdapter {

    private Context mContext;
    private LayoutInflater mInflater;

    public PreferredAppsAdapter(Context context, List<DisplayResolveInfo> apps) {
        super(context, null, null, null, null, false);

        mContext = context;
        mInflater = LayoutInflater.from(context);

        mList = apps;
        mShowExtended = true;
    }

    public void remove(DisplayResolveInfo item) {
        mList.remove(item);
    }

    void setApplications(List<DisplayResolveInfo> apps) {
        mList = apps;
        notifyDataSetChanged();
    }

    @Override
    protected void rebuildList() {
    }

    @Override
    protected ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.preferred_header, parent, false));
    }

    @Override
    protected void onBindHeaderViewHolder(ViewHolder holder, int position) {
        if (getItemCount() - getHeaderViewsCount() == 0) {
            holder.text.setText(R.string.desc_preferred_empty);
        } else {
            holder.text.setText(R.string.desc_preferred);
        }
    }

    @Override
    public ViewHolder onCreateItemViewHolder(ViewGroup viewGroup, int i) {
        final ViewHolder viewHolder = super.onCreateItemViewHolder(viewGroup, i);
        viewHolder.itemView.setMinimumHeight(Utils.dpToPx(mContext.getResources(), 72));
        return viewHolder;
    }
}
