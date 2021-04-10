package com.benny.pxerstudio.pxerexportable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;

import com.benny.pxerstudio.pxerexportable.ExportingUtils.OnExportConfirmedListener;
import com.benny.pxerstudio.util.Utils;
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
        ExportingUtils.INSTANCE.showExportingDialog(context, 2048, pxerView, new OnExportConfirmedListener() {
            @Override
            public void onExportConfirmed(String fileName, int width, int height) {
                Paint paint = new Paint();
                Canvas canvas = new Canvas();

                float pxerSize = pxerView.getPxerLayers().size();

                int atlasWidth = (int) Math.ceil(pxerSize / (float) Math.sqrt(pxerSize));
                int atlasHeight = (int) Math.ceil(pxerSize / (float) atlasWidth);

                final Bitmap bitmap = Bitmap.createBitmap(
                        width * atlasWidth,
                        height * atlasHeight,
                        Bitmap.Config.ARGB_8888);
                canvas.setBitmap(bitmap);

                int counter = 0;
                for (int y = 0; y < atlasHeight; y++) {
                    for (int x = 0; x < atlasWidth; x++) {
                        if (pxerSize > counter) {
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
                        Utils.freeMemory();
                        super.onPostExecute(aVoid);
                    }
                }.execute();
            }
        });
    }
}
