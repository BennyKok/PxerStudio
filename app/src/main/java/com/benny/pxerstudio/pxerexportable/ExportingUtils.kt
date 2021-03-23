package com.benny.pxerstudio.pxerexportable

import android.content.Context
import android.media.MediaScannerConnection
import android.view.LayoutInflater
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.benny.pxerstudio.databinding.DialogActivityDrawingBinding
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
        val binding = DialogActivityDrawingBinding.inflate(LayoutInflater.from(context))
        val layoutRoot = binding.root

        binding.dialogDrawingNameEdit.setText(pxerView.projectName)
        if (maxSize == -1) {
            binding.dialogDrawingSizeSeekBar.max = 4096 - pxerView.picWidth
        } else {
            binding.dialogDrawingSizeSeekBar.max = maxSize - pxerView.picWidth
        }
        binding.dialogDrawingSize.text =
            "Size : " + java.lang.String.valueOf(pxerView.picWidth) +
                    " x " + java.lang.String.valueOf(pxerView.picHeight)
        binding.dialogDrawingSizeSeekBar
            .setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    binding.dialogDrawingSize.text =
                        "Size : " + (i + pxerView.picWidth).toString() +
                                " x " + (i + pxerView.picHeight).toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        MaterialDialog(context!!)
//            .titleGravity(GravityEnum.CENTER)
//            .typeface(Tool.myType, Tool.myType)
            .customView(view = layoutRoot)
            .title(null, "Export")
            .positiveButton(null, "Export") {
                if (binding.dialogDrawingNameEdit.text.toString().isEmpty()) {
                    toast(context, "The file name cannot be empty!")
                    return@positiveButton
                }
                listener.onExportConfirmed(
                    binding.dialogDrawingNameEdit.text.toString(),
                    binding.dialogDrawingSizeSeekBar.progress + pxerView.picWidth,
                    binding.dialogDrawingSizeSeekBar.progress + pxerView.picHeight
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
