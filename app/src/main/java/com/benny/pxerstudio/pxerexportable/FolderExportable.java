package com.benny.pxerstudio.pxerexportable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;

import com.benny.pxerstudio.util.Tool;
import com.benny.pxerstudio.widget.PxerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by BennyKok on 10/17/2016.
 */

public class FolderExportable extends Exportable{
    @Override
    public void runExport(final Context context,final PxerView pxerView) {
        ExportingUtils.INSTANCE.showExportingDialog(context, pxerView, new ExportingUtils.OnExportConfirmedListenser() {
            @Override
            public void OnExportConfirmed(String fileName, int width, int height) {
                Paint paint = new Paint();
                Canvas canvas = new Canvas();

                final ArrayList<File> pngs = new ArrayList<>();
                final ArrayList<Bitmap> bitmaps = new ArrayList<>();

                for (int i = 0; i < pxerView.getPxerLayers().size(); i++) {
                    final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    canvas.setBitmap(bitmap);
                    canvas.drawBitmap(pxerView.getPxerLayers().get(i).bitmap, null, new Rect(0, 0, width, height), paint);
                    final File file = new File(ExportingUtils.INSTANCE.checkAndCreateProjectDirs(fileName,context),fileName + "_Frame_" + String.valueOf(i+1) + ".png");
                    pngs.add(file);
                    bitmaps.add(bitmap);
                }

                ExportingUtils.INSTANCE.showProgressDialog(context);
                new AsyncTask<Void, Integer, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            for (int i = 0; i < pngs.size(); i++) {
                                publishProgress(i);
                                pngs.get(i).createNewFile();
                                final OutputStream out = new FileOutputStream(pngs.get(i));
                                bitmaps.get(i).compress(Bitmap.CompressFormat.PNG, 100, out);
                                out.flush();
                                out.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onProgressUpdate(Integer... values) {
                        if (ExportingUtils.INSTANCE.currentProgressDialog != null) {
                            ExportingUtils.INSTANCE.currentProgressDialog.setTitle("Working on frame " + String.valueOf(values[0]+1));
                        }
                        super.onProgressUpdate(values);
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        ExportingUtils.INSTANCE.dismissAllDialogs();
                        ExportingUtils.INSTANCE.toastAndFinishExport(context,null);
                        ExportingUtils.INSTANCE.scanAlotsOfFile(context,pngs);
                        Tool.freeMemory();
                        super.onPostExecute(aVoid);
                    }
                }.execute();
            }
        });
    }
}
