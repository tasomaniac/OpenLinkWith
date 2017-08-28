package com.tasomaniac.openwith.resolver

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import javax.inject.Inject

class ResolveListAdapter @Inject
constructor(private val iconLoader: IconLoader) : RecyclerView.Adapter<ApplicationViewHolder>() {

  var applications = emptyList<DisplayResolveInfo>()
    set(value) {
      field = value
      notifyDataSetChanged()
    }
  var displayExtendedInfo = false
  var selectionEnabled = false
  var checkedItemPosition = RecyclerView.NO_POSITION
    private set

  var itemClickListener: ItemClickListener? = null
  var itemLongClickListener: ItemLongClickListener? = null

  override fun getItemCount() = applications.size

  override fun getItemId(position: Int) = position.toLong()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ApplicationViewHolder.create(parent, iconLoader, displayExtendedInfo)

  override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int, payloads: List<Any>) {
    super.onBindViewHolder(holder, position, payloads)
    holder.itemView.isActivated = position == checkedItemPosition
  }

  override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
    val itemClickListener = ItemClickListener {
      itemClickListener?.onItemClick(it)
      setItemChecked(holder.adapterPosition)
    }
    holder.bind(applications[position], itemClickListener, itemLongClickListener)
  }

  override fun onViewRecycled(holder: ApplicationViewHolder) {
    holder.unbind()
  }

  fun setItemChecked(position: Int) {
    if (!selectionEnabled) {
      return
    }

    notifyItemChanged(position, true)
    notifyItemChanged(checkedItemPosition, false)

    checkedItemPosition = position
  }

  fun remove(item: DisplayResolveInfo) {
    val position = applications.indexOf(item)
    applications -= item
    notifyItemRemoved(position)
  }
}
