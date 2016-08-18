package com.tasomaniac.openwith.homescreen;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;

class ShortcutIconCreator {

    private final Bitmap shortcutMark;

    ShortcutIconCreator(Bitmap shortcutMark) {
        this.shortcutMark = shortcutMark;
    }

    Bitmap createShortcutIconFor(BitmapDrawable originalDrawable) {
        return createShortcutIconFor(originalDrawable.getBitmap());
    }

    private Bitmap createShortcutIconFor(Bitmap bitmap) {
        return mergeBitmaps(bitmap, shortcutMark);
    }

    private static Bitmap mergeBitmaps(Bitmap background, Bitmap overlay) {
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
