package com.tasomaniac.openwith.util;

import android.content.res.Resources;
import android.util.TypedValue;
import androidx.annotation.NonNull;

public class Utils {

    /**
     * Converts dp value to px value.
     *
     * @param res Resources objects to get displayMetrics.
     * @param dp  original dp value.
     * @return px value.
     */
    public static int dpToPx(@NonNull Resources res, int dp) {
        return (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }

    private Utils() {
    }

}
