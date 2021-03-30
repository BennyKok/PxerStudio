package com.benny.pxerstudio.shape.draw

import com.benny.pxerstudio.widget.PxerView
import com.benny.pxerstudio.widget.PxerView.Pxer
import java.util.*
import kotlin.math.abs

/**
 * Created by BennyKok on 10/12/2016.
 */
class RectShape : DrawShape() {
    private val previousPxer = ArrayList<Pxer>()

    override fun onDraw(
        pxerView: PxerView,
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int
    ): Boolean {
        if (!super.onDraw(pxerView, startX, startY, endX, endY)) {
            return true
        }

        val layerToDraw = pxerView.pxerLayers[pxerView.currentLayer]!!.bitmap
        draw(layerToDraw!!, previousPxer)

        val rectWidth = abs(startX - endX)
        val rectHeight = abs(startY - endY)

        for (i in 0 until rectWidth + 1) {
            val mX = startX + i * if (endX - startX < 0) -1 else 1
            addPxerView(layerToDraw, previousPxer, mX, startY)
            addPxerView(layerToDraw, previousPxer, mX, endY)
            drawOnLayer(layerToDraw, pxerView, mX, startY)
            drawOnLayer(layerToDraw, pxerView, mX, endY)
        }

        for (i in 1 until rectHeight) {
            val mY = startY + i * if (endY - startY < 0) -1 else 1
            addPxerView(layerToDraw, previousPxer, startX, mY)
            addPxerView(layerToDraw, previousPxer, endX, mY)
            drawOnLayer(layerToDraw, pxerView, startX, mY)
            drawOnLayer(layerToDraw, pxerView, endX, mY)
        }
        pxerView.invalidate()
        return true
    }

    override fun onDrawEnd(pxerView: PxerView) {
        super.onDrawEnd(pxerView)
        pxerView.endDraw(previousPxer)
    }
}
