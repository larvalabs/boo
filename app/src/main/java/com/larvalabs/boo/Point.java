package com.larvalabs.boo;

public class Point {

    public static final Point ZERO = new Point(0, 0);

    public float x, y;

    public Point() {
        x = 0;
        y = 0;
    }

    public Point(Point other) {
        x = other.x;
        y = other.y;
    }

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        if (Float.compare(point.x, x) != 0) return false;
        if (Float.compare(point.y, y) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}