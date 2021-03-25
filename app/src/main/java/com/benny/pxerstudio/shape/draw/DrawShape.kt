package com.benny.pxerstudio.shape.draw

import android.graphics.Bitmap
import androidx.core.graphics.ColorUtils
import com.benny.pxerstudio.shape.BaseShape
import com.benny.pxerstudio.widget.PxerView
import java.util.*

open class DrawShape : BaseShape() {
    protected fun drawOnLayer(layerToDraw: Bitmap, pxerView: PxerView, x: Int, y: Int) {
        layerToDraw.setPixel(
            x, y, ColorUtils.compositeColors(
                pxerView.selectedColor,
                layerToDraw.getPixel(x, y)
            )
        )
    }

    protected fun addPxerView(
        layerToDraw: Bitmap,
        previousPxer: ArrayList<PxerView.Pxer>,
        x: Int,
        y: Int
    ) {
        previousPxer.add(PxerView.Pxer(x, y, layerToDraw.getPixel(x, y)))
    }

    protected fun draw(layerToDraw: Bitmap, previousPxer: ArrayList<PxerView.Pxer>) {
        for (i in previousPxer.indices) {
            val (x, y, color) = previousPxer[i]
            layerToDraw.setPixel(x, y, color)
        }
        previousPxer.clear()
    }
}
