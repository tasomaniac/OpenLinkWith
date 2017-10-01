package com.tasomaniac.openwith.preferred

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

import com.tasomaniac.openwith.HeaderAdapter
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.SimpleTextViewHolder
import com.tasomaniac.openwith.resolver.ResolveListAdapter
import com.tasomaniac.openwith.util.Utils

class PreferredAppsAdapter(private val innerAdapter: ResolveListAdapter)
  : HeaderAdapter(innerAdapter, R.layout.preferred_header) {

  init {
    innerAdapter.displayExtendedInfo = true
  }

  override fun onBindHeaderViewHolder(holder: SimpleTextViewHolder) {
    if (innerAdapter.itemCount == 0) {
      holder.setText(R.string.preferred_empty_description)
    } else {
      holder.setText(R.string.preferred_description)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
      super.onCreateViewHolder(parent, viewType).withMinHeight()

  private fun RecyclerView.ViewHolder.withMinHeight() = this.apply {
    itemView.minimumHeight = Utils.dpToPx(itemView.resources, 72)
  }

}
