package com.tasomaniac.openwith.extensions

import androidx.recyclerview.widget.RecyclerView

@Suppress("MagicNumber")
fun <T : RecyclerView.ViewHolder> T.withMinHeight() = apply {
    itemView.minimumHeight = itemView.resources.dpToPx(72)
}
