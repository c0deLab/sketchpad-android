package cc.scottland.sketchpad.shapes;

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
        return (Point) (isOn() ? this.at : this.clone());
    }
}
