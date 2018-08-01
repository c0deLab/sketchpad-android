package cc.scottland.sketchpad.shapes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cc.scottland.sketchpad.CanvasView;

/**
 * Created by scottdonaldson on 8/22/17.
 */

public class Compound extends Point {

    public List<Shape> shapes = new ArrayList<Shape>();
    public List<Point> points = new ArrayList<Point>();
    private boolean isComplete = false;
    private boolean active;

    public Compound(float x, float y) {
        super(x, y);
        invisible = true;
    }

    public Compound clone() {

        Compound c = new Compound(x, y);

        for (Shape shape : shapes) c.addShape(shape.clone());

        c.complete();

        return c;
    }

    public void move(int dx, int dy) {
        // this... doesn't do anything??
        // for (Shape shape : shapes) shape.move(dx, dx);
    }

    public Shape near(Point p) {

        for (Shape shape : shapes) {

            Shape closest = shape.near(p);

            if (closest != null) {

                Generic g = new Generic(p.x, p.y, this);

                x = p.x;
                y = p.y;

                return g;
            }
        }

        return null;
    }

    public void update(Cursor c, boolean isFinal) {

        if (!isComplete) return;

        setActive(!isFinal);

        Point p = c.target();

        int dx = (int)(p.x - this.x);
        int dy = (int)(p.y - this.y);

        // don't need this here -- we update it in .draw()
        // for (Shape shape : shapes) shape.setActive(!isFinal);

        for (Point point : points) point.move(dx, dy);

        super.update(c, isFinal);
    }

    public void draw(Canvas canvas, Paint p) {

        p.setColor(Color.WHITE);

        for (Shape shape : shapes) {

            if (!isComplete) {
                shape.setActive(!isComplete);
            } else {
                shape.setActive(active);
            }

            shape.draw(canvas, p);
        }
    }

    public boolean isTruePoint() { return false; }

    public void setActive(boolean active) {
        this.active = active;
        for (Shape s : shapes) s.setActive(active);
    }

    public boolean isActive() { return active; }

    public void reset() {
        setActive(false);
        for (Shape s : shapes) s.reset();
    }

    public void addShape(Shape s) {

        shapes.add(s);

        if (s instanceof Circle) {
            if (points.indexOf(s) < 0) points.add((Point)s);
        } else if (s instanceof Line) {
            Line l = (Line)s;
            if (points.indexOf(l.p1) < 0) points.add(l.p1);
            if (points.indexOf(l.p2) < 0) points.add(l.p2);
        }
    }

    public void remove() {
        for (Shape shape : shapes) shape.remove();
    }

    public void rotate(float angle, Point ref) {
        for (Point point : points) point.rotate(angle, ref);
    }

    public void scale(float factor, Point ref) {
        for (Point point : points) point.scale(factor, ref);
    }

    public void complete() { isComplete = true; }
}
