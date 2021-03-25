package com.benny.pxerstudio.shape;

import com.benny.pxerstudio.widget.PxerView;

import java.util.ArrayList;

/**
 * Created by BennyKok on 10/12/2016.
 */

public abstract class BaseShape {
    private boolean hasEnded;
    private int startX = -1, startY = -1, endX = -1, endY = -1;

    public boolean onDraw(PxerView pxerView, int startX, int startY, int endX, int endY) {
        if (this.startX == startX && this.startY == startY && this.endX == endX && this.endY == endY)
            return false;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        return true;
    }

    public void onDrawEnd(PxerView pxerView) {
        hasEnded = true;
    }

    public boolean hasEnded() {
        hasEnded = !hasEnded;
        return !hasEnded;
    }

    protected void endDraw(final ArrayList<PxerView.Pxer> previousPxer, final PxerView pxerView) {
        if (previousPxer.isEmpty()) {
            return;
        }
        pxerView.getCurrentHistory().addAll(previousPxer);
        previousPxer.clear();

        pxerView.setUnrecordedChanges(true);
        pxerView.finishAddHistory();
    }
}
