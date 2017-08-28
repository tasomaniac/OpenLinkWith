package com.tasomaniac.openwith.resolver

import android.graphics.drawable.Drawable
import android.os.AsyncTask
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
    private val iconLoader: IconLoader,
    private val displaySubtext: Boolean
) : RecyclerView.ViewHolder(view) {

  @BindView(R.id.text1) lateinit var text: TextView
  @BindView(R.id.text2) lateinit var text2: TextView
  @BindView(R.id.icon) lateinit var icon: ImageView

  private var loadIconTask: AsyncTask<*, *, *>? = null

  init {
    ButterKnife.bind(this, view)
  }

  fun bind(info: DisplayResolveInfo, itemClickListener: ItemClickListener, itemLongClickListener: ItemLongClickListener?) {
    text.text = info.displayLabel()
    if (displaySubtext) {
      text2.visibility = View.VISIBLE
      text2.text = info.extendedInfo()
    } else {
      text2.visibility = View.GONE
    }
    if (info.displayIcon() == null) {
      loadImage(info)
    }
    icon.setImageDrawable(info.displayIcon())

    itemView.setOnClickListener {
      itemClickListener.onItemClick(info)
    }
    itemView.setOnLongClickListener {
      itemLongClickListener != null && itemLongClickListener.onItemLongClick(info)
    }
  }

  private fun loadImage(info: DisplayResolveInfo) {
    loadIconTask?.cancel(true)
    loadIconTask = LoadIconTask(iconLoader) {
      info.displayIcon(it)
      icon.setImageDrawable(it)
    }.execute(info)
  }

  fun unbind() {
    loadIconTask?.cancel(true)
  }

  private class LoadIconTask(
      private val iconLoader: IconLoader,
      private val callback: (Drawable) -> Unit
  ) : AsyncTask<DisplayResolveInfo, Void, Drawable>() {

    override fun doInBackground(vararg params: DisplayResolveInfo): Drawable = params[0].load()

    private fun DisplayResolveInfo.load() = displayIcon() ?: iconLoader.loadFor(resolveInfo())

    override fun onPostExecute(drawable: Drawable) {
      callback(drawable)
    }
  }

  companion object {

    fun create(parent: ViewGroup, iconLoader: IconLoader, displaySubtext: Boolean): ApplicationViewHolder {
      val view = LayoutInflater.from(parent.context).inflate(R.layout.resolve_list_item, parent, false)
      return ApplicationViewHolder(view, iconLoader, displaySubtext)
    }
  }
}
