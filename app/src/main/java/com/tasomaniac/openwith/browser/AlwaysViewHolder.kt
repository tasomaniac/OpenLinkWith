package com.tasomaniac.openwith.browser

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tasomaniac.openwith.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.preferred_header.text1

class AlwaysViewHolder private constructor(
    override val containerView: View
) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    fun bind(clickListener: () -> Unit) {
        text1.setText(R.string.browser_always_ask)
        itemView.setOnClickListener { clickListener() }
    }

    companion object {
        fun create(parent: ViewGroup): AlwaysViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.preferred_header, parent, false)
            return AlwaysViewHolder(view)
        }
    }
}
