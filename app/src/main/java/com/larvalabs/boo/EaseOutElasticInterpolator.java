package com.larvalabs.boo;

import android.view.animation.Interpolator;

public class EaseOutElasticInterpolator implements Interpolator {

    private static final float PI_TIMES_2 = (float) (Math.PI * 2);

    @Override
    public float getInterpolation(float value) {
        float s = 0.3f / 4.0f;
        //return (float) (Math.pow(2.0, -10.0d * value) * Math.sin((value - s) * (PI_TIMES_2) / 0.3d) + 1.0d);
        // This version is a little less violent than the standard formula
        return (float) (Math.pow(2.0, -15.0d * value * value) * Math.sin((value * value - s) * (PI_TIMES_2) / 0.3d) + 1.0d);
    }

}
