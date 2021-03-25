package com.benny.pxerstudio.shape.draw;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;

import com.benny.pxerstudio.widget.PxerView;

import java.util.ArrayList;

/**
 * Created by BennyKok on 10/12/2016.
 */

public class LineShape extends DrawShape {

    private final Paint p = new Paint();
    private final ArrayList<PxerView.Pxer> previousPxer = new ArrayList<>();
    private boolean hasInit;

    public LineShape() {
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1f);
    }

    @Override
    public boolean onDraw(PxerView pxerView, int startX, int startY, int endX, int endY) {
        if (!super.onDraw(pxerView, startX, startY, endX, endY)) return true;

        if (!hasInit) {
            p.setColor(Color.YELLOW);
            pxerView.getPreview().eraseColor(Color.TRANSPARENT);
            pxerView.getPreviewCanvas().setBitmap(pxerView.getPreview());

            hasInit = true;
        }

        final Bitmap layerToDraw = pxerView.getPxerLayers().get(pxerView.getCurrentLayer()).bitmap;
        draw(layerToDraw, previousPxer);

        pxerView.getPreview().eraseColor(Color.TRANSPARENT);
/*
        if (startX < endX)
            endX++;
        else
            endX--;

        if (startX > endX)
            startX++;
        else
            startX--;

        if (startY < endY)
            endY++;
        else
            endY--;

        if (startY > endY)
            startY++;
        else
            startY--;
*/
        pxerView.getPreviewCanvas().drawLine(startX, startY, endX, endY, p);
        //pxerView.getPreview().setPixel(startX, startY, pxerView.getSelectedColor());
        //pxerView.getPreview().setPixel(endX, endY, pxerView.getSelectedColor());

        for (int i = 0; i < pxerView.getPicWidth(); i++) {
            for (int y = 0; y < pxerView.getPicHeight(); y++) {
                int c = pxerView.getPreview().getPixel(i, y);
                if (i == startX && y == startY || i == endX && y == endY)
                    c = Color.YELLOW;

                if (c == Color.YELLOW) {
                    addPxerView(layerToDraw, previousPxer, i, y);
                    drawOnLayer(layerToDraw, pxerView, i, y);
                }
            }
        }

        pxerView.invalidate();
        return true;
    }

    @Override
    public void onDrawEnd(PxerView pxerView) {
        super.onDrawEnd(pxerView);

        hasInit = false;
        endDraw(previousPxer, pxerView);
    }
}
