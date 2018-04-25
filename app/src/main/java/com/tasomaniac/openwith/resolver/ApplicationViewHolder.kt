package com.tasomaniac.openwith.resolver

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.extensions.inflate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.resolve_list_item.icon
import kotlinx.android.synthetic.main.resolve_list_item.text1
import kotlinx.android.synthetic.main.resolve_list_item.text2
import javax.inject.Inject

class ApplicationViewHolder private constructor(
    override val containerView: View,
    private val displaySubtext: Boolean
) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    fun bind(
        info: DisplayActivityInfo,
        itemClickListener: ItemClickListener? = null,
        itemLongClickListener: ItemLongClickListener? = null
    ) {
        text1.text = info.displayLabel()
        text2.isVisible = displaySubtext
        text2.text = info.extendedInfo()
        icon.setImageDrawable(info.displayIcon())

        itemView.setOnClickListener {
            itemClickListener?.onItemClick(info)
        }
        itemView.setOnLongClickListener {
            itemLongClickListener?.onItemLongClick(info) ?: false
        }
    }

    class Factory @Inject constructor() {

        private val creator = { view: View, displaySubtext: Boolean ->
            ApplicationViewHolder(view, displaySubtext)
        }

        fun createWith(parent: ViewGroup, displaySubtext: Boolean) =
            creator(parent.inflate(R.layout.resolve_list_item), displaySubtext)
    }
}
