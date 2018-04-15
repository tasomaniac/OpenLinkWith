package com.tasomaniac.openwith.browser

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.tasomaniac.openwith.extensions.withMinHeight
import com.tasomaniac.openwith.resolver.ApplicationViewHolder
import com.tasomaniac.openwith.resolver.DisplayActivityInfo
import com.tasomaniac.openwith.resolver.ItemClickListener
import com.tasomaniac.openwith.resolver.ResolveListAdapter

class BrowsersAdapter(
    private val innerAdapter: ResolveListAdapter,
    private val listener: Listener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        innerAdapter.itemClickListener = ItemClickListener { listener.onBrowserClick(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder = when (viewType) {
            TYPE_NONE -> NoneViewHolder.create(parent)
            TYPE_ALWAYS_ASK -> AlwaysViewHolder.create(parent)
            else -> innerAdapter.onCreateViewHolder(parent, viewType)
        }
        return viewHolder.withMinHeight()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NoneViewHolder -> holder.bind(listener::onNoneClick)
            is AlwaysViewHolder -> holder.bind(listener::onAlwaysAskClick)
            is ApplicationViewHolder -> {
                innerAdapter.onBindViewHolder(holder, position - EXTRA_ITEM_COUNT)
            }
            else -> throw IllegalStateException("Unknown holder at position: $position")
        }
    }

    override fun getItemCount() = innerAdapter.itemCount + EXTRA_ITEM_COUNT

    override fun getItemViewType(position: Int) = when (position) {
        0 -> TYPE_NONE
        1 -> TYPE_ALWAYS_ASK
        else -> innerAdapter.getItemViewType(position - EXTRA_ITEM_COUNT)
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
