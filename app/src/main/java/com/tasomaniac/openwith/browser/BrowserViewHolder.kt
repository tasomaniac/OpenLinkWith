package com.tasomaniac.openwith.browser

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.extensions.inflate
import com.tasomaniac.openwith.resolver.DisplayActivityInfo
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.browser_list_item.browserIcon
import kotlinx.android.synthetic.main.browser_list_item.browserSelected
import kotlinx.android.synthetic.main.browser_list_item.browserTitle
import javax.inject.Inject

class BrowserViewHolder private constructor(
    override val containerView: View
) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    fun bind(
        info: DisplayActivityInfo,
        isSelected: Boolean,
        onItemClicked: (DisplayActivityInfo) -> Unit
    ) {
        browserTitle.text = info.displayLabel()
        browserIcon.setImageDrawable(info.displayIcon())
        browserSelected.isChecked = isSelected

        itemView.setOnClickListener {
            onItemClicked(info)
        }
    }

    class Factory @Inject constructor() {

        private val creator = { view: View -> BrowserViewHolder(view) }

        fun createWith(parent: ViewGroup) = creator(parent.inflate(R.layout.browser_list_item))
    }
}
