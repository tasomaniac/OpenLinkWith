package com.tasomaniac.openwith.intro;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tasomaniac.openwith.R;

import java.util.ArrayList;
import java.util.List;

public class CircularIndicatorView extends LinearLayout {
    private static final int FIRST_PAGE_NUM = 0;
    public static final int DEFAULT_COLOR = 1;

    private List<ImageView> mDots;
    private int mSlideCount;
    int selectedDotColor = DEFAULT_COLOR;
    int unselectedDotColor = DEFAULT_COLOR;

    int mCurrentPosition;

    public CircularIndicatorView(Context context) {
        this(context, null);
    }

    public CircularIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CircularIndicatorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        setGravity(Gravity.CENTER);
        setOrientation(HORIZONTAL);
    }

    public void initialize(int slideCount) {
        mDots = new ArrayList<>();
        mSlideCount = slideCount;
        selectedDotColor = -1;
        unselectedDotColor = -1;

        for (int i = 0; i < slideCount; i++) {
            ImageView dot = new ImageView(getContext());
            dot.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.indicator_dot_grey));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            addView(dot, params);
            mDots.add(dot);
        }

        selectPosition(FIRST_PAGE_NUM);
    }

    public void selectPosition(int index) {
        mCurrentPosition = index;
        for (int i = 0; i < mSlideCount; i++) {
            int drawableId = (i == index) ? (R.drawable.indicator_dot_white) : (R.drawable.indicator_dot_grey);
            Drawable drawable = ContextCompat.getDrawable(getContext(), drawableId);
            if (selectedDotColor != DEFAULT_COLOR && i == index)
                drawable.mutate().setColorFilter(selectedDotColor, PorterDuff.Mode.SRC_IN);
            if (unselectedDotColor != DEFAULT_COLOR && i != index)
                drawable.mutate().setColorFilter(unselectedDotColor, PorterDuff.Mode.SRC_IN);
            mDots.get(i).setImageDrawable(drawable);
        }
    }

    public void setSelectedIndicatorColor(int color) {
        selectedDotColor = color;
        selectPosition(mCurrentPosition);
    }

    public void setUnselectedIndicatorColor(int color) {
        unselectedDotColor = color;
        selectPosition(mCurrentPosition);
    }
}
