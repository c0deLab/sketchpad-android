package cc.scottland.sketchpad.shapes;

import android.graphics.Canvas;

/**
 * Created by scottdonaldson on 7/12/17.
 */

public interface Shape {
    public void update(Cursor c, int x, int y, boolean isFinal);
    public void move(int x, int y);
    public Shape near(Point p, int x, int y);
    public Shape clone();
    public void draw(Canvas canvas, int x, int y);
    public void remove();
    public boolean isTruePoint();
}
