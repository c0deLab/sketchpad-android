package cc.scottland.sketchpad.shapes;

import android.graphics.Canvas;
import android.graphics.Paint;
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
    private boolean active;

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

    public void draw(Canvas canvas, Paint p) {}

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

    public void regularize() {
        Point c = center();
        Point p = points.get(0);
        double angle = Utils.angle(c, p);
        float distance = Utils.distance(c, p);

        for (int i = 1; i < points.size(); i++) {

            angle += (360 / points.size());
            angle = angle % 360;

            p = points.get(i);
            p.x = c.x + (int)(distance * Math.cos(Math.toRadians(angle)));
            p.y = c.y + (int)(distance * Math.sin(Math.toRadians(angle)));
        }
    }

    public boolean isTruePoint() { return false; }

    public void setActive(boolean active) { this.active = active; }
    public boolean isActive() { return active; }

    public void reset() {
        setActive(false);
    }

    // TODO ? maybe it's ok that these are noops
    public void rotate(float angle, Point ref) {}
    public void scale(float factor, Point ref) {}

    public void getInfo() {
        Log.e(this.toString(), "TODO");
    }
}
