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

/**
 * Created by BennyKok on 10/15/2016.
 */

public class AlphaSeekBar extends androidx.appcompat.widget.AppCompatSeekBar {

    private Bitmap hueBitmap;

    private Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint huePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public AlphaSeekBar(Context context) {
        super(context);

        init();
    }

    public AlphaSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init(){
        thumbPaint.setColor(Color.WHITE);
        thumbPaint.setStyle(Paint.Style.STROKE);
        thumbPaint.setStrokeWidth(8);

        setMax(255);
        setProgress(255);
        setPadding(0,0,0,0);

        setThumb(new Drawable() {
            @Override
            public int getIntrinsicHeight() {
                return getHeight();
            }

            @Override
            public int getIntrinsicWidth() {
                return getHeight()/3;
            }

            @Override
            public void draw(Canvas canvas) {
                thumbPaint.setColor(Color.argb(getProgress(),0,0,0));
                thumbPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawRect(getBounds(),thumbPaint);

                thumbPaint.setColor(Color.WHITE);
                thumbPaint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(getBounds(),thumbPaint);
            }
            @Override
            public void setAlpha(int alpha) {}
            @Override
            public void setColorFilter(ColorFilter colorFilter) {}
            @Override
            public int getOpacity() {
                return PixelFormat.TRANSPARENT;
            }
        });
        setThumbOffset(-(getHeight()/3)/6);
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
                    canvas.drawBitmap(hueBitmap,null,new Rect(0,0,getWidth(),getHeight()),huePaint);
            }
            @Override
            public void setAlpha(int alpha) {}
            @Override
            public void setColorFilter(ColorFilter colorFilter) {}
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
        int width = getWidth()/20;
        int height = getHeight()/20;

        Bitmap hueBitmap = Bitmap.createBitmap(width*2, height*2, Bitmap.Config.ARGB_8888);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height*2; j++) {
                int alpha = (int)((float)255*((float)i/(float)width));
                if (j%2 != 0)
                    hueBitmap.setPixel(i*2+1,j,Color.argb(alpha,100,100,100));
                else
                    hueBitmap.setPixel(i*2,j,Color.argb(alpha,100,100,100));
            }
        }

        return hueBitmap;

    }
}
