package com.benny.pxerstudio.exportable;

import android.content.Context;

import com.benny.pxerstudio.widget.PxerView;

/**
 * Created by BennyKok on 10/17/2016.
 */

public abstract class Exportable {
    public abstract void runExport(final Context context, final PxerView pxerView);
}
