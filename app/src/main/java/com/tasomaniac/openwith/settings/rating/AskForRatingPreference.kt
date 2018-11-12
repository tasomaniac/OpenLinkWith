package com.tasomaniac.openwith.settings.rating

import android.content.Context
import android.util.AttributeSet
import android.widget.RatingBar
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.tasomaniac.openwith.R

class AskForRatingPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : Preference(context, attrs, defStyleAttr) {

    var onRatingChange: (rating: Float) -> Unit = {}

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val ratingBar = holder.findViewById(R.id.ask_for_rating_bar) as RatingBar
        ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) onRatingChange(rating)
        }
    }
}
