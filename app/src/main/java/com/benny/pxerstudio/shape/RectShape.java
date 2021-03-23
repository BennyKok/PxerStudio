package com.benny.pxerstudio.shape;

import android.graphics.Bitmap;

import androidx.core.graphics.ColorUtils;

import com.benny.pxerstudio.widget.PxerView;

import java.util.ArrayList;

/**
 * Created by BennyKok on 10/12/2016.
 */

public class RectShape extends BaseShape {

    ArrayList<PxerView.Pxer> previousPxer = new ArrayList<>();

    @Override
    public boolean onDraw(PxerView pxerView, int startX, int startY, int endX, int endY) {
        if (!super.onDraw(pxerView, startX, startY, endX, endY)) return true;

        Bitmap layerToDraw = pxerView.getPxerLayers().get(pxerView.getCurrentLayer()).bitmap;

        for (int i = 0; i < previousPxer.size(); i++) {
            PxerView.Pxer pxer = previousPxer.get(i);
            layerToDraw.setPixel(pxer.getX(), pxer.getY(), pxer.getColor());
        }

        previousPxer.clear();

        int rectWidth = Math.abs(startX - endX), rectHeight = Math.abs(startY - endY);

        for (int i = 0; i < rectWidth + 1; i++) {
            int mX = startX + (i * ((endX - startX) < 0 ? -1 : 1));

            previousPxer.add(new PxerView.Pxer(mX, startY, layerToDraw.getPixel(mX, startY)));
            previousPxer.add(new PxerView.Pxer(mX, endY, layerToDraw.getPixel(mX, endY)));

            layerToDraw.setPixel(mX, startY,
                    ColorUtils.compositeColors(
                            pxerView.getSelectedColor(),
                            layerToDraw.getPixel(mX, startY)));
            layerToDraw.setPixel(mX, endY,
                    ColorUtils.compositeColors(
                            pxerView.getSelectedColor(),
                            layerToDraw.getPixel(mX, endY)));
        }
        for (int i = 1; i < rectHeight; i++) {
            int mY = startY + (i * ((endY - startY) < 0 ? -1 : 1));

            previousPxer.add(new PxerView.Pxer(startX, mY, layerToDraw.getPixel(startX, mY)));
            previousPxer.add(new PxerView.Pxer(endX, mY, layerToDraw.getPixel(endX, mY)));

            layerToDraw.setPixel(startX, mY,
                    ColorUtils.compositeColors(
                            pxerView.getSelectedColor(),
                            layerToDraw.getPixel(startX, mY)));
            layerToDraw.setPixel(endX, mY,
                    ColorUtils.compositeColors(
                            pxerView.getSelectedColor(),
                            layerToDraw.getPixel(endX, mY)));
        }

        pxerView.invalidate();
        return true;
    }

    @Override
    public void onDrawEnd(PxerView pxerView) {
        super.onDrawEnd(pxerView);
        if (previousPxer.isEmpty()) {
            return;
        }
        pxerView.getCurrentHistory().addAll(previousPxer);
        previousPxer.clear();

        pxerView.setUnrecordedChanges(true);
        pxerView.finishAddHistory();
    }
}
