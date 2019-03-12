package com.tasomaniac.openwith

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.tasomaniac.openwith.extensions.inflate

class SimpleTextViewHolder private constructor(view: View) : RecyclerView.ViewHolder(view) {

    fun setText(@StringRes text: Int) {
        itemView.findViewById<TextView>(android.R.id.text1).setText(text)
    }

    companion object {
        @JvmStatic
        fun create(parent: ViewGroup, @LayoutRes layoutRes: Int) =
            SimpleTextViewHolder(parent.inflate(layoutRes))
    }
}
