package cc.scottland.sketchpad.shapes;

/**
 * Created by scottdonaldson on 7/12/17.
 */

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cc.scottland.sketchpad.CanvasView;
import cc.scottland.sketchpad.utils.Utils;

public class Point implements Shape {

    public float x;
    public float y;
    public List<Line> lines = new ArrayList<Line>();

    private boolean active = false;

    public Point() {
        x = 0;
        y = 0;
    }

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update(Cursor c, boolean isFinal) {

        Point p = c.clone();

        for (Line line : lines) line.setActive(!isFinal);

        // we might want to *overwrite* this point with another,
        // as when moving the endpoint of a line to another point
        if (isFinal) {
            for (Shape shape : c.over()) {
                if (!shape.isTruePoint()) return;
                Point pt = (Point)shape;
                for (Line line : lines) {
                    if (this == line.p1) line.setP1(pt);
                    if (this == line.p2) line.setP2(pt);
                }
            }
        } else {
            this.x = p.x;
            this.y = p.y;
        }
    }

    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    /**
     * Removes all the lines associated with this point.
     */
    public void remove() {
        while (lines.size() > 0) lines.remove(0);
    }

    public Shape near(Point p) {
        return Utils.distance(p, this) < 12 ? this : null;
    }

    public Point clone() {
        Point p = new Point(x, y);
        return p;
    }

    public void draw(Canvas canvas, Paint p) { }

    public Polygon seek() {

        Point p = this;
        List<Point> pts = new ArrayList<Point>();

        if (p.lines.size() < 2) return null;

        Line l = p.lines.get(0);

        p = (l.p1 == p) ? l.p2 : l.p1;

        // add first two points
        pts.add(this);
        pts.add(p);

        int i = 0;

        while (p != this) {
            // if at any point, we've encountered an endpoint
            // (only connected to one line,
            // then it can't be a polygon
            if (p.lines.size() == 1) return null;

            // next line = not the last one
            l = p.lines.get((p.lines.get(0) == l) ? 1 : 0);
            // next point = not the last one
            p = (l.p1 == p) ? l.p2: l.p1;
            if (p != this) pts.add(p);
            i++;
        };

        // if we've exited the loop, then we found a polygon
        return new Polygon(pts);
    }

    public boolean isTruePoint() { return true; }

    public void setActive(boolean active) {
        for (Line line : lines) line.setActive(active);
        this.active = active;
    }
    public boolean isActive() { return active; }

    public void rotate(float angle, Point ref) {

        x -= ref.x;
        y -= ref.y;

        float s = (float)Math.sin(angle);
        float c = (float)Math.cos(angle);

        float xnew = x * c - y * s;
        float ynew = x * s + y * c;

        x = xnew;
        y = ynew;

        x += ref.x;
        y += ref.y;
    }

    public void scale(float factor, Point ref) {
        x -= ref.x;
        y -= ref.y;

        x = x * factor;
        y = y * factor;

        x += ref.x;
        y += ref.y;
    }
}
