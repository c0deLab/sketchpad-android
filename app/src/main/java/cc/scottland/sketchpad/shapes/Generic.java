package cc.scottland.sketchpad.shapes;

import android.util.Log;

/**
 * Created by scottdonaldson on 7/12/17.
 */

public class Generic extends Point {

    public Shape original;

    public Generic(float x, float y, Shape original) {
        super(x, y);
        this.original = original;
        this.invisible = true;
    }

    @Override
    public void update(Cursor c, boolean isFinal) {

        // if a compound, just pass the update on through
        if (original instanceof Compound) {
            original.update(c, isFinal);
            return;
        }

        Point p = c.target();
        float dx = p.x - this.x;
        float dy = p.y - this.y;

        original.move((int)dx, (int)dy);

        original.setActive(!isFinal);

        x += dx;
        y += dy;
    }

    public void remove() {
        super.remove();
        this.original = null;
    }

    public boolean isTruePoint() { return false; }
}
