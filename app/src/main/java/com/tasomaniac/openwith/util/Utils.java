package com.tasomaniac.openwith.util;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ShareCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;

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

    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();

        return px / (metrics.densityDpi / 160f);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean isUsageStatsEnabled(final Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private Utils() {
    }

    public static String extractUrlFrom(Intent intent, ShareCompat.IntentReader reader) {
        CharSequence text = reader.getText();
        if (text == null) {
            text = getExtraSelectedText(intent);
        }
        return Urls.findFirstUrl(text);
    }

    @SuppressLint("InlinedApi")
    private static CharSequence getExtraSelectedText(Intent intent) {
        return intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
    }

    public static Bitmap mergeBitmaps(Bitmap background, Bitmap overlay) {
        final int width = background.getWidth();
        final int height = background.getHeight();
        int overlayWidth = overlay.getWidth();
        int overlayHeight = overlay.getHeight();
        int left = width - overlayWidth;
        int top = height - overlayHeight;

        Bitmap bmOverlay = Bitmap.createBitmap(width, height, background.getConfig());
        Canvas canvas = new Canvas(bmOverlay);

        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        canvas.drawBitmap(background, 0, 0, paint);
        canvas.drawBitmap(overlay, left, top, paint);
        return bmOverlay;
    }
}
