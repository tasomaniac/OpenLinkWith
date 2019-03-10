package com.tasomaniac.openwith.intro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.viewpager2.widget.ViewPager2;

public class AppIntroViewPager extends ViewPager2 {

    private boolean pagingEnabled;
    private boolean nextPagingEnabled;
    private float initialXValue;
    private int lockPage;

    public AppIntroViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        pagingEnabled = true;
        nextPagingEnabled = true;
        lockPage = 0;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return shouldHandleTouch(event) && super.onInterceptTouchEvent(event);

    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        return shouldHandleTouch(event) && super.onTouchEvent(event);

    }

    private boolean shouldHandleTouch(MotionEvent event) {
        if (!pagingEnabled) {
            return false;
        }

        if (!nextPagingEnabled) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                initialXValue = event.getX();
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                return !detectSwipeToRight(event);
            }
        }
        return true;
    }

    // To enable/disable swipe
    void setNextPagingEnabled(boolean nextPagingEnabled) {
        this.nextPagingEnabled = nextPagingEnabled;
        if (!nextPagingEnabled) {
            lockPage = getCurrentItem();
        }
    }

    boolean isNextPagingEnabled() {
        return nextPagingEnabled;
    }

    boolean isPagingEnabled() {
        return pagingEnabled;
    }

    void setPagingEnabled(boolean pagingEnabled) {
        this.pagingEnabled = pagingEnabled;
    }

    int getLockPage() {
        return lockPage;
    }

    void setLockPage(int lockPage) {
        this.lockPage = lockPage;
    }

    // Detects the direction of swipe. Right or left.
    // Returns true if swipe is in right direction
    private boolean detectSwipeToRight(MotionEvent event) {
        final int SWIPE_THRESHOLD = 0; // detect swipe
        boolean result = false;

        try {
            float diffX = event.getX() - initialXValue;
            if (Math.abs(diffX) > SWIPE_THRESHOLD) {
                if (diffX < 0) {
                    // swipe from right to left detected ie.SwipeLeft
                    result = true;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;
    }
}
