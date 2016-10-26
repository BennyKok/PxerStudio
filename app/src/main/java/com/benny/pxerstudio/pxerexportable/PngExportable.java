package com.benny.pxerstudio.pxerexportable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.benny.pxerstudio.Tool;
import com.benny.pxerstudio.widget.PxerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by BennyKok on 10/17/2016.
 */

public class PngExportable extends Exportable{
    @Override
    public void runExport(final Context context, final PxerView pxerView) {
        ExportingUtils.getInstance().showExportingDialog(context, pxerView, new ExportingUtils.OnExportConfirmedListenser() {
            @Override
            public void OnExportConfirmed(String fileName, int width, int height) {
                Paint paint = new Paint();
                final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                for (int i = 0; i < pxerView.getPxerLayers().size(); i++) {
                    if (pxerView.getPxerLayers().get(i).visible)
                        canvas.drawBitmap(pxerView.getPxerLayers().get(i).bitmap, null, new Rect(0, 0, width, height), paint);
                }

                final File file = new File(ExportingUtils.getInstance().checkAndCreateProjectDirs(),fileName + ".png");

                ExportingUtils.getInstance().showProgressDialog(context);

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            file.createNewFile();
                            final OutputStream out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                            out.flush();
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        ExportingUtils.getInstance().dismissAllDialogs();
                        ExportingUtils.getInstance().toastAndFinishExport(context,file.toString());
                        Tool.freeMemory();
                        super.onPostExecute(aVoid);
                    }
                }.execute();
            }
        });
    }
}
