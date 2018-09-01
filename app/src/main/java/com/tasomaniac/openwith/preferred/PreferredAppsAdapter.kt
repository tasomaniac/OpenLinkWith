package com.tasomaniac.openwith.preferred

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.tasomaniac.openwith.extensions.withMinHeight
import com.tasomaniac.openwith.resolver.ApplicationViewHolder
import com.tasomaniac.openwith.resolver.DiffUtilsCallback
import com.tasomaniac.openwith.resolver.DisplayActivityInfo
import com.tasomaniac.openwith.resolver.ItemClickListener
import javax.inject.Inject

class PreferredAppsAdapter @Inject constructor(
    private val viewHolderFactory: ApplicationViewHolder.Factory
) : ListAdapter<DisplayActivityInfo, ApplicationViewHolder>(DiffUtilsCallback) {

    var itemClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        viewHolderFactory.createWith(parent, displaySubtext = true).withMinHeight()

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        holder.bind(getItem(position), itemClickListener)
    }
}
