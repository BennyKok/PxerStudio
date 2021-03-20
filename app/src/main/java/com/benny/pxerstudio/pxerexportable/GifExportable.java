package com.benny.pxerstudio.pxerexportable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;

import com.benny.pxerstudio.util.Tool;
import com.benny.pxerstudio.gifencoder.AnimatedGifEncoder;
import com.benny.pxerstudio.widget.PxerView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by BennyKok on 10/17/2016.
 */

public class GifExportable extends Exportable {
    @Override
    public void runExport(final Context context, final PxerView pxerView) {
        ExportingUtils.INSTANCE.showExportingDialog(context, pxerView, new ExportingUtils.OnExportConfirmedListenser() {
            @Override
            public void OnExportConfirmed(String fileName, int width, int height) {
                Paint paint = new Paint();
                Canvas canvas = new Canvas();

                //Make gif
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                AnimatedGifEncoder encoder = new AnimatedGifEncoder();
                encoder.start(bos);
                for (int i = 0; i < pxerView.getPxerLayers().size(); i++) {
                    final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    canvas.setBitmap(bitmap);
                    canvas.drawBitmap(pxerView.getPxerLayers().get(i).bitmap, null, new Rect(0, 0, width, height), paint);
                    encoder.addFrame(bitmap);
                }
                encoder.finish();
                final byte[] finalgif = bos.toByteArray();
                //Finish giffing

                final File file = new File(ExportingUtils.INSTANCE.checkAndCreateProjectDirs(context), fileName + ".gif");

                ExportingUtils.INSTANCE.showProgressDialog(context);

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            file.createNewFile();
                            final OutputStream out = new FileOutputStream(file);
                            out.write(finalgif);
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
                        ExportingUtils.INSTANCE.toastAndFinishExport(context,file.toString());
                        Tool.freeMemory();
                        super.onPostExecute(aVoid);
                    }
                }.execute();
            }
        });
    }
}
