package com.benny.pxerstudio.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import androidx.core.graphics.ColorUtils;

/**
 * Created by BennyKok on 10/15/2016.
 */

public class SatValView extends View {
    private final Paint satPaint = new Paint();
    private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap satBitmap;

    HueSeekBar hsb;
    AlphaSeekBar asb;

    int alpha;
    float hue;
    float sat;
    float val;

    OnColorChangeListener listener;

    float fingerX, fingerY;

    public SatValView(Context context) {
        super(context);

        init();
    }

    public SatValView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public void withHueBar(HueSeekBar hsb) {
        this.hsb = hsb;
        hsb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                hue = progress;
                if (getWidth() > 0)
                    computeHSVBitmapFromHue(hue);
                onColorRetrieved(alpha, hue, sat, val);
                invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void withAlphaBar(AlphaSeekBar asb) {
        this.asb = asb;
        asb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                alpha = progress;
                onColorRetrieved(alpha, hue, sat, val);
                // No need to invalidate here
                // invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        computeHSVBitmapFromHue(hue);
        placePointer(sat * getWidth(), getHeight() - val * getHeight(), false);
    }

    private void init() {
        thumbPaint.setStyle(Paint.Style.STROKE);
        thumbPaint.setStrokeWidth(8);
        thumbPaint.setColor(Color.WHITE);

        satPaint.setAntiAlias(true);
        satPaint.setFilterBitmap(true);
        satPaint.setDither(true);

        setWillNotDraw(false);
        setDrawingCacheEnabled(true);
        setWillNotCacheDrawing(false);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(satBitmap, null, canvas.getClipBounds(), satPaint);
        canvas.drawCircle(fingerX, fingerY, 20, thumbPaint);
    }

    /**
     * Computes a bitmap representing a spectrum of all possible saturation, and value, values for the given hue.
     *
     * @param hue the hue value to use when calculating the bitmap
     */
    public void computeHSVBitmapFromHue(float hue) {
        int width = 100;
        int height = 100;

        satBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float sat = x / (float) width;
                float val = (height - y) / (float) height;

                float[] hsv = {hue, sat, val};
                satBitmap.setPixel(x, y, Color.HSVToColor(hsv));
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                placePointer(event.getX(), event.getY(), true);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void placePointer(float x, float y, boolean notify) {
        if (x < 0)
            x = 0;
        else if (x > getWidth())
            x = getWidth();

        if (y < 0)
            y = 0;
        else if (y > getHeight())
            y = getHeight();

        fingerX = x;
        fingerY = y;

        if (notify)
            retrieveColorAt(x, y);

        invalidate();
    }

    private void retrieveColorAt(float x, float y) {
        fingerX = x;
        fingerY = y;

        sat = x / (float) getWidth();
        val = (getHeight() - y) / (float) getHeight();

        onColorRetrieved(alpha, hue, sat, val);
    }

    public void setListener(OnColorChangeListener listener) {
        this.listener = listener;
    }

    private void onColorRetrieved(int alpha, float hue, float sat, float val) {
        int color = ColorUtils.setAlphaComponent(Color.HSVToColor(new float[]{hue, sat, val}), alpha);

        if (asb != null) {
            asb.selectedColor = color;
            // Update the gradient if the selected color has changed
            if (asb.selectedColor != asb.oldSelectedColor) {
                if (asb.getWidth() > 0) {
                    asb.computeAlphaBitmap();
                    asb.invalidate();
                }
                asb.oldSelectedColor = asb.selectedColor;
            }
        }

        if (listener != null) {
            listener.onColorChanged(color);
        }
    }

    public void setColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        setSaturationAndValue(hsv[1], hsv[2]);
        alpha = Color.alpha(color);
        if (hsb != null) {
            hsb.setProgress((int) hsv[0]);
        }
        if (asb != null) {
            asb.setProgress(Color.alpha(color));
        }
    }

    private void setSaturationAndValue(float sat, float val) {
        this.sat = sat;
        this.val = val;
        placePointer(sat * getWidth(), getHeight() - val * getHeight(), false);
    }

    public interface OnColorChangeListener {
        void onColorChanged(int newColor);
    }
}
