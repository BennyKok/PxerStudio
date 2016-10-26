package com.benny.pxerstudio.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.view.View;

import com.benny.pxerstudio.R;

/**
 * Created by BennyKok on 10/10/2016.
 */

public class FastBitmapView extends View {
    private Paint bgpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private PorterDuffColorFilter overlay;

    public FastBitmapView(Context context) {
        super(context);

        init();
    }

    private boolean selected;

    private boolean visible = true;

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        invalidate();
    }

    private int accentColor;

    private RectF boundary = new RectF();
    private RectF boundary2 = new RectF();

    private Bitmap bitmap;

    private Path vibisibilityBg = new Path();

    private float strokeWidth = 8f;
    private float radius = 4f;


    public FastBitmapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public FastBitmapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public void init(){
        accentColor = getContext().getResources().getColor(R.color.colorAccent);
        bgpaint.setStyle(Paint.Style.STROKE);
        bgpaint.setStrokeWidth(5);
        bgpaint.setColor(Color.GRAY);

        overlay = new PorterDuffColorFilter(ColorUtils.setAlphaComponent(Color.DKGRAY,100), PorterDuff.Mode.SRC_OVER);

        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.WHITE);

        if(!visible) {
            paint.setColorFilter(overlay);
        }else {
            paint.setColorFilter(null);
        }

        if (bitmap != null)
            canvas.drawBitmap(bitmap,null,boundary2,paint);

        //if(!visible)
            //canvas.drawPath(vibisibilityBg,bgpaint);

        paint.setColorFilter(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        if (selected)
            paint.setColor(accentColor);
        else
            paint.setColor(Color.GRAY);
        canvas.drawRoundRect(boundary,radius,radius,paint);

        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        float w = getWidth() * Math.min(bitmap.getWidth(),bitmap.getHeight())/(Math.max(bitmap.getWidth(),bitmap.getHeight()));
        float h = getHeight() * Math.min(bitmap.getWidth(),bitmap.getHeight())/(Math.max(bitmap.getWidth(),bitmap.getHeight()));

        boundary.set(0,0,getWidth(),getHeight());
        boundary.inset(strokeWidth/2,strokeWidth/2);

        if (bitmap.getWidth()<bitmap.getHeight())
            boundary2.set(Math.abs(getWidth()-w)/2,0,getWidth()-Math.abs(getWidth()-w)/2,getHeight());
        else
            boundary2.set(0,Math.abs(getHeight()-h)/2,getHeight(),getHeight()-Math.abs(getHeight()-h)/2);
        boundary2.inset(strokeWidth,strokeWidth);

        vibisibilityBg.reset();
        vibisibilityBg.moveTo(strokeWidth,strokeWidth);
        vibisibilityBg.lineTo(getWidth()-strokeWidth,getHeight()-strokeWidth);

        vibisibilityBg.moveTo(getWidth()-strokeWidth,strokeWidth);
        vibisibilityBg.lineTo(strokeWidth,getHeight()-strokeWidth);

//        for (int i = 0; i < 6; i++) {
//            vibisibilityBg.moveTo(strokeWidth,i*getWidth()/6+strokeWidth);
//            vibisibilityBg.lineTo(getWidth()-strokeWidth,(i+1)*getWidth()/6-strokeWidth);
//        }
    }
}
