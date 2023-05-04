@file:JvmName("ContextKt")
@file:Suppress("PrintStackTrace")

package com.benny.pxerstudio.util

import android.content.Context
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.annotation.StringRes
import com.afollestad.materialdialogs.MaterialDialog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter

/**
 * Displays a toast to the viewer.
 *
 * @param string  The text displayed in the toast.
 */
fun Context.displayToast(@StringRes string: Int) {
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
}

fun Context.saveProject(name: String?, data: String?) {
    val dirs = File(this.getExternalFilesDir("/")!!.path + "/PxerStudio/Project")
    if (!dirs.exists()) {
        dirs.mkdirs()
    }
    try {
        val outputStreamWriter = OutputStreamWriter(FileOutputStream(File(dirs, name)))
        outputStreamWriter.write(data)
        outputStreamWriter.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun Context.convertDpToPixel(dp: Float): Float {
    val resources = this.resources
    val metrics = resources.displayMetrics
    return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Context.prompt(): MaterialDialog {
    return MaterialDialog(this)
        .negativeButton(android.R.string.cancel)
}
