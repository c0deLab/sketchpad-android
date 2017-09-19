package cc.scottland.sketchpad.shapes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import cc.scottland.sketchpad.CanvasView;
import cc.scottland.sketchpad.utils.Utils;

/**
 * Created by scottdonaldson on 7/12/17.
 */

public class Line implements Shape {

    public Point p1;
    public Point p2;

    private boolean active;

    public Line(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;

        // add line to points
        p1.lines.add(this);
        p2.lines.add(this);
    }

    public void setP1(Point p) {
        if (p.lines.indexOf(this) < 0) p.lines.add(this);
        this.p1 = p;
    }

    public void setP2(Point p) {
        if (p.lines.indexOf(this) < 0) p.lines.add(this);
        this.p2 = p;
    }

    public void update(Cursor c, boolean isFinal) {

        setActive(!isFinal);

        if (!isFinal || !c.isOn()) {
            p2.update(c, isFinal);
            return;
        }

        // can only add to point-like...
        if (c.target().isTruePoint()) {

            p2 = c.target();
            p2.lines.add(this);
            return;

        }

        p2.x = c.target().x;
        p2.y = c.target().y;

        // ...or circle-like objects
//        } else if (c.target() instanceof Generic) {
//
//            Circle cir = (Circle) (((Generic)c.target()).original);
//
//            p2 = c.target();
//            p2.lines.add(this);
//
//            cir.points.add(p2);
//            cir.angles.add((float) Utils.angle(cir, c, Utils.RADIANS));
//
//            // TODO: circle updates with line attached to it
//            p2.circles.add(cir);
//        }
    }

    public void move(int dx, int dy) {
        p1.move(dx, dy);
        p2.move(dx, dy);
    }

    public void remove() {
        if (p1 != null) p1.lines.remove(this);
        if (p2 != null) p2.lines.remove(this);
        p1 = null;
        p2 = null;
    }

    private Point parametrize(double t) {
        return new Point(
            (int)(p1.x + t * (p2.x - p1.x)),
            (int)(p1.y + t * (p2.y - p1.y))
        );
    }

    public Shape near(Point pt) {

        int minDistance = 30;

        // close to an endpoint
        if (Utils.distance(pt, p1) < minDistance) return p1;
        if (Utils.distance(pt, p2) < minDistance) return p2;

        float dx = p2.x - p1.x;
        float dy = p2.y - p1.y;
        float l2 = dx * dx + dy * dy; // distance squared
        if (l2 == 0) return null;

        // project point onto line segment to get closest point
        float t;
        t = (pt.x - p1.x) * (p2.x - p1.x) + (pt.y - p1.y) * (p2.y - p1.y);
        t /= l2;

        if (t < 0 || t > 1) return null;

        Point closest = parametrize(t);

        float d = Utils.distance(pt, closest);

        if (d < minDistance) {
            Generic g = new Generic(closest.x, closest.y, this);
            return g;
        }

        return null;
    }

    public Line clone() {
        Point p1 = this.p1.clone();
        Point p2 = this.p2.clone();
        Line l = new Line(p1, p2);
        return l;
    }

    @Override
    public void draw(Canvas canvas, Paint p) {

        p.setColor(Color.WHITE);

        if (isActive()) {
            DashPathEffect dashPath = new DashPathEffect(new float[]{8, 8}, (float) 1.0);
            p.setPathEffect(dashPath);
        } else {
            p.setPathEffect(null);
        }

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3);
        Path path = new Path();
        path.moveTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        canvas.drawPath(path, p);
    }

    public boolean isTruePoint() { return false; }

    public void setActive(boolean active) { this.active = active; }
    public boolean isActive() { return active; }

    public void reset() {
        setActive(false);
    }

    public void rotate(float angle, Point ref) {
        p1.rotate(angle, ref);
        p2.rotate(angle, ref);
    }

    public void scale(float factor, Point ref) {
        p1.scale(factor, ref);
        p2.scale(factor, ref);
    }

    public boolean isVertical() {
        return Math.abs(p1.x - p2.x) < 1;
    }

    public boolean isHorizontal() {
        return Math.abs(p1.y - p2.y) < 1;
    }

    public void getInfo() {
        Log.e(this.toString(), "points...");
        p1.getInfo();
        p2.getInfo();
    }
}
