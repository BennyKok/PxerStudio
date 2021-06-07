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
import android.util.Log;

import com.benny.pxerstudio.util.Utils;
import com.benny.pxerstudio.widget.PxerView;
import com.bumptech.glide.gifencoder.AnimatedGifEncoder;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by BennyKok on 10/17/2016.
 */

public class GifExportable extends Exportable {
    @Override
    public void runExport(final Context context, final PxerView pxerView) {
        ExportingUtils.INSTANCE.showExportingDialog(context, pxerView, new ExportingUtils.OnGifExportConfirmedListener() {
            @Override
            public void onExportConfirmed(@Nullable String fileName, int width, int height) {}

            @Override
            public void onExportConfirmed(String fileName, int width, int height, int frameTime) {
                Paint paint = new Paint();
                Canvas canvas = new Canvas();

                //Make gif
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                AnimatedGifEncoder encoder = new AnimatedGifEncoder();
                encoder.start(bos);
                for (int i = pxerView.getPxerLayers().size() - 1; i >= 0; i--) {
                    final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    canvas.setBitmap(bitmap);
                    canvas.drawBitmap(
                            pxerView.getPxerLayers().get(i).bitmap,
                            null,
                            new Rect(0, 0, width, height),
                            paint);
                    encoder.addFrame(bitmap);
                    encoder.setDelay(frameTime);
                }
                encoder.finish();
                final byte[] finalgif = bos.toByteArray();
                //Finish giffing


                ExportingUtils.INSTANCE.showProgressDialog(context);

                new AsyncTask<Void, Void, Void>() {
                    Uri uri = null;

                    @Override
                    protected Void doInBackground(Void... params) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            ContentValues values =ExportingUtils.INSTANCE.getExportContVals(fileName + ".gif", "image/gif");

                            final ContentResolver resolver = context.getContentResolver();
                            try {
                                uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                if (uri == null)
                                    throw new IOException("Failed to create new MediaStore record.");
                                    OutputStream out = resolver.openOutputStream(uri);
                                    out.write(finalgif);
                                    out.flush();
                                    out.close();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                if (uri != null)
                                    resolver.delete(uri, null, null);
                            }
                        } else {
                            try {
                                final File file = new File(ExportingUtils.INSTANCE.checkAndCreateProjectDirs(), fileName + ".gif");
                                file.createNewFile();
                                final OutputStream out = new FileOutputStream(file);
                                out.write(finalgif);
                                out.flush();
                                out.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        ExportingUtils.INSTANCE.dismissAllDialogs();
                        ExportingUtils.INSTANCE.toastAndFinishExport(context, ExportingUtils.INSTANCE.getAbsoluteExportablePath(fileName + ".gif"));
                        Utils.freeMemory();
                        super.onPostExecute(aVoid);
                    }
                }.execute();
            }
        });
    }
}
