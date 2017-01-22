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

    PreferredAppsAdapter(Context context, List<DisplayResolveInfo> apps) {
        super(context);

        mList.addAll(apps);
        mShowExtended = true;
    }

    void remove(DisplayResolveInfo item) {
        mList.remove(item);
    }

    void setApplications(List<DisplayResolveInfo> apps) {
        mList.clear();
        mList.addAll(apps);
        notifyDataSetChanged();
    }

    @Override
    protected ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.preferred_header, parent, false));
    }

    @Override
    protected void onBindHeaderViewHolder(ViewHolder holder) {
        if (getItemCount() - getHeadersCount() == 0) {
            holder.text.setText(R.string.desc_preferred_empty);
        } else {
            holder.text.setText(R.string.desc_preferred);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final ViewHolder viewHolder = super.onCreateViewHolder(viewGroup, i);
        viewHolder.itemView.setMinimumHeight(Utils.dpToPx(viewGroup.getResources(), 72));
        return viewHolder;
    }
}
