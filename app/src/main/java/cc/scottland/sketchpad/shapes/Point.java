package cc.scottland.sketchpad.shapes;

/**
 * Created by scottdonaldson on 7/12/17.
 */

import android.graphics.Canvas;

import cc.scottland.sketchpad.utils.Utils;

public class Point implements Shape {

    public int x;
    public int y;

    public Point() {
        this.x = 0;
        this.y = 0;
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update(Cursor c, boolean isFinal) {
        this.x = c.x;
        this.y = c.y;
    }

    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
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
}
