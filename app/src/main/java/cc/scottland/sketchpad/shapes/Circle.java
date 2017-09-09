package cc.scottland.sketchpad.shapes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.Log;

import cc.scottland.sketchpad.CanvasView;
import cc.scottland.sketchpad.utils.Utils;

/**
 * Created by scottdonaldson on 7/12/17.
 */

public class Circle extends Point {

    public float r;

    public Circle(float x, float y) {
        super(x, y);
        this.r = 0;
    }

    public Circle(float x, float y, float r) {
        super(x, y);
        this.r = r;
    }

    @Override
    public void update(Cursor c, boolean isFinal) {
        Point p = c.clone();
        setActive(!isFinal);
        this.r = (int)Utils.distance(p, this);
    }

    @Override
    public Shape near(Point pt) {

        int minDistance = 30;

        // too far away
        if (Math.abs(Utils.distance(pt, this) - this.r) > minDistance) return null;

        Point d = new Point(pt.x - this.x, pt.y - this.y);
        float m = Utils.distance(d, new Point());
        d.x *= (float)this.r / m;
        d.y *= (float)this.r / m;

        Generic g = new Generic(x + d.x, y + d.y, this);
        return g;
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
        canvas.drawCircle(this.x, this.y, r, p);
    }

    @Override
    public Circle clone() {
        Circle c = new Circle(x, y, r);
        return c;
    }

    public boolean isTruePoint() { return false; }

    public void scale(float factor, Point ref) {
        super.scale(factor, ref);
        r *= factor;
    }
}
