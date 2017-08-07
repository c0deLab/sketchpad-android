package cc.scottland.sketchpad.shapes;

import android.graphics.Canvas;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cc.scottland.sketchpad.CanvasView;
import cc.scottland.sketchpad.utils.Utils;

/**
 * Created by scottdonaldson on 8/4/17.
 */

public class Polygon implements Shape {

    public List<Point> points = new ArrayList<Point>();

    public Polygon() {}

    public Polygon(List<Point> points) {
        this.points = points;
    }

    public void setCanvasView(CanvasView cv) {}

    public void update(Cursor c, boolean isFinal) {}

    public void move(int x, int y) {
        for (Point p : points) p.move(x, y);
    }

    public Shape near(Point p) { return null; }

    public Shape clone() {
        List<Point> pts = new ArrayList<Point>();
        for (Point p : points) pts.add(p.clone());
        return new Polygon(pts);
    }

    public void draw(Canvas canvas) {}

    public void remove() {
        for (Point p : points) p.remove();
        points = new ArrayList<Point>();
    }

    public Point center() {
        Point c = new Point();
        for (Point p : points) {
            c.x += p.x;
            c.y += p.y;
        }
        c.x /= points.size();
        c.y /= points.size();
        return c;
    }

    public boolean isRegular() {

        int d = 0;

        for (int i = 0; i < points.size(); i++) {
            Point a = points.get(i);
            Point b = points.get(i < points.size() - 1 ? i + 1 : 0);

            if (i == 0) {
                d = Utils.distance(a, b);
            } else {
                // can't expect to have a perfectly regular polygon
                // with all the messiness of having to step toward it,
                // but we can say that if any side is more than 3 units
                // longer than another, it is NOT regular
                if (Math.abs(Utils.distance(a, b) - d) > 3) return false;
            }
        }

        return true;
    }

    private void stepTowardRegular() {

        Point c = center();

        Point p = points.get(0);
        Log.e("angle", Double.toString(Utils.angle(c, p)));

        // we don't want to move the 'start' point,
        // so start index at 1
//        for (int i = 1; i < points.size(); i++) {
//            Point p = points.get(i);
//            p.move(
//                p.x > c.x ? 1 : p.x == c.y ? 0 : -1,
//                p.y > c.y ? 1 : p.y == c.y ? 0 : -1
//            );
//        }
    }

    public void regularize() {
        Point c = center();
        Point p = points.get(0);
        double angle = Utils.angle(c, p);
        int distance = Utils.distance(c, p);

        for (int i = 1; i < points.size(); i++) {

            angle += (360 / points.size());
            angle = angle % 360;

            p = points.get(i);
            p.x = c.x + (int)(distance * Math.cos(Math.toRadians(angle)));
            p.y = c.y + (int)(distance * Math.sin(Math.toRadians(angle)));
        }
    }

    public boolean isTruePoint() { return false; }
}
