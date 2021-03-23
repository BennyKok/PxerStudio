package com.benny.pxerstudio.shape;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.benny.pxerstudio.widget.PxerView;

import java.util.ArrayList;

/**
 * Created by BennyKok on 10/15/2016.
 */

public class EraserShape extends BaseShape {

    private Paint p = new Paint();
    private boolean hasInit;
    private ArrayList<PxerView.Pxer> previousPxer = new ArrayList<>();
    private Path path;

    public EraserShape() {
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3f);
    }

    @Override
    public boolean onDraw(PxerView pxerView, int startX, int startY, int endX, int endY) {
        if (!super.onDraw(pxerView, startX, startY, endX, endY)) return true;
        if (!hasInit) {
            path = new Path();
            path.moveTo(startX, startY);
            p.setColor(Color.YELLOW);
            pxerView.getPreview().eraseColor(Color.TRANSPARENT);
            pxerView.getPreviewCanvas().setBitmap(pxerView.getPreview());

            hasInit = true;
        }

        Bitmap layerToDraw = pxerView.getPxerLayers().get(pxerView.getCurrentLayer()).bitmap;

        path.lineTo(endX, endY);

        pxerView.getPreviewCanvas().drawPath(path, p);

        for (int i = 0; i < pxerView.getPicWidth(); i++) {
            for (int y = 0; y < pxerView.getPicHeight(); y++) {
                int c = pxerView.getPreview().getPixel(i, y);
                if (c != Color.TRANSPARENT) {
                    PxerView.Pxer history = new PxerView.Pxer(i, y, layerToDraw.getPixel(i, y));
                    if (!previousPxer.contains(history))
                        previousPxer.add(history);
                    layerToDraw.setPixel(i, y, Color.TRANSPARENT);
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

        if (previousPxer.isEmpty()) return;
        pxerView.getCurrentHistory().addAll(previousPxer);
        previousPxer.clear();

        pxerView.setUnrecordedChanges(true);
        pxerView.finishAddHistory();
    }

}
