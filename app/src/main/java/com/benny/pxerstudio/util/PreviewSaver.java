package com.benny.pxerstudio.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.AsyncTask;

import com.benny.pxerstudio.widget.PxerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class PreviewSaver {
    public static void saveTo(File file, int width, int height, PxerView pxerView){
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
                        Utils.freeMemory();
                        super.onPostExecute(aVoid);
                    }
                }.execute();
    }
}
