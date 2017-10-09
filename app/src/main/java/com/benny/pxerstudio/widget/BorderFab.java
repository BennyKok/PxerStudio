package com.benny.pxerstudio.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.benny.pxerstudio.R;
import com.benny.pxerstudio.util.Tool;
import com.github.clans.fab.FloatingActionButton;

/**
 * Created by BennyKok on 10/9/2016.
 */

public class BorderFab extends FloatingActionButton{
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    float three,one;

    Bitmap bg;

    int color;

    public BorderFab(Context context) {
        super(context);
        init();
    }

    public BorderFab(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BorderFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BorderFab(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setColor(int color){
        this.color = color;
        invalidate();
    }

    private void init() {
        bg = Bitmap.createBitmap(2,2, Bitmap.Config.ARGB_8888);
        bg.eraseColor(Color.WHITE);
        bg.setPixel(0,0, Color.GRAY);
        bg.setPixel(1,1, Color.GRAY);

        three = Tool.convertDpToPixel(2,getContext());
        one = Tool.convertDpToPixel(1,getContext());

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getContext().getResources().getColor(R.color.colorAccent));
        paint.setStrokeWidth(Tool.convertDpToPixel(6,getContext()));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        colorPaint.setColor(Color.WHITE);

        canvas.save();
        Path p = new Path();
        p.addCircle(getWidth() / 2, getHeight()/ 2, getWidth() / 3 + one, Path.Direction.CCW);
        canvas.clipPath(p);
        canvas.drawBitmap(bg,null,new Rect(0,0,getWidth(),getHeight()),colorPaint);
        canvas.restore();

        colorPaint.setColor(color);
        canvas.drawCircle(getWidth() / 2,  getHeight()/ 2, getWidth() / 3 + one, colorPaint);
        canvas.drawCircle(getWidth() / 2,  getHeight() / 2, getWidth() / 3 + one, paint);
    }

}
