package com.benny.pxerstudio.shape

import com.benny.pxerstudio.widget.PxerView
import com.benny.pxerstudio.widget.PxerView.Pxer

/**
 * Created by BennyKok on 10/12/2016.
 */
abstract class BaseShape {
    private var hasEnded = false
    private var startX = -1
    private var startY = -1
    private var endX = -1
    private var endY = -1

    open fun onDraw(pxerView: PxerView, startX: Int, startY: Int, endX: Int, endY: Int): Boolean {
        if (this.startX == startX && this.startY == startY && this.endX == endX && this.endY == endY) {
            return false
        }
        this.startX = startX
        this.startY = startY
        this.endX = endX
        this.endY = endY
        return true
    }

    open fun onDrawEnd(pxerView: PxerView) {
        hasEnded = true
    }

    fun hasEnded(): Boolean {
        hasEnded = !hasEnded
        return !hasEnded
    }

    protected fun PxerView.endDraw(previousPxer: ArrayList<Pxer>) {
        if (previousPxer.isEmpty()) {
            return
        }
        this.currentHistory.addAll(previousPxer)
        previousPxer.clear()
        this.setUnrecordedChanges(true)
        this.finishAddHistory()
    }
}
