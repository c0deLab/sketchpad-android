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

    public Cursor(CanvasView cv) { super(); this.cv = cv; }

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

    public void draw(Canvas canvas) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);

        canvas.drawLine(x - 48, y, x - 12, y, paint);
        canvas.drawLine(x + 48, y, x + 12, y, paint);
        canvas.drawLine(x, y - 12, x, y - 48, paint);
        canvas.drawLine(x, y + 12, x, y + 48, paint);

        canvas.drawCircle(
            isOn() ? (target().x + cv.x) : x,
            isOn() ? (target().y + cv.y) : y,
            3,
            paint
        );
    }

    public Cursor clone() {
        Cursor c = new Cursor(cv);
        c.x = x;
        c.y = y;
        if (isOn()) c.on(at);
        return c;
    }
}
