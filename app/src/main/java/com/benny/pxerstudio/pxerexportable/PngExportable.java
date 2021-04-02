package com.benny.pxerstudio.pxerexportable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.AsyncTask;

import com.benny.pxerstudio.util.Tool;
import com.benny.pxerstudio.widget.PxerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by BennyKok on 10/17/2016.
 */

public class PngExportable extends Exportable {
    @Override
    public void runExport(final Context context, final PxerView pxerView) {
        ExportingUtils.INSTANCE.showExportingDialog(context, pxerView, new ExportingUtils.OnExportConfirmedListener() {
            @Override
            public void onExportConfirmed(String fileName, int width, int height) {
                final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                for (int i = 0; i < pxerView.getPxerLayers().size(); i++) {
                    if (pxerView.getPxerLayers().get(i).visible)
                        canvas.drawBitmap(
                                pxerView.getPxerLayers().get(i).bitmap,
                                null,
                                new Rect(0, 0, width, height),
                                null);
                }

                final File file = new File(
                        ExportingUtils.INSTANCE.checkAndCreateProjectDirs(context), fileName + ".png");

                ExportingUtils.INSTANCE.showProgressDialog(context);

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
                        ExportingUtils.INSTANCE.dismissAllDialogs();
                        ExportingUtils.INSTANCE.toastAndFinishExport(context, file.toString());
                        Tool.freeMemory();
                        super.onPostExecute(aVoid);
                    }
                }.execute();
            }
        });
    }
}
