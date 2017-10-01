package com.tasomaniac.openwith

import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import butterknife.BindView
import butterknife.ButterKnife

class SimpleTextViewHolder private constructor(view: View) : RecyclerView.ViewHolder(view) {

  @BindView(R.id.text1) lateinit var text: TextView

  init {
    ButterKnife.bind(this, view)
  }

  fun setText(@StringRes text: Int) {
    this.text.setText(text)
  }

  companion object {
    fun create(parent: ViewGroup, @LayoutRes layoutRes: Int): SimpleTextViewHolder {
      val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
      return SimpleTextViewHolder(view)
    }
  }
}
