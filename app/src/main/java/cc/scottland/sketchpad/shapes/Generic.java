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
    public void update(Cursor c, int x, int y, boolean isFinal) {

        Point target = c.target();
        int dx = target.x - this.x + x;
        int dy = target.y - this.y + y;

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
