package com.larvalabs.boo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public class Creature {

    private static final long NOTICING_TIME = 100;
    private static final long SCARED_TIME = 400;
    private static final float EYE_SCALE = 0.13f;

    private static final float GROW_CHANCE = 0.0002f;
    private static final long GROW_TIME = 4000;
    private static final long POP_TIME = 400;
    private static final Interpolator POP_INTERPOLATOR = new EaseOutElasticInterpolator();

    private enum Mode {

        OUT,
        RETURNING,
        IN,
        NOTICING,
        SCARED,
        ANTICIPATING,
        LEAVING;

        public boolean hiding() {
            return this == OUT || this == LEAVING || this == SCARED || this == ANTICIPATING || this == NOTICING;
        }

    }

    private enum BehaviorType {
        MOVE,
        GROW
    }

    private static class Behavior {

        BehaviorType type;

        long startTime;
        Point position;
        float size;

        private Behavior(Point position, long t) {
            this.position = position;
            type = BehaviorType.MOVE;
            startTime = t;
        }

        private Behavior(float size, long t) {
            this.size = size;
            type = BehaviorType.GROW;
            startTime = t;
        }

    }

    private Mode mode;
    private Behavior behavior = null;
    protected int bodyColor;
    private int eyeColor;
    private float originalBodySize;
    protected float bodySize;
    private float eyeSize;
    protected Paint paint;

    private Bubble bubble;
    private Eyes eyes;

    private PhysicsSystem system;
    private int index;

    private long startTime;

    private long scareTime = -1;

    private float escapeAngle;

    private Point position = new Point(0, 0);

    private long comeBackTime = 0;

    private CreatureInteraction creatureInteraction;

    public Creature(Context context, float bodySize, PhysicsSystem system, int index, CreatureInteraction creatureInteraction) {
        bodyColor = context.getResources().getColor(R.color.body_color);
        eyeColor = context.getResources().getColor(R.color.eye_color);
        mode = Mode.OUT;
        this.bodySize = bodySize;
        originalBodySize = bodySize;
        this.creatureInteraction = creatureInteraction;
        eyeSize = bodySize * EYE_SCALE;
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        this.system = system;
        this.index = index;
        bubble = new Bubble();
        eyes = new Eyes(this, creatureInteraction);
        startTime = System.currentTimeMillis();
    }

    public float getBodySize() {
        return bodySize;
    }

    public void draw(Canvas canvas, float screenWidth, float screenHeight, boolean doEffects) {
        long time = System.currentTimeMillis();
        long t = time - startTime;
        canvas.save();
        if (mode == Mode.RETURNING) {
            if (t > comeBackTime) {
                system.setSpringStrength(1f);
                system.moveTo(index, 0, 0);
                system.setRepulsionStrength(1f);
                mode = Mode.IN;
                creatureInteraction.creatureArrived(this, t);
            }
        } else if (mode == Mode.NOTICING) {
            if (t - scareTime > NOTICING_TIME) {
                scareTime = t;
                mode = Mode.SCARED;
            }
        } else if (mode == Mode.SCARED) {
            system.setRepulsionStrength(0.3f);
            //system.moveTo(index, anticipateX, anticipateY);
            mode = Mode.ANTICIPATING;
        } else if (mode == Mode.ANTICIPATING) {
            if (t - scareTime > SCARED_TIME) {
                mode = Mode.LEAVING;
            }
        } else if (mode == Mode.LEAVING) {
            float escapeX = (float) (PhysicsSystem.WIDTH * 2 * Math.cos(escapeAngle));
            float escapeY = (float) (PhysicsSystem.WIDTH * 2 * Math.sin(escapeAngle));
            system.setRepulsionStrength(1f);
            system.setSpringStrength(2);
            system.moveTo(index, escapeX, escapeY);
            mode = Mode.OUT;
        } else if (mode == Mode.IN && behavior == null) {
            if (doEffects && Math.random() < GROW_CHANCE) {
                behavior = new Behavior(MathUtils.random(2f, 3f), t);
                creatureInteraction.notice(this, t);
            }
        }
        if (behavior != null) {
            if (behavior.type == BehaviorType.GROW) {
                long elapsed = t - behavior.startTime;
                float scale;
                if (elapsed < GROW_TIME) {
                    scale = MathUtils.map(elapsed, 0, GROW_TIME, 1, behavior.size, Util.PATH_CURVE);
                } else if (elapsed < GROW_TIME + POP_TIME) {
                    scale = MathUtils.map(elapsed, GROW_TIME, GROW_TIME + POP_TIME, behavior.size, 1, POP_INTERPOLATOR);
                } else {
                    scale = 1;
                    behavior = null;
                }
                resize(scale);
            }
        }
        // Offset based on springs
        system.getOffset(index, position);
        canvas.translate(screenWidth / 2, screenHeight / 2);
        canvas.translate(position.x, position.y);
        doDraw(canvas, t);

        canvas.restore();
    }

    public void doDraw(Canvas canvas, long t) {
        //// Draw body
        paint.setColor(bodyColor);
        bubble.draw(canvas, paint, bodySize, t);

        //// Draw eyes
        paint.setColor(eyeColor);
        eyes.draw(canvas, paint, bodySize, eyeSize, t);
    }

    private void resize(float scale) {
        bodySize = originalBodySize * scale;
        eyeSize = bodySize * EYE_SCALE;
        system.scaleSize(index, scale);
    }

    public void reorient() {
        reorient(MathUtils.random(0, MathUtils.TWO_PI));
    }

    public void reorient(float angle) {
        escapeAngle = angle;
        float escapeX = (float) (PhysicsSystem.WIDTH * 2 * Math.cos(escapeAngle));
        float escapeY = (float) (PhysicsSystem.WIDTH * 2 * Math.sin(escapeAngle));
        system.forceTo(index, escapeX, escapeY);
    }

    public void comeBack(long delay) {
        if (mode.hiding()) {
            comeBackTime = System.currentTimeMillis() - startTime + delay;
            mode = Mode.RETURNING;
            eyes.comingBack(System.currentTimeMillis() - startTime);
        }
    }

    public void hide() {
        if (!mode.hiding()) {
            Point lastForce = system.getLastForce(index);
            if (lastForce.x == 0 && lastForce.y == 0) {
                escapeAngle = MathUtils.random(0, MathUtils.TWO_PI);
            } else {
                escapeAngle = (float) Math.atan2(lastForce.y, lastForce.x);
            }
            mode = Mode.NOTICING;
            scareTime = System.currentTimeMillis() - startTime;
            eyes.getScared(scareTime);
        }
    }

    public float getAngleTo(Creature other) {
        return (float) Math.atan2(other.position.y - position.y, other.position.x - position.x);
    }

    public void lookIfAble(long t, Creature other) {
        eyes.lookIfAble(t, other);
    }

}
