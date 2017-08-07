package cc.scottland.sketchpad.shapes;

import android.graphics.Canvas;

import cc.scottland.sketchpad.CanvasView;

/**
 * Created by scottdonaldson on 7/12/17.
 */

public interface Shape {
    public void setCanvasView(CanvasView cv);
    public void update(Cursor c, boolean isFinal);
    public void move(int x, int y);

    /**
     * Takes in a point already in CanvasView (not screen) coords.
     * @param p
     * @return
     */
    public Shape near(Point p);
    public Shape clone();
    public void draw(Canvas canvas);
    public void remove();
    public boolean isTruePoint();
}