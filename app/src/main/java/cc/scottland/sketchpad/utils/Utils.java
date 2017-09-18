package cc.scottland.sketchpad.utils;

/**
 * Created by scottdonaldson on 7/12/17.
 */

import cc.scottland.sketchpad.shapes.Point;

public class Utils {

    public static final int DEGREES = 1;
    public static final int RADIANS = 2;

    public static float distance(Point p1, Point p2) {

        float dx = p1.x - p2.x;
        float dy = p1.y - p2.y;

        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Returns true if p2 is "left" of p1, relative to center
     * @param center
     * @param p1
     * @param p2
     * @return
     */
    public static boolean isLeft(Point center, Point p1, Point p2) {
        return ((p1.x-center.x)*(p2.y-center.y)-(p1.y-center.y)*(p2.x-center.x)) > 0;
    }

    public static boolean sameSign(float a, float b) {
        return a * b > 0.0f;
    }

    public static double angle(Point p1, Point p2) {

        return Math.toDegrees(Math.atan2(p2.y - p1.y, p2.x - p1.x));
    }

    public static double angle(Point p1, Point p2, int type) {

        if (type == DEGREES) {
            return angle(p1, p2);
        } else if (type == RADIANS) {
            return Math.atan2(p2.y - p1.y, p2.x - p1.x);
        }

        throw new Error("Type must be either Utils.DEGREES or Utils.RADIANS");

    }

    public static double nonNegativeDegree(double n) {
        while (n < 0) n += 360;
        return n;
    }

    public static float nonNegativeDegree(float n) {
        while (n < 0) n += 360;
        return n;
    }

    public static int nonNegativeDegree(int n) {
        while (n < 0) n += 360;
        return n;
    }
}
