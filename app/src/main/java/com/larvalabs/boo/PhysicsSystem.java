package com.larvalabs.boo;

/**
 * Simulates a physical system with damping.
 */
public class PhysicsSystem {

    public static final float WIDTH = 360;

    // Larger value means that the bodies are heavier, harder to move/stop
    private static final float MASS = 1f;

    private static final float SPRING_STRENGTH = 250f;
    private static final float DAMPING = 0.75f;

    private static final float REPULSION_STRENGTH = 5000f;

    private static final float STEP = 1 / 60f;

    private static class Body {

        Point pos;
        Point vel;
        Point force;
        float mass;
        float size;
        float originalSize;

        Point springPos;
        Point springOffset;
        Point lastForce = new Point(0, 0);

        Body(float size) {
            pos = new Point(0, 0);
            vel = new Point(0, 0);
            force = new Point(0, 0);
            springPos = new Point(0, 0);
            springOffset = new Point(0, 0);
            this.size = size;
            originalSize = size;
            mass = MASS / 100f;
        }

        @Override
        public String toString() {
            return String.format("P(%2.3f, %2.3f) V(%2.3f, %2.3f)", pos.x, pos.y, vel.x, vel.y);
        }

        float distance(Body other) {
            float d = (float) Math.hypot(pos.x - other.pos.x, pos.y - other.pos.y);
            return Math.max(1f, d - size - other.size);
        }

    }

    private int n;
    private Body[] bodies;
    private float springStrength, repulsionStrength;
    private float repulsionFactor = 1;

    private double lastUpdate = -1;

    private float scale;

    public PhysicsSystem(int width, float... sizes) {
        scale = width / WIDTH;
        init(sizes);
    }

    public void setSpringOffset(int index, float x, float y) {
        Body body = bodies[index];
        body.springOffset.x = x / scale;
        body.springOffset.y = y / scale;
    }

    public void init(float[] sizes) {
        n = sizes.length;
        bodies = new Body[n];
        for (int i = 0; i < n; i++) {
            bodies[i] = new Body(sizes[i] / scale);
            float angle = MathUtils.random(0, MathUtils.TWO_PI);
            bodies[i].pos.x = (float) (Math.cos(angle) * 2 * WIDTH);
            bodies[i].pos.y = (float) (Math.sin(angle) * 2 * WIDTH);
        }
        springStrength = SPRING_STRENGTH;
        repulsionStrength = REPULSION_STRENGTH;
    }

    public void update() {
        double time = System.currentTimeMillis() / (1000d);
        float damping = DAMPING;
        float springForceX, springForceY;
        float dampingForceX, dampingForceY;
        float repulsiveForceX, repulsiveForceY;
        float distance, repulsion, distanceSum;
        if (lastUpdate >= 0) {
            float t = STEP;
            for (int i = 0; i < bodies.length; i++) {
                Body body = bodies[i];
                for (int j = 0; j < bodies.length; j++) {
                    Body other = bodies[j];
                    if (body != other) {
                        distance = body.distance(other);
                        repulsion = repulsionStrength * repulsionFactor / distance;
                        distanceSum = Math.max(1, Math.abs(body.pos.x - other.pos.x) + Math.abs(body.pos.y - other.pos.y));
                        repulsiveForceX = (body.pos.x - other.pos.x) * repulsion / distanceSum;
                        repulsiveForceY = (body.pos.y - other.pos.y) * repulsion / distanceSum;
//                        Util.log("Repulsion = " + repulsion + " -> " + repulsiveForceX + ", " + repulsiveForceY);
                        body.force.x += repulsiveForceX;
                        body.force.y += repulsiveForceY;
                    }
                }
                springForceX = -springStrength * body.mass * (body.pos.x - (body.springPos.x + body.springOffset.x));
                springForceY = -springStrength * body.mass * (body.pos.y - (body.springPos.y + body.springOffset.y));
                dampingForceX = -damping * body.vel.x;
                dampingForceY = -damping * body.vel.y;
                body.vel.x += t * (springForceX + dampingForceX + body.force.x) / body.mass;
                body.vel.y += t * (springForceY + dampingForceY + body.force.y) / body.mass;
                body.pos.x += t * body.vel.x;
                body.pos.y += t * body.vel.y;
                body.lastForce.x = body.force.x;
                body.lastForce.y = body.force.y;
                body.force.x = 0;
                body.force.y = 0;
            }
        }
        lastUpdate = time;
    }

    public void setSpringStrength(float factor) {
        springStrength = SPRING_STRENGTH * factor;
    }

    public void setRepulsionStrength(float factor) {
        repulsionStrength = REPULSION_STRENGTH * factor;
    }

    public void setRepulsionFactor(float repulsionFactor) {
        this.repulsionFactor = repulsionFactor;
    }

    public void moveTo(int index, float x, float y) {
        Body body = bodies[index];
        body.springPos.x = x;
        body.springPos.y = y;
    }

    public void forceTo(int index, float x, float y) {
        Body body = bodies[index];
        body.springPos.x = x;
        body.springPos.y = y;
        body.pos.x = x;
        body.pos.y = y;
    }

    public void scaleSize(int index, float factor) {
        Body body = bodies[index];
        body.size = body.originalSize * factor;
    }

    public void getOffset(int index, Point pos) {
        pos.x = scale * bodies[index].pos.x;
        pos.y = scale * bodies[index].pos.y;
    }

    public Point getLastForce(int index) {
        return bodies[index].lastForce;
    }

}
