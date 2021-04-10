@file:JvmName("Utils")

package com.benny.pxerstudio.util

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap

/**
 * Created by BennyKok on 10/6/2016.
 */

fun drawableToBitmap(drawable: Drawable): Bitmap? {
    var bitmap: Bitmap? = null
    if (drawable is BitmapDrawable) {
        if (drawable.bitmap != null) {
            return drawable.bitmap
        }
    }
    bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
        createBitmap(1, 1) // Single color bitmap will be created of 1x1 pixel
    } else {
        createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
    }
    return bitmap.applyCanvas {
        drawable.setBounds(0, 0, width, height)
        drawable.draw(this)
    }
}

fun freeMemory() {
    System.runFinalization()
    Runtime.getRuntime().gc()
    System.gc()
}
