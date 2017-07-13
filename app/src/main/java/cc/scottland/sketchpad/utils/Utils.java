package cc.scottland.sketchpad.utils;

/**
 * Created by scottdonaldson on 7/12/17.
 */

import cc.scottland.sketchpad.shapes.Point;

public class Utils {

    public static int distance(Point p1, Point p2) {
        int dx = p1.x - p2.x;
        int dy = p1.y - p2.y;
        return (int) (Math.sqrt(dx * dx + dy * dy));
    }

    public static double dot(Point p1, Point p2) {
        return p1.x * p2.x + p1.y * p2.y;
    }
}
