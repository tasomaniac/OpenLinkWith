package com.tasomaniac.openwith.browser

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tasomaniac.openwith.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.resolve_list_item.text1
import kotlinx.android.synthetic.main.resolve_list_item.text2

class NoneViewHolder private constructor(
    override val containerView: View
) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    fun bind(clickListener: () -> Unit) {
        text1.setText(R.string.browser_none)
        text2.setText(R.string.browser_none_description)
        itemView.setOnClickListener { clickListener() }
    }

    companion object {
        fun create(parent: ViewGroup): NoneViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.resolve_list_item, parent, false)
            return NoneViewHolder(view)
        }
    }
}
