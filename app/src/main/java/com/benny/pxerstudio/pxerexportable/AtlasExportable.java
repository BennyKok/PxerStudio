package com.benny.pxerstudio.pxerexportable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;

import com.benny.pxerstudio.pxerexportable.ExportingUtils.OnExportConfirmedListenser;
import com.benny.pxerstudio.util.Tool;
import com.benny.pxerstudio.widget.PxerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by BennyKok on 10/17/2016.
 */

public class AtlasExportable extends Exportable {
    @Override
    public void runExport(final Context context, final PxerView pxerView) {
        ExportingUtils.INSTANCE.showExportingDialog(context, 2048, pxerView, new OnExportConfirmedListenser() {
            @Override
            public void OnExportConfirmed(String fileName, int width, int height) {
                Paint paint = new Paint();
                Canvas canvas = new Canvas();

                int atlasWidth = (int) Math.ceil(
                        (float) pxerView.getPxerLayers().size() /
                                (float) Math.sqrt((float) pxerView.getPxerLayers().size()));
                int atlasHeight = (int) Math.ceil(
                        (float) pxerView.getPxerLayers().size() / (float) atlasWidth);

                final Bitmap bitmap = Bitmap.createBitmap(
                        width * atlasWidth,
                        height * atlasHeight,
                        Bitmap.Config.ARGB_8888);
                canvas.setBitmap(bitmap);

                int counter = 0;
                for (int y = 0; y < atlasHeight; y++) {
                    for (int x = 0; x < atlasWidth; x++) {
                        if (pxerView.getPxerLayers().size() > counter) {
                            canvas.drawBitmap(
                                    pxerView.getPxerLayers().get(counter).bitmap,
                                    null,
                                    new Rect(width * x,
                                            height * y,
                                            width * (x + 1),
                                            height * (y + 1)),
                                    paint);
                        }
                        counter++;
                    }
                }

                final File file = new File(
                        ExportingUtils.INSTANCE.checkAndCreateProjectDirs(context),
                        fileName + "_Atlas" + ".png");

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
