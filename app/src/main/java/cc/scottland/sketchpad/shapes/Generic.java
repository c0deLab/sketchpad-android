package cc.scottland.sketchpad.shapes;

import android.util.Log;

/**
 * Created by scottdonaldson on 7/12/17.
 */

public class Generic extends Point {

    public Shape original;

    public Generic(int x, int y, Shape original) {
        super(x, y);
        this.original = original;
    }

    @Override
    public void update(Cursor c, boolean isFinal) {

        if (cv == null) throw new Error(this.toString() + " has empty CanvasView!");

        Point p = c.target();
        p.toCanvasViewCoords();
        int dx = p.x - this.x;
        int dy = p.y - this.y;

        original.move(dx, dy);

        x += dx;
        y += dy;
    }

    public void remove() {
        super.remove();
        this.original = null;
    }

    public boolean isTruePoint() { return false; }
}
