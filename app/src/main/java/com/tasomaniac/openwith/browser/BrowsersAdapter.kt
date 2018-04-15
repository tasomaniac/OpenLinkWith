package com.tasomaniac.openwith.browser

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.SimpleTextViewHolder
import com.tasomaniac.openwith.resolver.ApplicationViewHolder
import com.tasomaniac.openwith.resolver.DisplayActivityInfo
import com.tasomaniac.openwith.resolver.ItemClickListener
import com.tasomaniac.openwith.resolver.ResolveListAdapter
import com.tasomaniac.openwith.util.Utils

class BrowsersAdapter(
    private val innerAdapter: ResolveListAdapter,
    private val listener: Listener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  init {
    innerAdapter.itemClickListener = ItemClickListener {
      listener.onBrowserClick(it)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val viewHolder = when (viewType) {
      TYPE_NONE, TYPE_ALWAYS_ASK -> SimpleTextViewHolder.create(parent, R.layout.preferred_header)
      else -> innerAdapter.onCreateViewHolder(parent, viewType)
    }
    return viewHolder.withMinHeight()
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (getItemViewType(position)) {
      TYPE_NONE -> {
        (holder as SimpleTextViewHolder).apply {
          itemView.setOnClickListener { listener.onNoneClick() }
          setText(R.string.browser_none)
        }
      }
      TYPE_ALWAYS_ASK -> {
        (holder as SimpleTextViewHolder).apply {
          itemView.setOnClickListener { listener.onAlwaysAskClick() }
          setText(R.string.browser_always_ask)
        }
      }
      else -> {
        innerAdapter.onBindViewHolder(holder as ApplicationViewHolder, position - EXTRA_ITEM_COUNT)
      }
    }
  }

  override fun getItemCount() = innerAdapter.itemCount + EXTRA_ITEM_COUNT

  override fun getItemViewType(position: Int) = when (position) {
    0 -> TYPE_NONE
    1 -> TYPE_ALWAYS_ASK
    else -> innerAdapter.getItemViewType(position - EXTRA_ITEM_COUNT)
  }

  private fun RecyclerView.ViewHolder.withMinHeight() = this.apply {
    itemView.minimumHeight = Utils.dpToPx(itemView.resources, 72)
  }

  companion object {
    private const val TYPE_ALWAYS_ASK = 1
    private const val TYPE_NONE = 2

    private const val EXTRA_ITEM_COUNT = 2
  }

  interface Listener {
      fun onBrowserClick(displayResolveInfo: DisplayActivityInfo)
    fun onNoneClick()
    fun onAlwaysAskClick()
  }
}
