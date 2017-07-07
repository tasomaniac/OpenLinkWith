package com.tasomaniac.openwith.browser

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

import com.tasomaniac.openwith.HeaderAdapter
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.resolver.ResolveListAdapter
import com.tasomaniac.openwith.util.Utils

internal class BrowsersAdapter(innerAdapter: ResolveListAdapter) : HeaderAdapter(innerAdapter, R.layout.preferred_header) {

    override fun onBindHeaderViewHolder(holder: HeaderAdapter.HeaderViewHolder) {
        holder.setText(R.string.browser_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return holderWithMinHeight(super.onCreateViewHolder(parent, viewType))
    }

    private fun holderWithMinHeight(viewHolder: RecyclerView.ViewHolder): RecyclerView.ViewHolder {
        viewHolder.itemView.minimumHeight = Utils.dpToPx(viewHolder.itemView.resources, 72)
        return viewHolder
    }

}
