package com.benny.pxerstudio.exportable;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.MimeTypeFilter;

import com.benny.pxerstudio.util.Utils;
import com.benny.pxerstudio.widget.PxerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
                for (int i = pxerView.getPxerLayers().size() - 1; i >= 0; i--) {
                    if (pxerView.getPxerLayers().get(i).visible)
                        canvas.drawBitmap(
                                pxerView.getPxerLayers().get(i).bitmap,
                                null,
                                new Rect(0, 0, width, height),
                                null);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName + ".png");
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, ExportingUtils.INSTANCE.getExportPath());

                    final ContentResolver resolver = context.getContentResolver();

                    ExportingUtils.INSTANCE.showProgressDialog(context);

                    new AsyncTask<Void, Void, Void>() {

                        Uri uri = null;
                        @Override
                        protected Void doInBackground(Void... params) {
                            try {
                                uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                if (uri == null)
                                    throw new IOException("Failed to create new MediaStore record.");
                                Log.println(Log.INFO, "Log" ,"Uri: " + uri.getPath());
                                OutputStream out = resolver.openOutputStream(Uri.parse(uri.toString()));
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                            } catch (Exception e) {
                                e.printStackTrace();
                                if (uri != null)
                                    resolver.delete(uri, null, null);
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            ExportingUtils.INSTANCE.dismissAllDialogs();
                            ExportingUtils.INSTANCE.toastAndFinishExport(context, fileName + ".png");
                            Utils.freeMemory();
                            super.onPostExecute(aVoid);
                        }
                    }.execute();
                } else {
                    Toast.makeText(context, "NYI: Remind me to implement this for old API!", Toast.LENGTH_LONG).show();
                    /*file.createNewFile();
                    final OutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();*/
                    /*final File file = new File(
                    ExportingUtils.INSTANCE.checkAndCreateProjectDirs(context), fileName + ".png");*/
                }
            }
        });
    }
}
