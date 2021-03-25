package com.benny.pxerstudio.shape.draw;

import android.graphics.Bitmap;

import com.benny.pxerstudio.widget.PxerView;

import java.util.ArrayList;

/**
 * Created by BennyKok on 10/12/2016.
 */

public class RectShape extends DrawShape {
    private final ArrayList<PxerView.Pxer> previousPxer = new ArrayList<>();

    @Override
    public boolean onDraw(PxerView pxerView, int startX, int startY, int endX, int endY) {
        if (!super.onDraw(pxerView, startX, startY, endX, endY)) return true;

        final Bitmap layerToDraw = pxerView.getPxerLayers().get(pxerView.getCurrentLayer()).bitmap;
        draw(layerToDraw, previousPxer);

        int rectWidth = Math.abs(startX - endX);
        int rectHeight = Math.abs(startY - endY);

        for (int i = 0; i < rectWidth + 1; i++) {
            int mX = startX + i * (endX - startX < 0 ? -1 : 1);

            addPxerView(layerToDraw, previousPxer, mX, startY);
            addPxerView(layerToDraw, previousPxer, mX, endY);

            drawOnLayer(layerToDraw, pxerView, mX, startY);
            drawOnLayer(layerToDraw, pxerView, mX, endY);
        }
        for (int i = 1; i < rectHeight; i++) {
            int mY = startY + i * (endY - startY < 0 ? -1 : 1);

            addPxerView(layerToDraw, previousPxer, startX, mY);
            addPxerView(layerToDraw, previousPxer, endX, mY);

            drawOnLayer(layerToDraw, pxerView, startX, mY);
            drawOnLayer(layerToDraw, pxerView, endX, mY);
        }

        pxerView.invalidate();
        return true;
    }

    @Override
    public void onDrawEnd(PxerView pxerView) {
        super.onDrawEnd(pxerView);

        endDraw(previousPxer, pxerView);
    }
}
