package com.tasomaniac.openwith.browser

import android.content.ComponentName
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.extensions.inflate
import com.tasomaniac.openwith.resolver.DisplayActivityInfo
import com.tasomaniac.openwith.util.componentName
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.browser_list_item.browserIcon
import kotlinx.android.synthetic.main.browser_list_item.browserInfo
import kotlinx.android.synthetic.main.browser_list_item.browserSelected
import kotlinx.android.synthetic.main.browser_list_item.browserTitle
import javax.inject.Inject

class BrowserViewHolder private constructor(
    override val containerView: View
) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    fun bind(info: DisplayActivityInfo, selectedBrowser: ComponentName?, onItemClicked: (DisplayActivityInfo) -> Unit) {
        browserTitle.text = info.displayLabel
        browserIcon.setImageDrawable(info.displayIcon)
        browserSelected.isChecked = info.activityInfo.componentName() == selectedBrowser
        browserInfo.isGone = true

        itemView.setOnClickListener {
            onItemClicked(info)
        }
    }

    class Factory @Inject constructor() {

        fun createWith(parent: ViewGroup) = BrowserViewHolder(parent.inflate(R.layout.browser_list_item))
    }
}
