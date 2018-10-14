package com.sleepbot.datetimepicker.canvassaveproxy;

import android.graphics.Canvas;
import android.os.Build;

/**
 * Created by Christian Grasser on 14.10.2018.
 */
public class CanvasSaveProxyFactory {

    public CanvasSaveProxy create(final Canvas canvas) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return new AndroidPCanvasSaveProxy(canvas);
        } else {
            return new LegacyCanvasSaveProxy(canvas);
        }
    }
}
