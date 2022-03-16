package com.benny.pxerstudio.shape

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.benny.pxerstudio.activity.DrawingActivity
import com.benny.pxerstudio.widget.PxerView
import com.benny.pxerstudio.widget.PxerView.Pxer
import java.util.*

/**
 * Created by BennyKok on 10/15/2016.
 */
class EraserShape : BaseShape() {
    private val p = Paint()
    private val previousPxer = ArrayList<Pxer>()
    private var hasInit = false
    private var path: Path? = null

    var width: Float
        get() { return p.strokeWidth   }
        set(value) { p.strokeWidth = value }

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
            path = Path()
            path!!.moveTo(startX.toFloat(), startY.toFloat())
            p.color = Color.YELLOW
            pxerView.preview!!.eraseColor(Color.TRANSPARENT)
            pxerView.previewCanvas.setBitmap(pxerView.preview)
            hasInit = true
        }

        val layerToDraw = pxerView.pxerLayers[pxerView.currentLayer]!!.bitmap
        path!!.lineTo(endX.toFloat(), endY.toFloat())
        pxerView.previewCanvas.drawPath(path!!, p)
        for (i in 0 until pxerView.picWidth) {
            for (y in 0 until pxerView.picHeight) {
                val c = pxerView.preview!!.getPixel(i, y)
                if (c != Color.TRANSPARENT) {
                    val history = Pxer(i, y, layerToDraw!!.getPixel(i, y))
                    if (!previousPxer.contains(history)) {
                        previousPxer.add(history)
                    }
                    layerToDraw.setPixel(i, y, Color.TRANSPARENT)
                }
            }
        }
        pxerView.invalidate()
        return true
    }

    override fun onDrawEnd(pxerView: PxerView) {
        super.onDrawEnd(pxerView)
        hasInit = false
        pxerView.endDraw(previousPxer)
    }

    fun setWidth() {
        val pxerPref = DrawingActivity.mContext.getSharedPreferences("pxerPref", Context.MODE_PRIVATE)
        p.strokeWidth = pxerPref.getFloat("eraser_width", 1f);
    }

    init {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 1f;
    }
}
