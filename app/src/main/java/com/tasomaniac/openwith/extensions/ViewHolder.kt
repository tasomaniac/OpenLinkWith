package com.tasomaniac.openwith.extensions

import android.support.v7.widget.RecyclerView
import com.tasomaniac.openwith.util.Utils

fun <T : RecyclerView.ViewHolder> T.withMinHeight() = apply {
    itemView.minimumHeight = Utils.dpToPx(itemView.resources, 72)
}
