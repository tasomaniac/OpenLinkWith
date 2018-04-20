package com.tasomaniac.openwith

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

open class HeaderAdapter<in T : RecyclerView.ViewHolder, H : RecyclerView.ViewHolder> @JvmOverloads constructor(
    private val innerAdapter: RecyclerView.Adapter<T>,
    private val createHeaderViewHolder: (parent: ViewGroup) -> H,
    private val bindHeader: H.() -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        innerAdapter.registerAdapterDataObserver(ForwardingDataObserver())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> createHeaderViewHolder(parent)
            else -> innerAdapter.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) =
        throw UnsupportedOperationException()

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {
        when (getItemViewType(position)) {
            TYPE_HEADER -> (holder as H).bindHeader()
            else -> innerAdapter.onBindViewHolder(holder as T, position - HEADER_COUNT, payloads)
        }
    }

    override fun getItemCount() = innerAdapter.itemCount + HEADER_COUNT

    override fun getItemViewType(position: Int) =
        if (position < HEADER_COUNT) {
            TYPE_HEADER
        } else {
            innerAdapter.getItemViewType(position - HEADER_COUNT)
        }

    private inner class ForwardingDataObserver : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            notifyDataSetChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            notifyItemRangeChanged(positionStart + 1, itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            notifyItemRangeChanged(positionStart + 1, itemCount, payload)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            notifyItemRangeInserted(positionStart + 1, itemCount)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            notifyItemRangeRemoved(positionStart + 1, itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            if (itemCount == 1) {
                notifyItemMoved(fromPosition + 1, toPosition + 1)
            }
        }
    }

    companion object {

        private const val TYPE_HEADER = Integer.MAX_VALUE
        private const val HEADER_COUNT = 1
    }
}
