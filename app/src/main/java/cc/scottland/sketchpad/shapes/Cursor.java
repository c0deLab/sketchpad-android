package cc.scottland.sketchpad.shapes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import cc.scottland.sketchpad.CanvasView;

/**
 * Created by scottdonaldson on 7/12/17.
 */

public class Cursor extends Point {

    private Shape at;

    public Cursor() { super(); }

    public Cursor(int x, int y) { super(x, y); }

    public void on(Shape p) {
        this.at = p;
    }

    public void off() {
        this.at = null;
    }

    public boolean isOn() {
        return this.at != null;
    }

    public Point target() {
        return (Point) (isOn() ? this.at : clone());
    }

    public boolean isTruePoint() { return false; }

    public void draw(Canvas canvas, Paint p) {

        p.setColor(Color.WHITE);
        p.setStrokeWidth(3);

        canvas.drawLine(x - 48, y, x - 12, y, p);
        canvas.drawLine(x + 48, y, x + 12, y, p);
        canvas.drawLine(x, y - 12, x, y - 48, p);
        canvas.drawLine(x, y + 12, x, y + 48, p);

        canvas.drawCircle(
            isOn() ? target().x : x,
            isOn() ? target().y : y,
            3,
            p
        );
    }

    public Point clone() {
        return new Point(x, y);
    }
}
