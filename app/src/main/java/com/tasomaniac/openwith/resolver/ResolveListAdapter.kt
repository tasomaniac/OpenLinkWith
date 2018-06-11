package com.tasomaniac.openwith.resolver

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import javax.inject.Inject
import kotlin.properties.Delegates.observable

class ResolveListAdapter @Inject constructor(
    private val viewHolderFactory: ApplicationViewHolder.Factory
) : ListAdapter<DisplayActivityInfo, ApplicationViewHolder>(DiffUtilsCallback) {

    var checkedItemPosition by observable(RecyclerView.NO_POSITION, { _, oldValue, newValue ->
        notifyItemChanged(newValue, true)
        notifyItemChanged(oldValue, false)
    })
    var displayExtendedInfo = false
    var selectionEnabled = false
    var itemClickListener: ItemClickListener? = null
    var itemLongClickListener: ItemLongClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        viewHolderFactory.createWith(parent, displayExtendedInfo)

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int, payloads: List<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        holder.itemView.isActivated = position == checkedItemPosition
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        val itemClickListener = ItemClickListener {
            itemClickListener?.onItemClick(it)
            if (selectionEnabled) {
                checkedItemPosition = holder.adapterPosition
            }
        }
        holder.bind(getItem(position), itemClickListener, itemLongClickListener)
    }

}
