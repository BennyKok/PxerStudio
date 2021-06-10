package com.benny.pxerstudio.exportable

import android.content.Context
import android.media.MediaScannerConnection
import android.transition.Visibility
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.benny.pxerstudio.R
import com.benny.pxerstudio.activity.DrawingActivity
import com.benny.pxerstudio.databinding.DialogActivityDrawingBinding
import com.benny.pxerstudio.util.displayToast
import com.benny.pxerstudio.widget.PxerView
import java.io.File
import java.lang.Integer.parseInt

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
        context?.displayToast(R.string.export_successful)
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

        if (listener !is OnGifExportConfirmedListener){
            binding.dialogFrameDelay.visibility = View.GONE;
            binding.dialogFrameDelayEdit.visibility = View.GONE;
        } else{
            binding.dialogFrameDelay.visibility = View.VISIBLE;
            binding.dialogFrameDelayEdit.visibility = View.VISIBLE;
        }

        binding.dialogDrawingNameEdit.setText(pxerView.projectName)
        if (maxSize == -1) {
            binding.dialogDrawingSizeSeekBar.max = 4096 / pxerView.picHeight.coerceAtLeast(pxerView.picWidth)
        } else {
            binding.dialogDrawingSizeSeekBar.max = maxSize / pxerView.picHeight.coerceAtLeast(pxerView.picWidth)
        }
        val res = DrawingActivity.mContext.resources;
        binding.dialogDrawingSize.text =
            res.getText(R.string.exportSize).toString() + java.lang.String.valueOf(pxerView.picWidth) +
                    " x " + java.lang.String.valueOf(pxerView.picHeight)
        binding.dialogDrawingSizeSeekBar
            .setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    binding.dialogDrawingSize.text =
                        res.getText(R.string.exportSize).toString() + (i * pxerView.picWidth).toString() +
                                " x " + (i * pxerView.picHeight).toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        context?.let {
            MaterialDialog(it)
                //.titleGravity(GravityEnum.CENTER)
                .customView(view = layoutRoot)
                .title(null, "Export")
                .positiveButton(null, "Export") {
                    if (binding.dialogDrawingNameEdit.text.toString().isEmpty()) {
                        context.displayToast(R.string.file_name_cannot_be_empty)
                        return@positiveButton
                    }

                    if (listener !is OnGifExportConfirmedListener) {
                        listener.onExportConfirmed(
                            binding.dialogDrawingNameEdit.text.toString(),
                            binding.dialogDrawingSizeSeekBar.progress * pxerView.picWidth,
                            binding.dialogDrawingSizeSeekBar.progress * pxerView.picHeight
                        )
                    } else {
                        if (binding.dialogFrameDelayEdit.text.toString().isEmpty()) {
                            context.displayToast(R.string.frame_time_cannot_be_empty)
                            return@positiveButton
                        }
                        listener.onExportConfirmed(
                            binding.dialogDrawingNameEdit.text.toString(),
                            binding.dialogDrawingSizeSeekBar.progress * pxerView.picWidth,
                            binding.dialogDrawingSizeSeekBar.progress * pxerView.picHeight,
                            parseInt(binding.dialogFrameDelayEdit.text.toString())
                        )
                    }
                }
                .negativeButton(null, "Cancel")
                .show()
        }
    }

    interface OnExportConfirmedListener {
        fun onExportConfirmed(fileName: String?, width: Int, height: Int)
    }

    interface OnGifExportConfirmedListener : OnExportConfirmedListener {
        fun onExportConfirmed(fileName: String?, width: Int, height: Int, frameTime: Int)
    }

    fun getExportPath(context: Context): String {
        return context.getExternalFilesDir("/")!!.path + "/PxerStudio/Export"
    }

    fun getProjectPath(context: Context): String {
        return context.getExternalFilesDir("/")!!.path + "/PxerStudio/Project"
    }
}
