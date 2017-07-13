package cc.scottland.sketchpad.shapes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import cc.scottland.sketchpad.utils.Utils;

/**
 * Created by scottdonaldson on 7/12/17.
 */

public class Line implements Shape {

    public Point p1;
    public Point p2;

    public Line(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public void update(Cursor c, boolean isFinal) {

        if (!isFinal || !c.isOn()) {
            p2.update(c, isFinal);
            return;
        }

        p2 = c.target();
    }

    public void move(int dx, int dy) {
        p1.move(dx, dy);
        p2.move(dx, dy);
    }

    private Point parametrize(double t) {
        return new Point(
            (int)(p1.x + t * (p2.x - p1.x)),
            (int)(p1.y + t * (p2.y - p1.y))
        );
    }

    public Shape near(Point pt) {

        int minDistance = 30;

        if (Utils.distance(pt, p1) < minDistance) return p1;
        if (Utils.distance(pt, p2) < minDistance) return p2;

        int dx = p2.x - p1.x;
        int dy = p2.y - p1.y;
        int l2 = dx * dx + dy * dy; // distance squared
        if (l2 == 0) return null;

        // project point onto line segment to get closest point
        double t;
        t = (pt.x - p1.x) * (p2.x - p1.x) + (pt.y - p1.y) * (p2.y - p1.y);
        t /= (double)l2;

        Point closest = parametrize(t);

        int d = Utils.distance(pt, closest);

        if (d < minDistance) return new Generic(closest.x, closest.y, this);

        return null;
    }

    public Shape clone() {
        return new Line((Point)p1.clone(), (Point)p2.clone());
    }

    @Override
    public void draw(Canvas canvas) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.WHITE);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3);
        canvas.drawLine(p1.x, p1.y, p2.x, p2.y, p);
    }
}
