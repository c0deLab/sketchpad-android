package cc.scottland.sketchpad.shapes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import cc.scottland.sketchpad.utils.Utils;

/**
 * Created by scottdonaldson on 7/12/17.
 */

public class Circle extends Point {

    public int r;

    public Circle(int x, int y) {
        super(x, y);
        this.r = 0;
    }

    public Circle(int x, int y, int r) {
        super(x, y);
        this.r = r;
    }

    @Override
    public void update(Cursor c, int x, int y, boolean isFinal) {
        Point p = new Point(c.x - x, c.y - y);
        this.r = Utils.distance(p, this);
    }

    @Override
    public Shape near(Point pt, int x, int y) {

        pt.x += x;
        pt.y += y;

        int minDistance = 30;

        // too far away
        if (Math.abs(Utils.distance(pt, this) - this.r) > minDistance) return null;

        Point d = new Point(pt.x - this.x, pt.y - this.y);
        int m = Utils.distance(d, new Point());
        d.x *= (float)this.r / m;
        d.y *= (float)this.r / m;

        return new Generic(this.x + d.x, this.y + d.y, this);
    }

    @Override
    public void draw(Canvas canvas, int x, int y) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.WHITE);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3);
        canvas.drawCircle(this.x + x, this.y + y, r, p);
    }

    @Override
    public Circle clone() {
        return new Circle(x, y, r);
    }

    public boolean isTruePoint() { return false; }
}
