package com.larvalabs.boo;

import android.graphics.Matrix;
import android.view.animation.Interpolator;

import java.util.List;
import java.util.Random;

public class MathUtils {

    public static final float SQRT_2 = (float) (Math.sqrt(2));
    public static final float TWO_PI = (float) (Math.PI * 2);
    public static final float PI = (float) (Math.PI);
    public static final float PI_OVER_2 = (float) (Math.PI / 2.0);
    public static final float PI_OVER_4 = (float) (Math.PI / 4.0);

    public static final float ROOT_PI_OVER_TWO = (float) (Math.sqrt(Math.PI / 2));

    public static float clamp(float x, float min, float max) {
        if (x < min) {
            x = min;
        } else if (x > max) {
            x = max;
        }
        return x;
    }

    public static float degreesToRadians(float degrees) {
        return degrees * PI / 180;
    }

    public static float radiansToDegrees(float degrees) {
        return 180 * degrees / PI;
    }

    public static float map(float x, float a, float b, float u, float v) {
        float p = (x - a) / (b - a);
        return u + p * (v - u);
    }

    public static long mapLong(double x, double a, double b, long u, long v) {
        double p = (x - a) / (b - a);
        return (long) (u + p * (v - u));
    }

    public static int mapInt(float x, float a, float b, int u, int v) {
        float p = (x - a) / (b - a);
        return (int) (u + p * (v - u));
    }

    public static int mapInt(double x, double a, double b, int u, int v) {
        double p = (x - a) / (b - a);
        return (int) (u + p * (v - u));
    }

    public static int mapInt(float x, float a, float b, int u, int v, Interpolator interpolator) {
        float p = interpolator.getInterpolation((x - a) / (b - a));
        return (int) (u + p * (v - u));
    }

    public static int clampedMapInt(float x, float a, float b, int u, int v) {
        if (x < a) {
            return u;
        } else if (x > b) {
            return v;
        }
        float p = (x - a) / (b - a);
        return (int) (u + p * (v - u));
    }

    public static int clampedMapInt(double x, double a, double b, int u, int v) {
        if (x < a) {
            return u;
        } else if (x > b) {
            return v;
        }
        double p = (x - a) / (b - a);
        return (int) (u + p * (v - u));
    }

    public static float clampedMap(float x, float a, float b, float u, float v) {
        if (x <= a) {
            return u;
        } else if (x >= b) {
            return v;
        }
        float p = (x - a) / (b - a);
        return u + p * (v - u);
    }

    public static double clampedMap(double x, double a, double b, double u, double v) {
        if (x <= a) {
            return u;
        } else if (x >= b) {
            return v;
        }
        double p = (x - a) / (b - a);
        return u + p * (v - u);
    }

    public static float clampedMap(float x, float a, float b, float u, float v, Interpolator interpolator) {
        if (x <= a) {
            return u;
        } else if (x >= b) {
            return v;
        }
        float p = interpolator.getInterpolation((x - a) / (b - a));
        return u + p * (v - u);
    }

    public static float map(float x, float a, float b, float u, float v, Interpolator interpolator) {
        float p = interpolator.getInterpolation((x - a) / (b - a));
        return u + p * (v - u);
    }

    public static float map(double x, double a, float b, float u, float v, Interpolator interpolator) {
        float p = interpolator.getInterpolation((float) ((x - a) / (b - a)));
        return (u + p * (v - u));
    }

    public static float cycle(long time, float period) {
        float v = time % period;
        return map(v, 0, period, 0, 1);
    }

    public static float cycle(long time, float period, float phaseShift) {
        double v = ((double) time + phaseShift * period) % period;
        if (v < 0) {
            v += period;
        }
        return map((float) v, 0, period, 0, 1);
    }

    // Go from 0 to 1 and back down again, but "hanging" out at 1 for a while.
    public static float hang(long time, float period) {
        float v = (time % period) * 2 * ROOT_PI_OVER_TWO / period - ROOT_PI_OVER_TWO;
        return (float) Math.cos(v * v);
    }

    // Smoothly oscillate from 0 to 1 and back
    public static float smoothPulse(long time, float period) {
        double v = MathUtils.clampedMap((double) time, 0, period, -PI, PI);
        return (float) (Math.cos(v) + 1) / 2;
    }

    public static float cycleBackwards(long time, float period) {
        return 1 - cycle(time, period);
    }

    public static float oscillate(long time, float period) {
        return (float) Math.sin((double) time * TWO_PI / (double) period);
    }

    public static float doubleOscillate(long time, float period1, float period2) {
        return (float) ((Math.sin((double) time * TWO_PI / (double) period1) + Math.sin((double) time * TWO_PI / (double) period2)) / 2);
    }

    // Phase shift is 1-based
    public static float oscillate(long time, float period, float phaseShift) {
        return (float) Math.sin(((time / (double) period) + phaseShift) * TWO_PI);
    }

    // Do a smooth pulse at start of a longer period
    public static float pulse(long time, float period, float pulseWidth, float phaseShift) {
        float t = cycle(time, period, phaseShift) * period / pulseWidth;
        if (t >= 1) {
            return 0;
        } else {
            return MathUtils.map((float) Math.cos(t * TWO_PI), -1, 1, 1, 0);
        }
    }

    public static float fractionalPart(float v) {
        return (float) (v - Math.floor(v));
    }

    public static int quadrant(long time, float period) {
        float v = time * TWO_PI / period;
        float x = (float) Math.cos(v);
        float y = (float) Math.sin(v);
        if (x > 0 && y > 0) {
            return 0;
        } else if (x < 0 && y > 0) {
            return 1;
        } else if (x < 0 && y < 0) {
            return 2;
        } else {
            return 3;
        }
    }

