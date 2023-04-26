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
import androidx.core.graphics.ColorUtils;
import com.github.clans.fab.FloatingActionButton;

/**
 * Created by BennyKok on 10/9/2016.
 */

public class BorderFab extends FloatingActionButton {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap backgroundBitmap;

    private float three, one;
    private Path path = new Path();

    private int color;

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
        three = ContextKt.convertDpToPixel(getContext(), 2);
        one = ContextKt.convertDpToPixel(getContext(), 1);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getContext().getResources().getColor(R.color.colorAccent));
        paint.setStrokeWidth(ContextKt.convertDpToPixel(getContext(), 6));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int width = getWidth();
        final int height = getHeight();

        canvas.save();
        path.addCircle(width / 2f, height / 2f, width / 3f + one, Path.Direction.CCW);
        canvas.clipPath(path);
        canvas.drawBitmap(backgroundBitmap, null, new Rect(0, 0, getWidth(), getHeight()), null);
        canvas.restore();

        colorPaint.setColor(color);
        canvas.drawCircle(width / 2f, height / 2f, width / 3f + one, colorPaint);
        canvas.drawCircle(width / 2f, height / 2f, width / 3f + one, paint);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        computeBackgroundBitmap();
        invalidate();
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
