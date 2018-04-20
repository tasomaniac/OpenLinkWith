package com.tasomaniac.openwith.browser

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.tasomaniac.openwith.extensions.withMinHeight
import com.tasomaniac.openwith.resolver.ApplicationViewHolder
import com.tasomaniac.openwith.resolver.DisplayActivityInfo
import com.tasomaniac.openwith.resolver.ItemClickListener

class BrowsersAdapter(
    private val browsers: List<DisplayActivityInfo>,
    private val viewHolderFactory: ApplicationViewHolder.Factory,
    private val listener: Listener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder = when (viewType) {
            TYPE_NONE -> NoneViewHolder.create(parent)
            TYPE_ALWAYS_ASK -> AlwaysViewHolder.create(parent)
            else -> viewHolderFactory.createWith(parent, displaySubtext = false)
        }
        return viewHolder.withMinHeight()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        when (holder) {
            is NoneViewHolder -> holder.bind(listener::onNoneClick)
            is AlwaysViewHolder -> holder.bind(listener::onAlwaysAskClick)
            is ApplicationViewHolder -> {
                val info = browsers[position - EXTRA_ITEM_COUNT]
                holder.bind(info, ItemClickListener {
                    listener.onBrowserClick(it)
                })
            }
            else -> throw IllegalStateException("Unknown holder at position: $position")
        }

    override fun getItemCount() = browsers.size + EXTRA_ITEM_COUNT

    override fun getItemViewType(position: Int) = when (position) {
        0 -> TYPE_NONE
        1 -> TYPE_ALWAYS_ASK
        else -> TYPE_BROWSER
    }

    companion object {
        private const val TYPE_ALWAYS_ASK = 1
        private const val TYPE_NONE = 2
        private const val TYPE_BROWSER = 3

        private const val EXTRA_ITEM_COUNT = 2
    }

    interface Listener {
        fun onBrowserClick(displayResolveInfo: DisplayActivityInfo)
        fun onNoneClick()
        fun onAlwaysAskClick()
    }
}
