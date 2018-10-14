package com.sleepbot.datetimepicker.canvassaveproxy;

import android.graphics.Canvas;

/**
 * Created by Christian Grasser on 14.10.2018.
 */
public interface CanvasSaveProxy {
    int save();

    boolean isFor(final Canvas canvas);
}
