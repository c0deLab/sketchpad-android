package cc.scottland.sketchpad.shapes;

/**
 * Created by scottdonaldson on 7/12/17.
 */

import android.graphics.Canvas;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cc.scottland.sketchpad.CanvasView;
import cc.scottland.sketchpad.utils.Utils;

public class Point implements Shape {

    public int x;
    public int y;
    public List<Line> lines = new ArrayList<Line>();

    public CanvasView cv;
    private boolean inCanvasViewCoords = false;
    private boolean active = false;

    public Point() {
        x = 0;
        y = 0;
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setCanvasView(CanvasView cv) {
        this.cv = cv;
    }

    public void toCanvasViewCoords() {
        if (cv == null) throw new Error(this.toString() + " has empty CanvasView!");
        if (!inCanvasViewCoords) {
            inCanvasViewCoords = true;
            this.x -= cv.x;
            this.y -= cv.y;
        }
    }

    public void update(Cursor c, boolean isFinal) {
        if (cv == null) throw new Error(this.toString() + " has empty CanvasView!");
        Point p = c.clone();
        p.toCanvasViewCoords();
        this.x = p.x;
        this.y = p.y;
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
        if (cv == null) throw new Error(this.toString() + " has empty CanvasView!");
        return Utils.distance(p, this) < 12 ? this : null;
    }

    public Point clone() {
        Point p = new Point(x, y);
        p.setCanvasView(cv);
        return p;
    }

    public void draw(Canvas canvas) { }

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

    public void setActive(boolean active) { this.active = active; }
    public boolean isActive() { return active; }

    public void rotate(double angle, Point ref) {

        x -= ref.x;
        y -= ref.y;

        double s = Math.sin(angle);
        double c = Math.cos(angle);

        int xnew = (int)(x * c - y * s);
        int ynew = (int)(x * s + y * c);

        x = xnew;
        y = ynew;

        x += ref.x;
        y += ref.y;
    }

    public void scale(double factor, Point ref) {
        x -= ref.x;
        y -= ref.y;

        x = (int)(x * factor);
        y = (int)(y * factor);

        x += ref.x;
        y += ref.y;
    }
}
