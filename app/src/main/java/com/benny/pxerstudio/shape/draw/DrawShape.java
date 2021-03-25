package com.benny.pxerstudio.shape.draw;

import android.graphics.Bitmap;

import androidx.core.graphics.ColorUtils;

import com.benny.pxerstudio.shape.BaseShape;
import com.benny.pxerstudio.widget.PxerView;

import java.util.ArrayList;

public class DrawShape extends BaseShape {

    protected void drawOnLayer(final Bitmap layerToDraw, final PxerView pxerView,
                               final int x, final int y) {
        layerToDraw.setPixel(x, y,
                ColorUtils.compositeColors(
                        pxerView.getSelectedColor(),
                        layerToDraw.getPixel(x, y)));
    }

    protected void addPxerView(final Bitmap layerToDraw, final ArrayList<PxerView.Pxer> previousPxer,
                               final int x, final int y) {
        previousPxer.add(new PxerView.Pxer(x, y, layerToDraw.getPixel(x, y)));
    }

    protected void draw(final Bitmap layerToDraw, final ArrayList<PxerView.Pxer> previousPxer) {
        for (int i = 0; i < previousPxer.size(); i++) {
            final PxerView.Pxer pxer = previousPxer.get(i);
            layerToDraw.setPixel(pxer.getX(), pxer.getY(), pxer.getColor());
        }
        previousPxer.clear();
    }
}
