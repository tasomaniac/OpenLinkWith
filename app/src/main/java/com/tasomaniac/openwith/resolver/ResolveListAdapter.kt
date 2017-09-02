package com.tasomaniac.openwith.resolver

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import javax.inject.Inject
import kotlin.properties.Delegates

class ResolveListAdapter @Inject
constructor(private val iconLoader: IconLoader) : RecyclerView.Adapter<ApplicationViewHolder>() {

  var applications by Delegates.observable(emptyList<DisplayResolveInfo>(), { _, _, _ ->
    notifyDataSetChanged()
  })
  var checkedItemPosition by Delegates.observable(RecyclerView.NO_POSITION, { _, oldValue, newValue ->
    notifyItemChanged(newValue, true)
    notifyItemChanged(oldValue, false)
  })
  var displayExtendedInfo = false
  var selectionEnabled = false
  var itemClickListener: ItemClickListener? = null
  var itemLongClickListener: ItemLongClickListener? = null

  override fun getItemCount() = applications.size

  override fun getItemId(position: Int) = position.toLong()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
      ApplicationViewHolder.create(parent, iconLoader, displayExtendedInfo)

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
    holder.bind(applications[position], itemClickListener, itemLongClickListener)
  }

  override fun onViewRecycled(holder: ApplicationViewHolder) {
    holder.unbind()
  }

  fun remove(item: DisplayResolveInfo) {
    val position = applications.indexOf(item)
    applications -= item
    notifyItemRemoved(position)
  }
}
