package cc.scottland.sketchpad.shapes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import cc.scottland.sketchpad.utils.Utils;

/**
 * Created by scottdonaldson on 8/23/17.
 */

public class Arc extends Circle {

    // two angles, in degrees
    public float start; // -180 <= start < 180
    public float end; // -180 < end <= 180
    public float sweep; // 0 <= sweep <= 360

    public boolean hasRadius = false;
    public boolean hasStart = false;
    public boolean hasEnd = false;

    private boolean wasLeft;
    private boolean clockwise = true;

    private boolean nearCenter = false;

    public Arc(float x, float y) {
        super(x, y);
    }

    public Arc(float x, float y, float r) {
        super(x, y, r);
    }

    public Arc(float x, float y, float r, float start, float end) {
        super(x, y, r);
        this.start = start;
        this.end = end;
        this.sweep = end - start;
    }

    public void setRadius(float r) {
        this.r = r;
        this.hasRadius = true;
    }

    public void setStart(float start) {
        this.start = Utils.nonNegativeDegree(start);
        hasStart = true;
    }

    public void setEnd(float end) {
        this.end = Utils.nonNegativeDegree(end);
        hasEnd = true;
        sweep = clockwise ? start - end : end - start;
        sweep = Utils.nonNegativeDegree(sweep);
    }

    @Override
    public void reset() {
        super.reset();
        nearCenter = false;
    }

    @Override
    public void update(Cursor c, boolean isFinal) {

        setActive(!isFinal);

        float angle = (float) Utils.angle(this, c.target());
        angle = Utils.nonNegativeDegree(angle);

        if (!hasRadius && !hasStart) {

            this.r = Utils.distance(this, c.target());
            this.start = angle;

            return;
        }

        boolean isLeft = Utils.isLeft(this, startPoint(), c.target());

        // changing directions?
        if (wasLeft && !isLeft || !wasLeft && isLeft) {
            // really, this should be very small, i.e. < 1...
            // but if moving quickly, could be higher.
            // will be in the neighborhood of 180 if going around opposite side of circle
            if (Math.abs(angle - start) < 50) {
                clockwise = !clockwise;
            }
        }

        wasLeft = isLeft;

        end = (float) Utils.angle(this, c.target());
        end = Utils.nonNegativeDegree(end);

        sweep = clockwise ? start - end : end - start;
        sweep = Utils.nonNegativeDegree(sweep);
    }

    @Override
    public Shape near(Point pt) {

        int minDistance = 30;

        // not in the right piece of pie
        double angle = Utils.angle(this, pt);
        angle = Utils.nonNegativeDegree(angle);

        if (Utils.distance(pt, startPoint()) < 12) {
            Generic g = new Generic(startPoint().x, startPoint().y, this);
            return g;
        }

        if (Utils.distance(pt, endPoint()) < 12) {
            Generic g = new Generic(endPoint().x, endPoint().y, this);
            return g;
        }

        // TODO?
        // nearCenter = Utils.distance(this, pt) < minDistance;

        float a = clockwise ? end : start;
        angle -= a;
        angle = Utils.nonNegativeDegree(angle);

        if (angle > sweep) return null;

        // too far away
        if (Math.abs(Utils.distance(pt, this) - r) > minDistance) return null;

        Point d = new Point(pt.x - x, pt.y - y);
        float m = Utils.distance(d, new Point());
        d.x *= r / m;
        d.y *= r / m;

        Generic g = new Generic(x + d.x, y + d.y, this);
        return g;
    }

    @Override
    public void draw(Canvas canvas, Paint p) {

        p.setColor(Color.WHITE);

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3);

        if (hasRadius && hasStart && hasEnd) {

            canvas.drawArc(
                    x - r,
                    y - r,
                    x + r,
                    y + r,
                    start,
                    end - start,
                    false,
                    p
            );

        } else {

            if (isActive()) {
                DashPathEffect dashPath = new DashPathEffect(new float[]{8, 8}, (float) 1.0);
                p.setPathEffect(dashPath);
            } else {
                p.setPathEffect(null);
            }

            // line from radius to start
            float sx = (float)(r * Math.cos(Math.toRadians(start)));
            float sy = (float)(r * Math.sin(Math.toRadians(start)));

            Path path = new Path();
            path.moveTo(x, y);
            path.lineTo(x + sx, y + sy);

            if (!hasRadius && !hasStart) canvas.drawPath(path, p);

            if (!hasStart || Math.abs(start - end) < 0.5) return;

            // arc from start or end (depending on direction)
            // over `sweep` degrees
            canvas.drawArc(
                    x - r,
                    y - r,
                    x + r,
                    y + r,
                    clockwise ? end : start,
                    sweep,
                    false,
                    p
            );

//            if (nearCenter) {
//
//                DashPathEffect dashPath = new DashPathEffect(new float[]{8, 8}, (float) 1.0);
//                p.setPathEffect(dashPath);
//
//                path = new Path();
//                path.moveTo(startPoint().x, startPoint().y);
//                path.lineTo(x, y);
//                path.lineTo(endPoint().x, endPoint().y);
//                canvas.drawPath(path, p);
//            }

            p.setPathEffect(null);
        }
    }

    @Override
    public Arc clone() {
        Arc a = new Arc(x, y);
        a.setRadius(r);
        a.setStart(start);
        a.setEnd(end);
        return a;
    }

    public Point startPoint() {
        float sx = (float)(r * Math.cos(Math.toRadians(start)));
        float sy = (float)(r * Math.sin(Math.toRadians(start)));
        Point p = new Point(x + sx, y + sy);
        return p;
    }

    public Point endPoint() {
        float sx = (float)(r * Math.cos(Math.toRadians(end)));
        float sy = (float)(r * Math.sin(Math.toRadians(end)));
        Point p = new Point(x + sx, y + sy);
        return p;
    }

    @Override
    public void rotate(float angle, Point ref) {

        super.rotate(angle, ref);

        start += Math.toDegrees(angle);
        start = Utils.nonNegativeDegree(start);
        end += Math.toDegrees(angle);
        end = Utils.nonNegativeDegree(end);
    }
}
