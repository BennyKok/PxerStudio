@file:JvmName("Utils")

package com.benny.pxerstudio.util

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.updateBounds

/**
 * Created by BennyKok on 10/6/2016.
 */

fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable) {
        drawable.bitmap?.let {
            return it
        }
    }
    val bitmap: Bitmap =
        if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            createBitmap(1, 1) // Single color bitmap will be created of 1x1 pixel
        } else {
            createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        }
    return bitmap.applyCanvas {
        drawable.updateBounds(
            right = width,
            bottom = height
        )
        drawable.draw(this)
    }
}

fun freeMemory() {
    System.runFinalization()
    Runtime.getRuntime().gc()
    System.gc()
}
