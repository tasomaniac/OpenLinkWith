package com.tasomaniac.openwith.preferred;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.tasomaniac.openwith.IconLoader;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.resolver.ResolveListAdapter;
import com.tasomaniac.openwith.util.Utils;

class PreferredAppsAdapter extends ResolveListAdapter {

    PreferredAppsAdapter(IconLoader iconLoader) {
        super(iconLoader);
        setDisplayHeader(true);
        setDisplayExtendedInfo(true);
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
