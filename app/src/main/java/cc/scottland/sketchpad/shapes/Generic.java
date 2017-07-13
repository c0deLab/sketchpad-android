package cc.scottland.sketchpad.shapes;

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

        Point target = c.target();
        int dx = target.x - x;
        int dy = target.y - y;

        original.move(dx, dy);

        x += dx;
        y += dy;
    }
}
