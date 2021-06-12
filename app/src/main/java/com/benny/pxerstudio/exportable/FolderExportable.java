package com.benny.pxerstudio.exportable;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;

import com.benny.pxerstudio.util.Utils;
import com.benny.pxerstudio.widget.PxerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by BennyKok on 10/17/2016.
 */

public class FolderExportable extends Exportable {
    @Override
    public void runExport(final Context context, final PxerView pxerView) {
        ExportingUtils.INSTANCE.showExportingDialog(context, pxerView, new ExportingUtils.OnExportConfirmedListener() {
            @Override
            public void onExportConfirmed(String fileName, int width, int height) {
                Paint paint = new Paint();
                Canvas canvas = new Canvas();

                final ArrayList<File> pngs = new ArrayList<>();

                ExportingUtils.INSTANCE.showProgressDialog(context);
                new AsyncTask<Void, Integer, Void>() {
                    Uri uri = null;

                    @Override
                    protected Void doInBackground(Void... params) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                            final ContentResolver resolver = context.getContentResolver();
                            try {
                                for (int i = 0; i < pxerView.getPxerLayers().size(); i++) {
                                    final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                                    canvas.setBitmap(bitmap);
                                    canvas.drawBitmap(
                                    pxerView.getPxerLayers().get(i).bitmap, null,
                                        new Rect(0, 0, width, height),
                                        paint);

                                    ContentValues values = ExportingUtils.INSTANCE.getExportContVals(fileName  + "_Frame_" + (i + 1)+ ".png", "image/png", fileName);

                                    uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                    if (uri == null)
                                        throw new IOException("Failed to create new MediaStore record.");
                                    OutputStream out = resolver.openOutputStream(uri);
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                                    out.flush();
                                    out.close();
                                    pngs.add(new File(uri.getPath()));
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                if (uri != null)
                                resolver.delete(uri, null, null);
                            }
                        } else {
                            try {
                                for (int i = 0; i < pxerView.getPxerLayers().size(); i++) {
                                    final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                                    canvas.setBitmap(bitmap);
                                    canvas.drawBitmap(
                                            pxerView.getPxerLayers().get(i).bitmap,
                                            null,
                                            new Rect(0, 0, width, height),
                                            paint);
                                    final File file = new File(
                                            ExportingUtils.INSTANCE.checkAndCreateProjectDirs(fileName),
                                            fileName + "_Frame_" + (i + 1) + ".png");
                                    publishProgress(i);

                                    file.createNewFile();
                                    final OutputStream out = new FileOutputStream(file);
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                                    out.flush();
                                    out.close();
                                    pngs.add(file);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                            return null;

                    }

                    @Override
                    protected void onProgressUpdate(Integer... values) {
                        if (ExportingUtils.currentProgressDialog != null) {
                            ExportingUtils.currentProgressDialog.setTitle(
                                    "Working on frame " + (values[0] + 1));
                        }
                        super.onProgressUpdate(values);
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        ExportingUtils.INSTANCE.dismissAllDialogs();
                        ExportingUtils.INSTANCE.toastAndFinishExport(context, null);
                        ExportingUtils.INSTANCE.scanAlotsOfFile(context, pngs);
                        Utils.freeMemory();
                        super.onPostExecute(aVoid);
                    }
                }.execute();
            }
        });
    }
}
