package com.benny.pxerstudio.colorpicker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.benny.pxerstudio.R;
import com.benny.pxerstudio.util.Tool;

/**
 * Created by BennyKok on 10/14/2016.
 */

public class ColorPicker {
    private PopupWindow popupWindow;
    private SatValView.OnColorChangeListener listener;
    private SatValView satValView;

    public ColorPicker(Context c, int startColor, SatValView.OnColorChangeListener listener) {
        View contentView = LayoutInflater.from(c).inflate(R.layout.colorpicker_popup, null);
        contentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        satValView = (SatValView) contentView.findViewById(R.id.satValView);
        satValView.withHueBar((HueSeekBar) contentView.findViewById(R.id.hueSeekBar));
        satValView.withAlphaBar((AlphaSeekBar) contentView.findViewById(R.id.alphaSeekBar));
        satValView.setListener(listener);
        satValView.setColor(startColor);
        popupWindow = new PopupWindow(contentView);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setHeight((int) Tool.convertDpToPixel(292, c));
        popupWindow.setWidth((int) Tool.convertDpToPixel(216, c));
    }

    public void show(View anchor) {
        if (!popupWindow.isShowing())
            popupWindow.showAsDropDown(anchor, -popupWindow.getWidth() / 2 + anchor.getWidth() / 2, 0);
        else
            popupWindow.dismiss();
    }

    public void setColor(int color) {
        satValView.setColor(color);
    }

    public void onConfigChanges() {
        popupWindow.dismiss();
    }
}
