package cc.scottland.sketchpad.shapes;

import android.graphics.Canvas;

/**
 * Created by scottdonaldson on 7/12/17.
 */

public interface Shape {
    public void update(Cursor c, boolean isFinal);
    public void move(int x, int y);
    public Shape near(Point p);
    public Shape clone();
    public void draw(Canvas canvas);
}
