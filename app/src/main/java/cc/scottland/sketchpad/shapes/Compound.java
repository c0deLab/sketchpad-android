package cc.scottland.sketchpad.shapes;

import android.graphics.Canvas;
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

    public Compound(int x, int y) {
        super(x, y);
    }

    // TODO
    public Compound clone() {

        Compound c = new Compound(x, y);
        c.setCanvasView(cv);

        for (Shape shape : shapes) c.addShape(shape.clone());

        c.complete();

        return c;
    }

    public void move(int dx, int dy) {
        for (Shape shape : shapes) shape.move(dx, dx);
    }

    public Shape near(Point p) {

        if (cv == null) throw new Error(this.toString() + " has empty CanvasView!");

        for (Shape shape : shapes) {

            Shape closest = shape.near(p);

            if (closest != null) {

                Generic g = new Generic(p.x, p.y, this);

                x = p.x;
                y = p.y;

                g.setCanvasView(cv);

                return g;
            }
        }

        return null;
    }

    public void update(Cursor c, boolean isFinal) {

        if (!isComplete) return;

        Point p = c.target();
        p.toCanvasViewCoords();

        int dx = p.x - this.x;
        int dy = p.y - this.y;

        for (Shape shape : shapes) shape.setActive(!isFinal);

        for (Point point : points) point.move(dx, dy);

        super.update(c, isFinal);
    }

    public void draw(Canvas canvas) {
        for (Shape shape : shapes) {
            shape.setActive(!isComplete);
            shape.draw(canvas);
        }
    }

    public boolean isTruePoint() { return false; }

    public void setActive(boolean active) {
        this.active = active;
        for (Shape s : shapes) s.setActive(active);
    }

    public boolean isActive() { return active; }

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

    public void rotate(double angle, Point ref) {
        for (Point point : points) point.rotate(angle, ref);
    }

    public void scale(double factor, Point ref) {
        for (Point point : points) point.scale(factor, ref);
    }

    public void complete() { isComplete = true; }
}
