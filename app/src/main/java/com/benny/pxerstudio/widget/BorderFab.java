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
import com.benny.pxerstudio.util.ContextKt;
import com.github.clans.fab.FloatingActionButton;

/**
 * Created by BennyKok on 10/9/2016.
 */

public class BorderFab extends FloatingActionButton {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    float three, one;
    Path path = new Path();
    Rect rect = new Rect(0, 0, getWidth(), getHeight());

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

    public void setColor(int color) {
        this.color = color;
        invalidate();
    }

    private void init() {
        bg = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
        bg.eraseColor(Color.WHITE);
        bg.setPixel(0, 0, Color.GRAY);
        bg.setPixel(1, 1, Color.GRAY);

        three = ContextKt.convertDpToPixel(getContext(), 2);
        one = ContextKt.convertDpToPixel(getContext(), 1);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getContext().getResources().getColor(R.color.colorAccent));
        paint.setStrokeWidth(ContextKt.convertDpToPixel(getContext(), 6));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        colorPaint.setColor(Color.WHITE);

        final int width = getWidth();
        final int height = getHeight();

        canvas.save();
        path.addCircle(width / 2f, height / 2f, width / 3f + one, Path.Direction.CCW);
        canvas.clipPath(path);
        canvas.drawBitmap(bg, null, rect, colorPaint);
        canvas.restore();

        colorPaint.setColor(color);
        canvas.drawCircle(width / 2f, height / 2f, width / 3f + one, colorPaint);
        canvas.drawCircle(width / 2f, height / 2f, width / 3f + one, paint);
    }
}
