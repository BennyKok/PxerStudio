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

import androidx.core.graphics.ColorUtils;
import android.util.AttributeSet;
import android.view.View;

import com.benny.pxerstudio.R;
import com.benny.pxerstudio.util.Tool;

/**
 * Created by BennyKok on 10/10/2016.
 */

public class FastBitmapView extends View {
    private Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private PorterDuffColorFilter overlay;
    private boolean selected;
    private boolean visible = true;
    private int accentColor;
    private RectF boundary = new RectF();
    private RectF boundary2 = new RectF();
    private RectF boundary3 = new RectF();
    private Bitmap bitmap;
    private Bitmap invisibleBitmap;
    private Path visibilityBg = new Path();
    private float strokeWidth = 2.5f;
    private float radius = 0f;
    private float iconSize = Tool.convertDpToPixel(24, getContext());
    private float iconSize2 = iconSize / 2;

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
        bgPaint.setStrokeWidth(Tool.convertDpToPixel(2, getContext()));
        bgPaint.setColor(Color.GRAY);

        iconPaint.setFilterBitmap(true);
        iconPaint.setColor(Color.WHITE);
        iconPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        overlay = new PorterDuffColorFilter(ColorUtils.setAlphaComponent(Color.DKGRAY, 100), PorterDuff.Mode.SRC_OVER);

        strokeWidth = Tool.convertDpToPixel(strokeWidth, getContext());

        invisibleBitmap = Tool.drawableToBitmap(getResources().getDrawable(R.drawable.ic_visibility_off_24dp));

        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.WHITE);

        if (!visible) {
            paint.setColorFilter(overlay);
        } else {
            paint.setColorFilter(null);
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
        canvas.drawRoundRect(boundary, radius, radius, paint);

        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        float w = getWidth() * Math.min(bitmap.getWidth(), bitmap.getHeight()) / (Math.max(bitmap.getWidth(), bitmap.getHeight()));
        float h = getHeight() * Math.min(bitmap.getWidth(), bitmap.getHeight()) / (Math.max(bitmap.getWidth(), bitmap.getHeight()));

        boundary.set(0, 0, getWidth(), getHeight());
        boundary.inset(strokeWidth / 2, strokeWidth / 2);

        if (bitmap.getWidth() < bitmap.getHeight())
            boundary2.set(Math.abs(getWidth() - w) / 2, 0, getWidth() - Math.abs(getWidth() - w) / 2, getHeight());
        else
            boundary2.set(0, Math.abs(getHeight() - h) / 2, getHeight(), getHeight() - Math.abs(getHeight() - h) / 2);
        boundary2.inset(strokeWidth, strokeWidth);

        boundary3.set(getWidth() / 2 - iconSize2, getHeight() / 2 - iconSize2, getWidth() / 2 + iconSize2, getHeight() / 2 + iconSize2);

        visibilityBg.reset();
        visibilityBg.moveTo(strokeWidth, strokeWidth);
        visibilityBg.lineTo(getWidth() - strokeWidth, getHeight() - strokeWidth);

        visibilityBg.moveTo(getWidth() - strokeWidth, strokeWidth);
        visibilityBg.lineTo(strokeWidth, getHeight() - strokeWidth);
    }
}
