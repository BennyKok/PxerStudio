package com.benny.pxerstudio.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

/**
 * Created by BennyKok on 10/15/2016.
 */

public class SatValView extends View {
    Paint satPaint = new Paint();
    Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    Bitmap satBitmap;
    Rect satBound = new Rect();

    HueSeekBar hsb;
    AlphaSeekBar asb;

    int alpha;
    float hue;
    float sat;
    float val;

    OnColorChangeListener listener;

    float fingerX, fingerY;
    private Bitmap bgbitmap;

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
                    satBitmap = getSatValBitmap(hue, alpha);
                onColorRetrieved(alpha,hue, sat, val);
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
                onColorRetrieved(alpha,hue, sat, val);
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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        satBitmap = getSatValBitmap(hue, alpha);
        reCalBackground();
        satBound.set(0,0,getRight(),getBottom());
        placePointer(sat * getWidth(), getHeight() - (val * getHeight()),false);
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
        satPaint.setAlpha(alpha);
        if (bgbitmap != null && !isInEditMode())
            canvas.drawBitmap(bgbitmap,null,canvas.getClipBounds(),bgPaint);
        canvas.drawBitmap(satBitmap, null, canvas.getClipBounds(), satPaint);
        canvas.drawCircle(fingerX, fingerY, 20, thumbPaint);
    }

    public Bitmap getSatValBitmap(float hue,int alpha) {
        int skipCount = 1;
        int width = 100;
        int height = 100;
        Bitmap hueBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        int[] colors = new int[width * height];
        int pix = 0;
        for (int y = 0; y < height; y += skipCount) {
            for (int x = 0; x < width; x += skipCount) {

                if (pix >= (width * height))
                    break;

                float sat = (x) / (float) width;
                float val = ((height - y)) / (float) height;

                float[] hsv = new float[]{hue, sat, val};

                int color =  Color.HSVToColor(hsv);
                for (int m = 0; m < skipCount; m++) {
                    if (pix >= (width * height))
                        break;
                    if ((x + m) < width) {
                        colors[pix] = color;
                        pix++;
                    }
                }
            }

            for (int n = 0; n < skipCount; n++) {
                if (pix >= (width * height))
                    break;
                for (int x = 0; x < width; x++) {
                    colors[pix] = colors[pix - width];
                    pix++;
                }
            }
        }
        hueBitmap.setPixels(colors, 0, width, 0, 0, width, height);
        return hueBitmap;

    }

    public void reCalBackground(){
        bgbitmap = Bitmap.createBitmap(10*2,10*2,Bitmap.Config.ARGB_8888);
        bgbitmap.eraseColor(ColorUtils.setAlphaComponent(Color.GRAY,200));

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10*2; j++) {
                if (j%2 != 0)
                    bgbitmap.setPixel(i*2+1,j,Color.argb(200,220,220,220));
                else
                    bgbitmap.setPixel(i*2,j,Color.argb(200,220,220,220));
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                placePointer(event.getX(), event.getY(),true);
                return true;
            case MotionEvent.ACTION_MOVE:
                placePointer(event.getX(), event.getY(),true);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void placePointer(float x, float y,boolean notify) {
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

        sat = (x) / (float) getWidth();
        val = ((getHeight() - y)) / (float) getHeight();

        onColorRetrieved(alpha,hue, sat, val);
    }

    public void setListener(OnColorChangeListener listener) {
        this.listener = listener;
    }


    public interface OnColorChangeListener {
        void onColorChanged(int newColor);
    }

    private void onColorRetrieved(int alpha,float hue, float sat, float val) {
        int color = ColorUtils.setAlphaComponent(Color.HSVToColor(new float[]{hue, sat, val}),alpha);

        if (listener != null) {
            listener.onColorChanged(color);
        }
    }

    public void setColor(int color){
        float[] hsv = new float[3];
        Color.colorToHSV(color,hsv);
        setSaturationAndValue(hsv[1], hsv[2]);
        alpha = Color.alpha(color);
        if (hsb != null){
            hsb.setProgress((int)hsv[0]);
        }
        if (asb != null){
            asb.setProgress(Color.alpha(color));
        }
    }

    private void setSaturationAndValue(float sat,float val){
        this.sat = sat;
        this.val = val;
        placePointer(sat * getWidth(), getHeight() - (val * getHeight()),false);
    }
}
