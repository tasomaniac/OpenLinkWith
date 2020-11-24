package com.tasomaniac.openwith.browser

import android.content.ComponentName
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.tasomaniac.openwith.browser.preferred.databinding.BrowserListItemBinding
import com.tasomaniac.openwith.extensions.componentName
import com.tasomaniac.openwith.extensions.inflater
import com.tasomaniac.openwith.resolver.DisplayActivityInfo
import javax.inject.Inject

class BrowserViewHolder private constructor(
    private val binding: BrowserListItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        info: DisplayActivityInfo,
        selectedBrowser: ComponentName?,
        onItemClicked: (DisplayActivityInfo) -> Unit
    ) = binding.apply {
        browserTitle.text = info.displayLabel
        browserIcon.setImageDrawable(info.displayIcon)
        browserSelected.isChecked = info.activityInfo.componentName() == selectedBrowser
        browserInfo.isGone = true
        itemView.setOnClickListener { onItemClicked(info) }
    }

    class Factory @Inject constructor() {

        fun createWith(parent: ViewGroup) =
            BrowserViewHolder(BrowserListItemBinding.inflate(parent.inflater(), parent, false))
    }
}
