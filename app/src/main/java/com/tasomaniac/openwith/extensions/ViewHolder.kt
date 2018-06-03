package com.tasomaniac.openwith.extensions

import androidx.recyclerview.widget.RecyclerView
import com.tasomaniac.openwith.util.Utils

@Suppress("MagicNumber")
fun <T : RecyclerView.ViewHolder> T.withMinHeight() = apply {
    itemView.minimumHeight = Utils.dpToPx(itemView.resources, 72)
}
