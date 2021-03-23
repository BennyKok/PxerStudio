package com.benny.pxerstudio.pxerexportable

import android.content.Context
import android.media.MediaScannerConnection
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.benny.pxerstudio.R
import com.benny.pxerstudio.util.Tool.toast
import com.benny.pxerstudio.widget.PxerView
import java.io.File

/**
 * Created by BennyKok on 10/17/2016.
 */
object ExportingUtils {
    @JvmField
    var currentProgressDialog: MaterialDialog? = null
    fun dismissAllDialogs() {
        if (currentProgressDialog != null) currentProgressDialog!!.dismiss()
    }

    fun checkAndCreateProjectDirs(context: Context): File {
        val path = context.getExternalFilesDir("/")!!.path + "/PxerStudio/Export"
        val dirs = File(path)
        if (!dirs.exists()) {
            dirs.mkdirs()
        }
        return dirs
    }

    fun checkAndCreateProjectDirs(extraFolder: String?, context: Context): File {
        if (extraFolder == null || extraFolder.isEmpty()) return checkAndCreateProjectDirs(context)
        val path = context.getExternalFilesDir("/")!!.path + "/PxerStudio/Export/" + extraFolder
        val dirs = File(path)
        if (!dirs.exists()) {
            dirs.mkdirs()
        }
        return dirs
    }

    fun toastAndFinishExport(context: Context?, fileName: String?) {
        if (fileName != null && fileName.isNotEmpty()) MediaScannerConnection.scanFile(
            context, arrayOf(fileName), null
        ) { _, _ -> }
        toast(context, "Exported successfully")
    }

    fun scanAlotsOfFile(context: Context?, files: List<File>) {
        val paths = arrayOfNulls<String>(files.size)
        for (i in files.indices) {
            paths[i] = "${files[i]}"
        }
        MediaScannerConnection.scanFile(
            context,
            paths, null
        ) { _, _ -> }
    }

    fun showProgressDialog(context: Context?) {
        currentProgressDialog = MaterialDialog(context!!)
//            .typeface(Tool.myType, Tool.myType)
            .cancelable(false)
            .cancelOnTouchOutside(false)
            .title(null, "Painting...")
            .message(null, "Exporting...")
//            .progress
//            .progress(true, 0)
//            .progressIndeterminateStyle(true)

        currentProgressDialog!!.show()
    }

    fun showExportingDialog(
        context: Context?,
        pxerView: PxerView,
        listener: OnExportConfirmedListener
    ) {
        showExportingDialog(context, -1, pxerView, listener)
    }

    fun showExportingDialog(
        context: Context?,
        maxSize: Int,
        pxerView: PxerView,
        listener: OnExportConfirmedListener
    ) {
        val l = LayoutInflater.from(context)
            .inflate(R.layout.dialog_activity_drawing, null) as ConstraintLayout
        val editText = l.findViewById<View>(R.id.et1) as EditText
        val seekBar = l.findViewById<View>(R.id.sb) as SeekBar
        val textView = l.findViewById<View>(R.id.tv2) as TextView
        editText.setText(pxerView.projectName)
        if (maxSize == -1) seekBar.max = 4096 - pxerView.picWidth else seekBar.max =
            maxSize - pxerView.picWidth
        textView.text =
            "Size : " + java.lang.String.valueOf(pxerView.picWidth) +
                    " x " + java.lang.String.valueOf(
                pxerView.picHeight
            )
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                textView.text =
                    "Size : " + (i + pxerView.picWidth).toString() +
                            " x " + (i + pxerView.picHeight).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        MaterialDialog(context!!)
//            .titleGravity(GravityEnum.CENTER)
//            .typeface(Tool.myType, Tool.myType)
            .customView(view = l)
            .title(null, "Export")
            .positiveButton(null, "Export") {
                if (editText.text.toString().isEmpty()) {
                    toast(context, "The file name cannot be empty!")
                    return@positiveButton
                }
                listener.onExportConfirmed(
                    editText.text.toString(),
                    seekBar.progress + pxerView.picWidth,
                    seekBar.progress + pxerView.picHeight
                )
            }
            .negativeButton(null, "Cancel")
            .show()
    }

    interface OnExportConfirmedListener {
        fun onExportConfirmed(fileName: String?, width: Int, height: Int)
    }

    fun getExportPath(context: Context): String {
        return context.getExternalFilesDir("/")!!.path + "/PxerStudio/Export"
    }

    fun getProjectPath(context: Context): String {
        return context.getExternalFilesDir("/")!!.path + "/PxerStudio/Project"
    }
}
