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
    float start;
    float end;

    public boolean hasRadius = false;
    public boolean hasStart = false;
    public boolean hasEnd = false;

    public static final int CLOCKWISE = 0;
    public static final int COUNTERCLOCKWISE = 1;

    public Arc(float x, float y) {
        super(x, y);
    }

    public Arc(float x, float y, int r) {
        super(x, y, r);
    }

    public Arc(float x, float y, int r, float start, float end) {
        super(x, y, r);
        this.start = start;
        this.end = end;
    }

    public void setRadius(int r) {
        this.r = r;
        this.hasRadius = true;
    }

    public void setStart(float start) {
        this.start = start;
        hasStart = true;
    }

    public void setEnd(float end) {
        this.end = end;
        hasEnd = true;
    }

    @Override
    public void update(Cursor c, boolean isFinal) {

        if (cv == null) throw new Error(this.toString() + " has empty CanvasView!");

        Point p = c.clone();
        p.toCanvasViewCoords();

        setActive(!isFinal);

        if (!hasRadius && !hasStart) {

            this.r = (int) Utils.distance(this, p);
            this.start = (float) Utils.angle(this, p);

        } else if (!hasEnd) {
            this.end = (float) Utils.angle(this, p);
        }
    }

    @Override
    public Shape near(Point pt) {

        if (cv == null) throw new Error(this.toString() + " has empty CanvasView!");

        int minDistance = 30;

        // not in the right piece of pie
        double angle = Utils.angle(this, pt);

        float a = start > end ? end : start;
        float b = start > end ? start : end;

        if (angle < a || angle > b) return null;

        // too far away
        if (Math.abs(Utils.distance(pt, this) - this.r) > minDistance) return null;

        Point d = new Point(pt.x - this.x, pt.y - this.y);
        float m = Utils.distance(d, new Point());
        d.x *= (float)this.r / m;
        d.y *= (float)this.r / m;

        Generic g = new Generic(x + d.x, y + d.y, this);
        g.setCanvasView(cv);
        return g;
    }

    @Override
    public void draw(Canvas canvas) {
        if (cv == null) throw new Error(this.toString() + " has empty CanvasView!");
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
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
            }

            // line from radius to start
            float sx = (float)(r * Math.cos(Math.toRadians(start)));
            float sy = (float)(r * Math.sin(Math.toRadians(start)));

            Path path = new Path();
            path.moveTo(x, y);
            path.lineTo(x + sx, y + sy);
            if (!hasRadius && !hasStart) canvas.drawPath(path, p);

            if (!hasStart) return;

            // arc from start to (temp) end
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
        }
    }

    @Override
    public Arc clone() {
        Arc a = new Arc(x, y);
        a.setCanvasView(cv);
        a.setRadius(r);
        a.setStart(start);
        a.setEnd(end);
        return a;
    }
}