    public static int phase(long time, float period, int n) {
        float v = cycle(time, period);
        return (int) (v * n);
    }

    public static final Random RANDOM = new Random();

    public static float random(float min, float max) {
        return MathUtils.map(RANDOM.nextFloat(), 0, 1, min, max);
    }

    public static int random(int min, int max) {
        int i = MathUtils.mapInt(RANDOM.nextFloat(), 0, 1, min, max + 1);
        if (i == max + 1) {
            i = max;
        }
        return i;
    }

    public static long random(long min, long max) {
        long i = MathUtils.mapLong(RANDOM.nextDouble(), 0, 1, min, max + 1);
        if (i == max + 1) {
            i = max;
        }
        return i;
    }

    /**
     * Choose random numbers from 0 to n-1
     *
     * @param n       the choice limit.
     * @param results the array to put the choices. Infinite loop if length > n.
     */
    public static void choose(int n, int[] results) {
        int k = results.length;
        for (int i = 0; i < k; i++) {
            boolean done = false;
            while (!done) {
                results[i] = RANDOM.nextInt(n);
                done = true;
                for (int j = 0; j < i; j++) {
                    if (results[j] == results[i]) {
                        done = false;
                    }
                }
            }
        }
    }

    /**
     * Compute an overscroll-like soft clamping of a value between min and max.
     *
     * @param x
     * @param min
     * @param max
     * @param unit
     * @return
     */
    public static float overshoot(float x, float min, float max, float unit) {
        if (x > max) {
            float amount = (x - max) / unit;
            float adjusted = amount / (amount + 1);
            return adjusted * unit + max;
        } else if (x < min) {
            float amount = (min - x) / unit;
            float adjusted = amount / (amount + 1);
            return min - adjusted * unit;
        } else {
            // Still in range
            return x;
        }
    }

    private static float[] srcTriangle = new float[6];
    private static float[] dstTriangle = new float[6];

    private static float[] srcQuad = new float[8];
    private static float[] dstQuad = new float[8];

    // Get the transform that converts one triangle into another
    public static Matrix transformTriangle(
            float x11, float y11, float x21, float y21, float x31, float y31,
            float x12, float y12, float x22, float y22, float x32, float y32) {
        Matrix matrix = new Matrix();
        transformTriangle(x11, y11, x21, y21, x31, y31, x12, y12, x22, y22, x32, y32, matrix);
        return matrix;
    }

    public static void transformTriangle(float x11, float y11, float x21, float y21, float x31, float y31, float x12, float y12, float x22, float y22, float x32, float y32, Matrix matrix) {
        srcTriangle[0] = x11;
        srcTriangle[1] = y11;
        srcTriangle[2] = x21;
        srcTriangle[3] = y21;
        srcTriangle[4] = x31;
        srcTriangle[5] = y31;
        dstTriangle[0] = x12;
        dstTriangle[1] = y12;
        dstTriangle[2] = x22;
        dstTriangle[3] = y22;
        dstTriangle[4] = x32;
        dstTriangle[5] = y32;
        matrix.setPolyToPoly(srcTriangle, 0, dstTriangle, 0, 3);
    }

    public static void transformQuads(float x11, float y11, float x21, float y21, float x31, float y31, float x41, float y41, float x12, float y12, float x22, float y22, float x32, float y32, float x42, float y42, Matrix matrix) {
        srcQuad[0] = x11;
        srcQuad[1] = y11;
        srcQuad[2] = x21;
        srcQuad[3] = y21;
        srcQuad[4] = x31;
        srcQuad[5] = y31;
        srcQuad[6] = x41;
        srcQuad[7] = y41;
        dstQuad[0] = x12;
        dstQuad[1] = y12;
        dstQuad[2] = x22;
        dstQuad[3] = y22;
        dstQuad[4] = x32;
        dstQuad[5] = y32;
        dstQuad[6] = x42;
        dstQuad[7] = y42;
        matrix.setPolyToPoly(srcQuad, 0, dstQuad, 0, 4);
    }

    private static Point work = new Point();

    public static void applyToSphere(float x, float y, float size, float radius, float curvature, Matrix matrix) {
        float x11 = x - size;
        float y11 = y - size;
        float x21 = x + size;
        float y21 = y - size;
        float x31 = x + size;
        float y31 = y + size;
        float x41 = x - size;
        float y41 = y + size;
        mapToSphere(x11, y11, radius, curvature, work);
        float x12 = work.x;
        float y12 = work.y;
        mapToSphere(x21, y21, radius, curvature, work);
        float x22 = work.x;
        float y22 = work.y;
        mapToSphere(x31, y31, radius, curvature, work);
        float x32 = work.x;
        float y32 = work.y;
        mapToSphere(x41, y41, radius, curvature, work);
        float x42 = work.x;
        float y42 = work.y;
        transformQuads(x11, y11, x21, y21, x31, y31, x41, y41, x12, y12, x22, y22, x32, y32, x42, y42, matrix);
    }

    private static void mapToSphere(float x, float y, float radius, float curvature, Point result) {
        double theta = Math.atan2(y, x);
        double r = Math.min(1, Math.hypot(x, y) / radius);
        double newD = ((1 - curvature) * r + curvature * Math.sqrt(r)) * radius;
        result.x = (float) (Math.cos(theta) * newD);
        result.y = (float) (Math.sin(theta) * newD);
    }

    public static int constrain(int v, int min, int max) {
        return Math.min(max, Math.max(v, min));
    }

    public static boolean flip(float chance) {
        return RANDOM.nextFloat() < chance;
    }

    public static <T> T chooseAtRandom(List<T> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }

}
