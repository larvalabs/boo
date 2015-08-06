package com.larvalabs.boo;

import android.util.Log;
import android.view.animation.PathInterpolator;

public class Util {

    public static final boolean VERBOSE_ON = false;
    public static final String TAG = "unlookable";

    public static void log(String message) {
        Log.d(TAG, message);
    }

    public static void verbose(String message) {
        if (VERBOSE_ON) {
            Log.v(TAG, message);
        }
    }

    public static void warn(String message) {
        Log.w(TAG, message);
    }

    public static void error(String message) {
        Log.e(TAG, message);
    }

    public static void error(Throwable e) {
        Log.e(TAG, "", e);
    }

    public static void error(String message, Throwable e) {
        Log.e(TAG, message, e);
    }

    /**
     * Use for everything but entering/exiting..
     */
    public static final PathInterpolator PATH_CURVE = new PathInterpolator(0.4f, 0.0f, 0.2f, 1f);

    /**
     * Use for entering, or fading in.
     */
    public static final PathInterpolator PATH_IN = new PathInterpolator(0.0f, 0.0f, 0.2f, 1f);

    /**
     * Use for exiting, or fading out.
     */
    public static final PathInterpolator PATH_OUT = new PathInterpolator(0.4f, 0.0f, 1f, 1f);

}
