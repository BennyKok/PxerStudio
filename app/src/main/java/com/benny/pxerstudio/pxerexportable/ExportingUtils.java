package com.benny.pxerstudio.pxerexportable;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.pxerstudio.R;
import com.benny.pxerstudio.util.Tool;
import com.benny.pxerstudio.widget.PxerView;

import java.io.File;
import java.util.List;

/**
 * Created by BennyKok on 10/17/2016.
 */
public class ExportingUtils {
    private static ExportingUtils ourInstance = new ExportingUtils();

    public static ExportingUtils getInstance() {
        return ourInstance;
    }

    public MaterialDialog currentProgressDialog;

    private ExportingUtils() {
    }

    public void dismissAllDialogs(){
        if(currentProgressDialog != null)
            currentProgressDialog.dismiss();
    }

    public File checkAndCreateProjectDirs(){
        String path = Environment.getExternalStorageDirectory().getPath().concat("/PxerStudio/Export");
        File dirs = new File(path);
        if (!dirs.exists()) {
            dirs.mkdirs();
        }
        return dirs;
    }

    public static String getExportPath(){
        return Environment.getExternalStorageDirectory().getPath().concat("/PxerStudio/Export");
    }

    public static String getProjectPath(){
        return Environment.getExternalStorageDirectory().getPath().concat("/PxerStudio/Project");
    }

    public File checkAndCreateProjectDirs(String extraFolder){
        if (extraFolder == null || extraFolder.isEmpty())return checkAndCreateProjectDirs();
        String path = Environment.getExternalStorageDirectory().getPath().concat("/PxerStudio/Export/"+extraFolder);
        File dirs = new File(path);
        if (!dirs.exists()) {
            dirs.mkdirs();
        }
        return dirs;
    }

    public void toastAndFinishExport(Context context,String fileName){
        if (fileName != null && !fileName.isEmpty())
        MediaScannerConnection.scanFile(context,
                new String[]{fileName}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {}
                });

        Tool.toast(context,"Exported successfully");
    }

    public void scanAlotsOfFile(Context context,List<File> files){
        String[] paths = new String[files.size()];
        for (int i = 0; i < files.size(); i++) {
            paths[i] = files.get(i).toString();
        }
        MediaScannerConnection.scanFile(context,
                paths, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {}
                });
    }

    public void showProgressDialog(Context context){
        currentProgressDialog = new MaterialDialog.Builder(context)
                .titleGravity(GravityEnum.CENTER)
                .typeface(Tool.myType, Tool.myType)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .title("Painting...")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .show();
    }

    public void showExportingDialog(final Context context, final PxerView pxerView, final OnExportConfirmedListenser listenser){
        showExportingDialog(context,-1,pxerView,listenser);
    }

    public void showExportingDialog(final Context context,int maxSize, final PxerView pxerView, final OnExportConfirmedListenser listenser){
        final ConstraintLayout l = (ConstraintLayout) LayoutInflater.from(context).inflate(R.layout.dialog_activity_drawing, null);
        final EditText editText = (EditText) l.findViewById(R.id.et1);
        final SeekBar seekBar = (SeekBar) l.findViewById(R.id.sb);
        final TextView textView = (TextView) l.findViewById(R.id.tv2);

        editText.setText(pxerView.getProjectName());

        if (maxSize == -1)
            seekBar.setMax(4096 - pxerView.getPicWidth());
        else
            seekBar.setMax(maxSize - pxerView.getPicWidth());

        textView.setText("Size : " + String.valueOf(pxerView.getPicWidth()) + " x " + String.valueOf(pxerView.getPicHeight()));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textView.setText("Size : " + String.valueOf(i + pxerView.getPicWidth()) + " x " + String.valueOf(i + pxerView.getPicHeight()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        new MaterialDialog.Builder(context)
                .titleGravity(GravityEnum.CENTER)
                .typeface(Tool.myType, Tool.myType)
                .customView(l, false)
                .title("Export")
                .positiveText("Export")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (editText.getText().toString().isEmpty()){
                            Tool.toast(context,"The file name cannot be empty!");
                            return;
                        }
                        listenser.OnExportConfirmed(editText.getText().toString(),seekBar.getProgress() + pxerView.getPicWidth(),seekBar.getProgress() + pxerView.getPicHeight());
                    }
                })
                .show();
    }

    public interface OnExportConfirmedListenser{
       public void OnExportConfirmed(String fileName,int width,int height);
    }
}
