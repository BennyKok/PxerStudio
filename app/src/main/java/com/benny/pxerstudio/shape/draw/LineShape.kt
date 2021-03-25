package com.benny.pxerstudio.shape.draw

import android.graphics.Color
import android.graphics.Paint
import com.benny.pxerstudio.widget.PxerView
import com.benny.pxerstudio.widget.PxerView.Pxer
import java.util.*

/**
 * Created by BennyKok on 10/12/2016.
 */
class LineShape : DrawShape() {
    private val p = Paint()
    private val previousPxer = ArrayList<Pxer>()
    private var hasInit = false

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

        if (!hasInit) {
            p.color = Color.YELLOW
            pxerView.preview!!.eraseColor(Color.TRANSPARENT)
            pxerView.previewCanvas.setBitmap(pxerView.preview)
            hasInit = true
        }
        val layerToDraw = pxerView.pxerLayers[pxerView.currentLayer]!!.bitmap
        draw(layerToDraw!!, previousPxer)
        pxerView.preview!!.eraseColor(Color.TRANSPARENT)
/*
        if (startX < endX) endX++ else endX--
        if (startX > endX) startX++ else startX--
        if (startY < endY) endY++ else endY--
        if (startY > endY) startY++ else startY--
*/
        pxerView.previewCanvas.drawLine(
            startX.toFloat(),
            startY.toFloat(),
            endX.toFloat(),
            endY.toFloat(),
            p
        )

//        pxerView.preview!!.setPixel(startX, startY, pxerView.selectedColor)
//        pxerView.preview!!.setPixel(endX, endY, pxerView.selectedColor)

        for (i in 0 until pxerView.picWidth) {
            for (y in 0 until pxerView.picHeight) {
                var c = pxerView.preview!!.getPixel(i, y)
                if (i == startX && y == startY || i == endX && y == endY) c = Color.YELLOW
                if (c == Color.YELLOW) {
                    addPxerView(layerToDraw, previousPxer, i, y)
                    drawOnLayer(layerToDraw, pxerView, i, y)
                }
            }
        }
        pxerView.invalidate()
        return true
    }

    override fun onDrawEnd(pxerView: PxerView) {
        super.onDrawEnd(pxerView)
        hasInit = false
        endDraw(previousPxer, pxerView)
    }

    init {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 1f
    }
}
