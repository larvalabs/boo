package com.larvalabs.boo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class Bubble {

    private static final float SPLINE_8 = 0.2652031f;
    private static final float SQRT_HALF = (float) Math.sqrt(0.5);

    private static final float[] CIRCLE_8_X =
            {
                    0, SQRT_HALF, 1, SQRT_HALF, 0, -SQRT_HALF, -1, -SQRT_HALF
            };

    private static final float[] CIRCLE_8_Y =
            {
                    1, SQRT_HALF, 0, -SQRT_HALF, -1, -SQRT_HALF, 0, SQRT_HALF
            };

    private static final float[] CIRCLE_8_THETA =
            {
                    0, -MathUtils.PI_OVER_4, -MathUtils.PI_OVER_2, -3 * MathUtils.PI_OVER_4, MathUtils.PI, 3 * MathUtils.PI_OVER_4, MathUtils.PI_OVER_2, MathUtils.PI_OVER_4
            };

    private static final float[] CIRCLE_8_AX = new float[8];
    private static final float[] CIRCLE_8_AY = new float[8];
    private static final float[] CIRCLE_8_BX = new float[8];
    private static final float[] CIRCLE_8_BY = new float[8];

    private static int TEST_COLORS[] = new int[8];

    static {
        float[] hsv = new float[3];
        hsv[1] = 1;
        hsv[2] = 1;
        for (int i = 0; i < 8; i++) {
            CIRCLE_8_AX[i] = (float) (CIRCLE_8_X[i] - Math.cos(CIRCLE_8_THETA[i]) * SPLINE_8);
            CIRCLE_8_BX[i] = (float) (CIRCLE_8_X[i] + Math.cos(CIRCLE_8_THETA[i]) * SPLINE_8);
            CIRCLE_8_AY[i] = (float) (CIRCLE_8_Y[i] - Math.sin(CIRCLE_8_THETA[i]) * SPLINE_8);
            CIRCLE_8_BY[i] = (float) (CIRCLE_8_Y[i] + Math.sin(CIRCLE_8_THETA[i]) * SPLINE_8);
            hsv[0] = i * 360 / 8f;
            TEST_COLORS[i] = 0x80FFFFFF & Color.HSVToColor(hsv);
        }
    }

    private static final int MIN_PERIOD = 1500;
    private static final int MAX_PERIOD = 2500;

    private static final float WOBBLE_AMOUNT = 0.035f;

    private int[] rotationPeriods;

    private Path path;

    private Paint testPaint;

    public Bubble() {
        rotationPeriods = new int[8];
        for (int i = 0; i < 8; i++) {
            rotationPeriods[i] = MathUtils.random(MIN_PERIOD, MAX_PERIOD);
        }
        path = new Path();
        testPaint = new Paint();
        testPaint.setAntiAlias(true);
    }

    public void draw(Canvas canvas, Paint paint, float size, long t) {
        path.reset();
        float iTheta, jTheta, iX, iY, jX, jY;
        for (int i = 0; i < 8; i++) {
            int j = (i + 1) % 8;
            iTheta = MathUtils.cycle(t, rotationPeriods[i]) * MathUtils.TWO_PI;
            jTheta = MathUtils.cycle(t, rotationPeriods[j]) * MathUtils.TWO_PI;
            iX = (float) (Math.cos(iTheta) * size * WOBBLE_AMOUNT);
            iY = (float) (Math.sin(iTheta) * size * WOBBLE_AMOUNT);
            jX = (float) (Math.cos(jTheta) * size * WOBBLE_AMOUNT);
            jY = (float) (Math.sin(jTheta) * size * WOBBLE_AMOUNT);
            if (i == 0) {
                path.moveTo(CIRCLE_8_X[i] * size + iX, CIRCLE_8_Y[i] * size + iY);
            }
            path.cubicTo(CIRCLE_8_BX[i] * size + iX, CIRCLE_8_BY[i] * size + iY, CIRCLE_8_AX[j] * size + jX, CIRCLE_8_AY[j] * size + jY, CIRCLE_8_X[j] * size + jX, CIRCLE_8_Y[j] * size + jY);
        }
        path.close();
        canvas.drawPath(path, paint);
    }

}
