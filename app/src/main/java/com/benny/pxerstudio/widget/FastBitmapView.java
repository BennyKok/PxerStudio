package com.benny.pxerstudio.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.graphics.ColorUtils;

import com.benny.pxerstudio.R;
import com.benny.pxerstudio.util.ContextKt;
import com.benny.pxerstudio.util.Utils;

/**
 * Created by BennyKok on 10/10/2016.
 */

public class FastBitmapView extends View {
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF boundary = new RectF();
    private final RectF boundary2 = new RectF();
    private final RectF boundary3 = new RectF();
    private final Path visibilityBg = new Path();
    private final float iconSize = ContextKt.convertDpToPixel(getContext(), 24);
    private final float iconSize2 = iconSize / 2;
    private PorterDuffColorFilter overlay;
    private boolean selected;
    private boolean visible = true;
    private int accentColor;
    private Bitmap bitmap;
    private Bitmap invisibleBitmap;
    private float strokeWidth = 2.5f;

    public FastBitmapView(Context context) {
        super(context);

        init();
    }

    public FastBitmapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public FastBitmapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        invalidate();
    }

    public void init() {
        accentColor = getContext().getResources().getColor(R.color.colorAccent);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(ContextKt.convertDpToPixel(getContext(), 2));
        bgPaint.setColor(Color.GRAY);

        iconPaint.setFilterBitmap(true);
        iconPaint.setColor(Color.WHITE);
        iconPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        overlay = new PorterDuffColorFilter(
                ColorUtils.setAlphaComponent(Color.DKGRAY, 100), PorterDuff.Mode.SRC_OVER);

        strokeWidth = ContextKt.convertDpToPixel(getContext(), strokeWidth);

        invisibleBitmap = Utils.drawableToBitmap(getResources().getDrawable(R.drawable.ic_visibility_off));

        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.WHITE);

        if (visible) {
            paint.setColorFilter(null);
        } else {
            paint.setColorFilter(overlay);
        }

        if (bitmap != null)
            canvas.drawBitmap(bitmap, null, boundary2, paint);

        if (!visible && invisibleBitmap != null)
            canvas.drawBitmap(invisibleBitmap, null, boundary3, iconPaint);

        paint.setColorFilter(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        if (selected)
            paint.setColor(accentColor);
        else
            paint.setColor(Color.parseColor("#c6c6c6"));
        float radius = 0f;
        canvas.drawRoundRect(boundary, radius, radius, paint);

        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();

        final int bitmapDimensions =
                Math.min(bitmapWidth, bitmapHeight) / Math.max(bitmapWidth, bitmapHeight);

        final int width = getWidth();
        final int height = getHeight();

        final float w = width * bitmapDimensions;
        final float h = height * bitmapDimensions;

        boundary.set(0, 0, width, height);
        boundary.inset(strokeWidth / 2, strokeWidth / 2);

        if (bitmapWidth < bitmapHeight) {
            boundary2.set(
                    Math.abs(width - w) / 2,
                    0,
                    width - Math.abs(width - w) / 2,
                    height);
        } else {
            boundary2.set(
                    0,
                    Math.abs(height - h) / 2,
                    height,
                    height - Math.abs(height - h) / 2);
        }
        boundary2.inset(strokeWidth, strokeWidth);

        boundary3.set(
                width / 2f - iconSize2,
                height / 2f - iconSize2,
                width / 2f + iconSize2,
                height / 2f + iconSize2);

        visibilityBg.reset();
        visibilityBg.moveTo(strokeWidth, strokeWidth);
        visibilityBg.lineTo(width - strokeWidth, height - strokeWidth);

        visibilityBg.moveTo(width - strokeWidth, strokeWidth);
        visibilityBg.lineTo(strokeWidth, height - strokeWidth);
    }
}
