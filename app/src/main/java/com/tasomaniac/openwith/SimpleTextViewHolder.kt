package com.tasomaniac.openwith

import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tasomaniac.openwith.extensions.inflate

class SimpleTextViewHolder private constructor(view: View) : RecyclerView.ViewHolder(view) {

    fun setText(@StringRes text: Int) {
        itemView.findViewById<TextView>(android.R.id.text1).setText(text)
    }

    companion object {
        @JvmStatic
        fun create(parent: ViewGroup, @LayoutRes layoutRes: Int) = SimpleTextViewHolder(parent.inflate(layoutRes))
    }
}
