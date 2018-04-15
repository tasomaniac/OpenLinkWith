package com.tasomaniac.openwith.resolver

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.tasomaniac.openwith.R

class ApplicationViewHolder private constructor(
    view: View,
    private val displaySubtext: Boolean
) : RecyclerView.ViewHolder(view) {

    @BindView(R.id.text1) lateinit var text: TextView
    @BindView(R.id.text2) lateinit var text2: TextView
    @BindView(R.id.icon) lateinit var icon: ImageView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(
        info: DisplayActivityInfo,
        itemClickListener: ItemClickListener,
        itemLongClickListener: ItemLongClickListener?
    ) {
        text.text = info.displayLabel()
        if (displaySubtext) {
            text2.visibility = View.VISIBLE
            text2.text = info.extendedInfo()
        } else {
            text2.visibility = View.GONE
        }
        icon.setImageDrawable(info.displayIcon())

        itemView.setOnClickListener {
            itemClickListener.onItemClick(info)
        }
        itemView.setOnLongClickListener {
            itemLongClickListener?.onItemLongClick(info) ?: false
        }
    }

    companion object {

        fun create(parent: ViewGroup, displaySubtext: Boolean): ApplicationViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.resolve_list_item, parent, false)
            return ApplicationViewHolder(view, displaySubtext)
        }
    }
}
