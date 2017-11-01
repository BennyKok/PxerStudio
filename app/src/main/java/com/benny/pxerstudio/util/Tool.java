package com.benny.pxerstudio.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.pxerstudio.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by BennyKok on 10/6/2016.
 */
public class Tool {
    public static final Typeface myType = Typeface.create("sans-serif-light", Typeface.NORMAL);

    public static void print(Object o) {
        Log.d("Hey", o.toString());
    }

    public static void print(Object... o) {
        String result = "";
        for (int i = 0; i < o.length; i++) {
            result += " " + o[i].toString();
        }
        Log.d("Hey", result);
    }

    public static void toast(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static void saveProject(String name, String data) {
        File dirs = new File(Environment.getExternalStorageDirectory().getPath().concat("/PxerStudio/Project"));
        if (!dirs.exists()) {
            dirs.mkdirs();
        }

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(new File(dirs, name)));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String stripExtension(String str) {
        if (str == null) return null;
        int pos = str.lastIndexOf(".");
        if (pos == -1) return str;
        return str.substring(0, pos);
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static void freeMemory() {
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }

    public static String trimLongString(String str) {
        if (str.length() > 25) {
            return "..." + str.substring(str.length() - 21, str.length());
        }
        return str;
    }

    public static MaterialDialog.Builder prompt(Context c) {
        return new MaterialDialog.Builder(c)
                .negativeText(R.string.cancel)
                .titleGravity(GravityEnum.CENTER)
                .typeface(Tool.myType, Tool.myType)
                .positiveColor(Color.RED);
    }

    public static MaterialDialog.Builder promptTextInput(Context c, String title) {
        return new MaterialDialog.Builder(c)
                .negativeText(R.string.cancel)
                .positiveText(R.string.ok)
                .title(title)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRange(0, 20)
                .titleGravity(GravityEnum.CENTER)
                .typeface(Tool.myType, Tool.myType)
                .positiveColor(Color.GREEN);
    }
}
