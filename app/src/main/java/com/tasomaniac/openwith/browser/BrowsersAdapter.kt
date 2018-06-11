package com.tasomaniac.openwith.browser

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tasomaniac.openwith.extensions.withMinHeight
import com.tasomaniac.openwith.resolver.DisplayActivityInfo

class BrowsersAdapter(
    private val browsers: List<DisplayActivityInfo>,
    private val preference: BrowserPreferences.Mode,
    private val viewHolderFactory: BrowserViewHolder.Factory,
    private val listener: Listener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder = when (viewType) {
            TYPE_NONE -> NoneViewHolder.create(parent)
            TYPE_ALWAYS_ASK -> AlwaysViewHolder.create(parent)
            else -> viewHolderFactory.createWith(parent)
        }
        return viewHolder.withMinHeight()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        when (holder) {
            is NoneViewHolder -> holder.bind(preference === BrowserPreferences.Mode.None, listener::onNoneClick)
            is AlwaysViewHolder -> holder.bind(
                preference === BrowserPreferences.Mode.AlwaysAsk,
                listener::onAlwaysAskClick
            )
            is BrowserViewHolder -> {
                val info = browsers[position - EXTRA_ITEM_COUNT]
                val selectedBrowser = (preference as? BrowserPreferences.Mode.Browser)?.componentName
                holder.bind(info, selectedBrowser, listener::onBrowserClick)
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
