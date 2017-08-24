package cc.scottland.sketchpad.utils;

/**
 * Created by scottdonaldson on 7/12/17.
 */

import cc.scottland.sketchpad.shapes.Point;

public class Utils {

    public static float distance(Point p1, Point p2) {

        float dx = p1.x - p2.x;
        float dy = p1.y - p2.y;

        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    public static double dot(Point p1, Point p2) {

        return p1.x * p2.x + p1.y * p2.y;
    }

    public static double angle(Point p1, Point p2) {

        return Math.toDegrees(Math.atan2(p2.y - p1.y, p2.x - p1.x));
    }
}
