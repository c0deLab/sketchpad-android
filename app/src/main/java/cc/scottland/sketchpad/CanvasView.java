package cc.scottland.sketchpad;

import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Canvas;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toast;

import java.util.List;
import java.util.ArrayList;

import cc.scottland.sketchpad.shapes.Circle;
import cc.scottland.sketchpad.shapes.Cursor;
import cc.scottland.sketchpad.shapes.Generic;
import cc.scottland.sketchpad.shapes.Line;
import cc.scottland.sketchpad.shapes.Point;
import cc.scottland.sketchpad.shapes.Polygon;
import cc.scottland.sketchpad.shapes.Shape;

/**
 * Created by scottdonaldson on 7/12/17.
 */

public class CanvasView extends View {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<Shape> objects = new ArrayList<Shape>();

    private boolean isTouchDown = false;
    private Cursor cursor = new Cursor(this);
    private String action = "";

    public int x = 0;
    public int y = 0;

    private int bg = Color.BLACK;

    private Shape activeObj;

    private Context context;

    public CanvasView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    @Override
    public void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        paint.setColor(bg);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

        if (isTouchDown) cursor.draw(canvas);

        for (Shape object : objects) object.draw(canvas);

        paint.setColor(Color.WHITE);
        paint.setTextSize(24);
        canvas.drawText(Integer.toString(x) + ", " + Integer.toString(y), 24, 40, paint);

        invalidate();
        requestLayout();
    }

    public void init() {

        invalidate();
        requestLayout();
    }

    /**
     * If the given action is the current state action, turn it off.
     * Otherwise, set it to be the new state action.
     */
    public void toggleAction(String action) {

        if (is(action)) {
            this.action = "";
            return;
        }

        this.action = action;
    }

    private boolean is(String action) {
        return this.action == action;
    }

    public void onTouchStart(MotionEvent e) {
        isTouchDown = true;
        update(e, false);
    }

    public void onTouchEnd(MotionEvent e) {
        isTouchDown = false;
        cancel(e);
    }

    public void cancel(MotionEvent e) {
        update(e, true);
        toggleAction("");
        activeObj = null;
    }

    public void update(MotionEvent e, boolean isFinal) {

        int x = (int)e.getX();
        int y = (int)e.getY();

        // update cursor
        cursor.x = x;
        cursor.y = y;
        cursor.off();

        Point p = cursor.clone();
        p.toCanvasViewCoords();

        // determine if cursor is `near` any object
        for (Shape object : objects) {

            if (is("moving")) break; // but not if we're moving an object
            if (object == activeObj) break;

            Shape near = object.near(p);
            if (near == null) continue;

            cursor.on(near);
        }

        // update active object, if it exists
        if (activeObj != null) activeObj.update(cursor, isFinal);

        invalidate();
        requestLayout();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (!isTouchDown) return false;

        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
                return createCircle();
            case KeyEvent.KEYCODE_2:
                return createLine();
            case KeyEvent.KEYCODE_3:
                return moveObject();
            case KeyEvent.KEYCODE_4:
                return copyObject();
            case KeyEvent.KEYCODE_5:
                return deleteObject();
            case KeyEvent.KEYCODE_6:
                return makeRegular();
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    public void addObject(Shape s) {
        s.setCanvasView(this);
        objects.add(s);
    }

    public boolean createCircle() {

        if (this.is("drawing")) return false;

        this.toggleAction("drawing");

        Point pt = cursor.target();
        if (!cursor.isOn()) pt.toCanvasViewCoords();
        Circle c = new Circle(pt.x, pt.y, 0);
        addObject(c);
        activeObj = c;

        return true;
    }

    public boolean createLine() {

        if (is("drawing")) return false;

        toggleAction("drawing");

        Point p1 = cursor.target();
        p1.setCanvasView(this);
        if (!cursor.isOn()) p1.toCanvasViewCoords();
        Point p2 = p1.clone();

        Line line = new Line(p1, p2);
        addObject(line);
        activeObj = line;

        return true;
    }

    public boolean moveObject() {

        if (!cursor.isOn()) return false;

        toggleAction("moving");

        if (!is("moving") || !cursor.isOn()) return false;

        Point p = cursor.clone();
        p.toCanvasViewCoords();

        for (Shape object : objects) {
            Shape near = object.near(p);
            if (near == null) continue;
            activeObj = near;
        }

        if (activeObj != null) activeObj.update(cursor, false);

        return true;
    }

    public boolean copyObject() {

        if (!cursor.isOn()) return false;

        toggleAction("moving");

        if (!is("moving")) return false;

        Point p = cursor.clone();
        p.toCanvasViewCoords();

        for (Shape object : objects) {
            Shape near = object.near(p);
            if (near == null) continue;
            activeObj = near;
        }

        // must copy a Generic resulting from near object
        if (activeObj == null || !(activeObj instanceof Generic)) return false;

        Shape copy = ((Generic)activeObj).original.clone();

        Generic genericCopy = new Generic(
            cursor.target().x,
            cursor.target().y,
            copy
        );
        genericCopy.setCanvasView(this);

        objects.add(copy);
        activeObj = genericCopy;

        return true;
    }

    public boolean deleteObject() {

        if (is("moving") || is("drawing")) return false;

        List<Shape> remainingObjects = new ArrayList<Shape>();

        Point p = cursor.clone();
        p.toCanvasViewCoords();

        for (Shape object : objects) {

            // if it's a point with no lines, remove it
            if (object.isTruePoint() && ((Point)object).lines.size() == 0) {
                object.remove();
            }

            // if not near the object, keep it
            if (object.near(p) == null) {
                remainingObjects.add(object);
            // otherwise, remove it
            } else {
                object.remove();
            }
        }

        objects = remainingObjects;

        invalidate();
        requestLayout();

        return true;
    }

    public boolean makeRegular() {

        if (is("moving") || is("drawing")) return false;

        Point p = cursor.clone();
        p.toCanvasViewCoords();

        for (Shape object : objects) {

            Shape near = object.near(p);
            if (near == null) continue;

            if (near instanceof Generic) { // circle or line
                Log.e("near generic", near.toString());
            } else if (near instanceof Point) {
                Polygon poly = ((Point)near).seek();
                if (poly == null) {
                    Log.e("no polygon found", "nope");
                } else {
                    poly.regularize();
                    invalidate();
                    requestLayout();
//                    Log.e("polygon", poly.toString());
//                    Log.e("polygon pts", Integer.toString(poly.points.size()));
                }
                // only start seeking from closest point
                return false;
            }
        }

        return true;
    }
}