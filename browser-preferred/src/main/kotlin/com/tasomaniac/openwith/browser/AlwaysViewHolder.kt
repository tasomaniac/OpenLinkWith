package com.tasomaniac.openwith.browser

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tasomaniac.openwith.browser.preferred.R
import com.tasomaniac.openwith.browser.preferred.databinding.BrowserListItemBinding
import com.tasomaniac.openwith.extensions.inflater

class AlwaysViewHolder private constructor(
    private val binding: BrowserListItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(isSelected: Boolean, clickListener: () -> Unit) = binding.apply {
        browserIcon.visibility = View.GONE
        browserTitle.setText(R.string.browser_always_ask)
        browserInfo.setText(R.string.browser_always_ask_description)
        browserSelected.isChecked = isSelected
        itemView.setOnClickListener { clickListener() }
    }

    companion object {
        fun create(parent: ViewGroup) =
            AlwaysViewHolder(BrowserListItemBinding.inflate(parent.inflater(), parent, false))
    }
}
