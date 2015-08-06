package com.larvalabs.boo;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

public class Eyes {

    private static final float SCARED_SCALE = 1.65f;
    private static final float EYE_CURVATURE = 0.25f;

    private enum BehaviorType {

        BLINK(1 / 4f/ 60f, 125, 75, 75),
        LOOK(1 / 3f/ 60f, 300, 500, 3000),
        SQUINT(1 / 10f / 60f, 300, 1000, 4000),
        NOTICING(0, 60, 0, 0),
        SCARED(0, 100, 500, 600),
        STARE(0, 20, 800, 2500);

        BehaviorType(float chance, long changeTime, long minDuration, long maxDuration) {
            this.chance = chance;
            this.changeTime = changeTime;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
        }

        float chance;
        long changeTime, minDuration, maxDuration;

        boolean shouldStart() {
            return MathUtils.flip(chance);
        }

    }

    private static class Behavior {
        BehaviorType type;
        long startTime;
        long holdTime;
        long stopTime;
        long endTime;
        float param;

        Behavior(long t, BehaviorType type, float param) {
            startTime = t;
            this.type = type;
            long duration = MathUtils.random(type.minDuration, type.maxDuration);
            holdTime = startTime + type.changeTime;
            stopTime = holdTime + duration;
            endTime = stopTime + type.changeTime;
            this.param = param;
        }

        boolean isDone(long t) {
            return t >= endTime;
        }

        float getProgress(long t) {
            if (t < holdTime) {
                return MathUtils.map(t, startTime, holdTime, 0, 1, Util.PATH_CURVE);
            } else if (t < stopTime) {
                return 1;
            } else if (t < endTime) {
                return MathUtils.map(t, stopTime, endTime, 1, 0, Util.PATH_CURVE);
            } else {
                return 0;
            }
        }

        void cancel(long t) {
            long newEnd = endTime;
            if (t < holdTime) {
                newEnd = t + (t - startTime);
            } else if (t < stopTime) {
                newEnd = t + (endTime - stopTime);
            }
            long delta = newEnd - endTime;
            startTime += delta;
            holdTime += delta;
            stopTime += delta;
            endTime += delta;
        }

    }

    private Matrix matrix = new Matrix();
    private Matrix workMatrix = new Matrix();

    private RectF workRect = new RectF();

    private Behavior look, blink, squint, notice, freakOut, stare;

    private Paint testPaint;

    private Creature creature;
    private CreatureInteraction creatureInteraction;
    private Creature lookTarget = null;

    public Eyes(Creature creature, CreatureInteraction creatureInteraction) {
        this.creature = creature;
        this.creatureInteraction = creatureInteraction;
        testPaint = new Paint();
        testPaint.setAntiAlias(true);
        testPaint.setColor(0x8000FFFF);
    }

    public void draw(Canvas canvas, Paint paint, float bodySize, float eyeSize, long t) {
        float cx = 0, cy = 0;
        float eyeScale = 1;
        float curvature = 0;
        float blinkAmount = 0;
        float squintLevel = 0;
        if (look != null) {
            if (look.isDone(t)) {
                look = null;
            } else {
                float p = look.getProgress(t);
                float theta;
                if (lookTarget == null) {
                    theta = look.param;
                } else {
                    theta = creature.getAngleTo(lookTarget);
                }
                cx = (float) (bodySize / 3 * Math.cos(theta) * p);
                cy = (float) (bodySize / 2 * Math.sin(theta) * p);
                curvature = MathUtils.map(p, 0, 1, 0, EYE_CURVATURE);
            }
        } else {
            // Won't look around if scared
            if (notice == null && freakOut == null  && stare == null && BehaviorType.LOOK.shouldStart()) {
                startLooking(t, null);
            }
        }
        if (notice != null) {
            if (notice.isDone(t)) {
                notice = null;
                freakOut = new Behavior(t, BehaviorType.SCARED, 0);
            }
        } else if (freakOut != null) {
            if (freakOut.isDone(t)) {
                freakOut = null;
            } else {
                float p = freakOut.getProgress(t);
                eyeScale = MathUtils.map(p, 0, 1, 1, SCARED_SCALE);
            }
        }
        if (stare != null) {
            if (stare.isDone(t)) {
                stare = null;
            }
        }
        if (blink != null) {
            if (blink.isDone(t)) {
                blink = null;
            } else {
                blinkAmount = blink.getProgress(t);
            }
        } else {
            if (notice == null && freakOut == null && BehaviorType.BLINK.shouldStart()) {
                blink = new Behavior(t, BehaviorType.BLINK, 0);
            }
        }
        if (squint != null) {
            if (squint.isDone(t)) {
                squint = null;
            } else {
                squintLevel = squint.getProgress(t);
            }
        } else {
            if (notice == null && freakOut == null && blink == null && BehaviorType.SQUINT.shouldStart()) {
                squint = new Behavior(t, BehaviorType.SQUINT, 0);
            }
        }
        if (squintLevel > 0) {
            blinkAmount = MathUtils.map(squintLevel, 0, 1, blinkAmount, 0.5f + blinkAmount / 2);
        }
        for (int i = 0; i < 2; i++) {
            float x = i == 0 ? cx - bodySize / 3 : cx + bodySize / 3;
            float y = cy;
            canvas.save();
            matrix.reset();
            MathUtils.applyToSphere(x, y, eyeSize, bodySize, curvature, matrix);
            canvas.getMatrix(workMatrix);
            workMatrix.preConcat(matrix);
            canvas.setMatrix(workMatrix);
            canvas.scale(eyeScale, eyeScale, x, y);
            if (blinkAmount > 0) {
                float top = MathUtils.clampedMap(blinkAmount, 0, 0.9f, y - eyeSize, y);
                float bottom = MathUtils.clampedMap(blinkAmount, 0, 0.9f, y + eyeSize, y);
                workRect.set(x - 2 * eyeSize, top, x + 2 * eyeSize, bottom);
                canvas.clipRect(workRect);
//                canvas.drawRect(workRect, testPaint);
            }
            if (blinkAmount < 0.9f) {
                canvas.drawCircle(x, y, eyeSize, paint);
            }
            canvas.restore();
        }

    }

    private void startLooking(long t, Creature target) {
        float theta;
        if (creatureInteraction.isNewArrival(t) || MathUtils.flip(0.7f) || target != null) {
            lookTarget = target == null ? creatureInteraction.getLookTarget(creature) : target;
            theta = creature.getAngleTo(lookTarget);
        } else {
            lookTarget = null;
            theta = MathUtils.random(0, MathUtils.TWO_PI);
        }
        look = new Behavior(t, BehaviorType.LOOK, theta);
    }

    public void getScared(long t) {
        if (look != null) {
            look.cancel(t);
        }
        if (stare != null) {
            stare.cancel(t);
        }
        if (blink != null) {
            blink.cancel(t);
        }
        if (squint != null) {
            squint.cancel(t);
        }
        notice = new Behavior(t, BehaviorType.NOTICING, 0);
    }

    public void comingBack(long t) {
        if (look != null) {
            look.cancel(t);
        }
        if (notice != null) {
            notice.cancel(t);
        }
        if (freakOut != null) {
            freakOut.cancel(t);
        }
        stare = new Behavior(t, BehaviorType.STARE, 0);
    }

    public void lookIfAble(long t, Creature other) {
        if (notice == null && freakOut == null && look == null) {
            if (stare != null) {
                stare.cancel(t);
            }
            startLooking(t, other);
        }
    }

}
