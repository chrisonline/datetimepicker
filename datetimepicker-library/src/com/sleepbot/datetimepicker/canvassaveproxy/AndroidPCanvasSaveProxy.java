package com.sleepbot.datetimepicker.canvassaveproxy;


import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

/**
 * Created by Christian Grasser on 14.10.2018.
 */
@RequiresApi(api = Build.VERSION_CODES.P)
public class AndroidPCanvasSaveProxy implements CanvasSaveProxy {
    private static final String TAG = CanvasSaveProxy.class.getSimpleName();
    private final Canvas mCanvas;

    AndroidPCanvasSaveProxy(final Canvas canvas) {
        Log.d(TAG, "New AndroidPCanvasSaveProxy");

        mCanvas = canvas;
    }

    @Override
    public int save() {
        return mCanvas.save();
    }

    @Override
    public boolean isFor(final Canvas canvas) {
        return canvas == mCanvas;
    }
}
