package com.tasomaniac.openwith.resolver

import android.support.v7.util.DiffUtil

object DiffUtilsCallback : DiffUtil.ItemCallback<DisplayActivityInfo>() {
    override fun areItemsTheSame(oldItem: DisplayActivityInfo, newItem: DisplayActivityInfo) = oldItem == newItem

    override fun areContentsTheSame(oldItem: DisplayActivityInfo, newItem: DisplayActivityInfo) = true
}
