package com.tasomaniac.openwith.browser

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tasomaniac.openwith.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.browser_list_item.browserIcon
import kotlinx.android.synthetic.main.browser_list_item.browserInfo
import kotlinx.android.synthetic.main.browser_list_item.browserSelected
import kotlinx.android.synthetic.main.browser_list_item.browserTitle

class NoneViewHolder private constructor(
    override val containerView: View
) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    fun bind(isSelected: Boolean, clickListener: () -> Unit) {
        browserIcon.visibility = View.GONE
        browserTitle.setText(R.string.browser_none)
        browserInfo.setText(R.string.browser_none_description)
        browserSelected.isChecked = isSelected
        itemView.setOnClickListener { clickListener() }
    }

    companion object {
        fun create(parent: ViewGroup): NoneViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.browser_list_item, parent, false)
            return NoneViewHolder(view)
        }
    }
}
