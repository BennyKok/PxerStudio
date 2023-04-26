package com.benny.pxerstudio.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.core.graphics.ColorUtils;
import androidx.appcompat.widget.AppCompatSeekBar;

/**
 * Created by BennyKok on 10/15/2016.
 */

public class AlphaSeekBar extends AppCompatSeekBar {

    private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap alphaBitmap;
    private Bitmap backgroundBitmap;

    public int selectedColor, oldSelectedColor;

    public AlphaSeekBar(Context context) {
        super(context);

        init();
    }

    public AlphaSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        thumbPaint.setColor(Color.WHITE);
        thumbPaint.setStyle(Paint.Style.STROKE);
        thumbPaint.setStrokeWidth(8);

        setMax(255);
        setProgress(255);
        setPadding(0, 0, 0, 0);

        setThumb(new Drawable() {
            @Override
            public int getIntrinsicHeight() {
                return getHeight();
            }

            @Override
            public int getIntrinsicWidth() {
                return getHeight() / 3;
            }

            @Override
            public void draw(Canvas canvas) {
                // Draw a grid background, to see alpha area
                canvas.save();
                canvas.clipRect(getBounds());
                canvas.drawBitmap(backgroundBitmap, null, new Rect(0, 0, getWidth(), getHeight()), null);
                canvas.restore();

                // Use the selected color with the alpha of the current seek bar
                int color = ColorUtils.setAlphaComponent(selectedColor, getProgress());
                thumbPaint.setColor(color);
                thumbPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawRect(getBounds(), thumbPaint);

                // Draw the stroke of the thumb
                thumbPaint.setColor(Color.WHITE);
                thumbPaint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(getBounds(), thumbPaint);
            }

            @Override
            public void setAlpha(int alpha) {
            }

            @Override
            public void setColorFilter(ColorFilter colorFilter) {
            }

            @Override
            public int getOpacity() {
                return PixelFormat.TRANSPARENT;
            }
        });
        setThumbOffset(-(getHeight() / 3) / 6);
        setProgressDrawable(new Drawable() {
            @Override
            public int getIntrinsicHeight() {
                return getHeight();
            }

            @Override
            public int getIntrinsicWidth() {
                return getWidth();
            }

            @Override
            public void draw(Canvas canvas) {
                // Draw a grid background, to see alpha area
                canvas.drawBitmap(backgroundBitmap, null, new Rect(0, 0, getWidth(), getHeight()), null);

                // Draw an alpha gradient of the selected color
                canvas.drawBitmap(alphaBitmap, null, new Rect(0, 0, getWidth(), getHeight()), null);
            }

            @Override
            public void setAlpha(int alpha) {
            }

            @Override
            public void setColorFilter(ColorFilter colorFilter) {
            }

            @Override
            public int getOpacity() {
                return PixelFormat.TRANSPARENT;
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        computeBackgroundBitmap();
        computeAlphaBitmap();
        invalidate();
    }

    /**
     * Computes an alpha gradient of the selected color.
     */
    public void computeAlphaBitmap() {
        int width = getWidth();
        int height = getHeight();

        alphaBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int i = 0; i < width; i++) {
            int alpha = (int) ((float) 255 * ((float) i / (float) width));

            int color = ColorUtils.setAlphaComponent(selectedColor, alpha);

            for (int j = 0; j < height; j++) {
                alphaBitmap.setPixel(i, j, color);
            }
        }
    }

    /**
     * Computes a bitmap with a checkerboard pattern of gray squares.
     */
    public void computeBackgroundBitmap() {
        int width = getWidth() / 10;
        int height = getHeight() / 10;

        backgroundBitmap = Bitmap.createBitmap(width * 2, height * 2, Bitmap.Config.ARGB_8888);
        backgroundBitmap.eraseColor(ColorUtils.setAlphaComponent(Color.GRAY, 200));

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height * 2; j++) {
                if (j % 2 == 0) {
                    backgroundBitmap.setPixel(i * 2, j, Color.argb(200, 220, 220, 220));
                } else {
                    backgroundBitmap.setPixel(i * 2 + 1, j, Color.argb(200, 220, 220, 220));
                }
            }
        }
    }
}
