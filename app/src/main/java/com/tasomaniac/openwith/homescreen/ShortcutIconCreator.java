package com.tasomaniac.openwith.homescreen;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.core.graphics.drawable.IconCompat;

import static android.graphics.Bitmap.Config.ARGB_8888;

class ShortcutIconCreator {

    private final Bitmap shortcutMark;

    ShortcutIconCreator(Bitmap shortcutMark) {
        this.shortcutMark = shortcutMark;
    }

    IconCompat createIconFor(Drawable drawable) {
        return IconCompat.createWithBitmap(createBitmapFor(drawable));
    }

    private Bitmap createBitmapFor(Drawable drawable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return mergeBitmaps(drawable, shortcutMark);
        } else {
            return convertToBitmap(drawable);
        }
    }

    private Bitmap convertToBitmap(Drawable background) {
        final int width = background.getIntrinsicWidth();
        final int height = background.getIntrinsicHeight();

        Bitmap bmOverlay = Bitmap.createBitmap(width, height, ARGB_8888);
        Canvas canvas = new Canvas(bmOverlay);

        background.draw(canvas);
        return bmOverlay;
    }

    private static Bitmap mergeBitmaps(Drawable background, Bitmap overlay) {
        final int width = background.getIntrinsicWidth();
        final int height = background.getIntrinsicHeight();
        int overlayWidth = overlay.getWidth();
        int overlayHeight = overlay.getHeight();
        int left = width - overlayWidth;
        int top = height - overlayHeight;

        Bitmap bmOverlay = Bitmap.createBitmap(width, height, ARGB_8888);
        Canvas canvas = new Canvas(bmOverlay);

        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        background.draw(canvas);
        canvas.drawBitmap(overlay, left, top, paint);
        return bmOverlay;
    }
}
