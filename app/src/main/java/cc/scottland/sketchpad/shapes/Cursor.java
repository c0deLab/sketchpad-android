package cc.scottland.sketchpad.shapes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cc.scottland.sketchpad.CanvasView;

/**
 * Created by scottdonaldson on 7/12/17.
 */

public class Cursor extends Point {

    private Shape at;
    private CanvasView cv;

    public Cursor() { super(); }

    public Cursor(int x, int y) { super(x, y); }

    public void setCanvasView(CanvasView cv) { this.cv = cv; }

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

        int toEnd = 64;
        int fromCenter = 16;

        canvas.drawLine(x - toEnd, y, x - fromCenter, y, p);
        canvas.drawLine(x + toEnd, y, x + fromCenter, y, p);
        canvas.drawLine(x, y - fromCenter, x, y - toEnd, p);
        canvas.drawLine(x, y + fromCenter, x, y + toEnd, p);

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

    /**
     * Find the shapes that the cursor is near (relative to the CanvasView).
     * @return
     */
    public List<Shape> over() {
        List<Shape> shapes = new ArrayList<Shape>();
        for (Shape object : cv.objects) {
            Shape near = object.near(this);
            if (near != null) shapes.add(near);
        }
        return shapes;
    }
}
