package com.benny.pxerstudio.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * Created by BennyKok on 10/15/2016.
 */

public class HueSeekBar extends androidx.appcompat.widget.AppCompatSeekBar {

    private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap hueBitmap;

    public HueSeekBar(Context context) {
        super(context);

        init();
    }

    public HueSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        thumbPaint.setColor(Color.WHITE);
        thumbPaint.setStyle(Paint.Style.STROKE);
        thumbPaint.setStrokeWidth(8);

        setMax(360);
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
                float[] hsv = {getProgress(), 1, 1};
                thumbPaint.setColor(Color.HSVToColor(hsv));
                thumbPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawRect(getBounds(), thumbPaint);

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
                if (hueBitmap != null)
                    canvas.drawBitmap(hueBitmap, null, getBounds(), null);
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

        hueBitmap = getHueBitmap();
        invalidate();
    }

    public Bitmap getHueBitmap() {
        int width = getWidth();
        int height = getHeight();

        Bitmap hueBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int x = 0; x < width; x++) {
            float hue = 0;
            if (width > height) {
                hue = x * 360f / width;
            }
            for (int y = 0; y < height; y++) {
                if (width <= height) {
                    hue = y * 360f / height;
                }

                float[] hsv = {hue, 1, 1};

                hueBitmap.setPixel(x, y, Color.HSVToColor(hsv));
            }
        }

        return hueBitmap;
    }
}
