package cc.scottland.sketchpad.shapes;

/**
 * Created by scottdonaldson on 7/12/17.
 */

import android.graphics.Canvas;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cc.scottland.sketchpad.utils.Utils;

public class Point implements Shape {

    public int x;
    public int y;
    public List<Line> lines = new ArrayList<Line>();

    public Point() {
        x = 0;
        y = 0;
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update(Cursor c, boolean isFinal) {
        x = c.x;
        y = c.y;
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
        return new Point(x, y);
    }

//    public Generic clone(int x, int y) {
//        return new Generic(x, y, this);
//    }

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
}
