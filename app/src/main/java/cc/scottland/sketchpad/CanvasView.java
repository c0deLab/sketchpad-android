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

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

import cc.scottland.sketchpad.shapes.Arc;
import cc.scottland.sketchpad.shapes.Circle;
import cc.scottland.sketchpad.shapes.Compound;
import cc.scottland.sketchpad.shapes.Cursor;
import cc.scottland.sketchpad.shapes.Generic;
import cc.scottland.sketchpad.shapes.Line;
import cc.scottland.sketchpad.shapes.Point;
import cc.scottland.sketchpad.shapes.Polygon;
import cc.scottland.sketchpad.shapes.Shape;
import cc.scottland.sketchpad.utils.Utils;

/**
 * Created by scottdonaldson on 7/12/17.
 */

public class CanvasView extends View {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public List<Shape> objects = new ArrayList<Shape>();

    private boolean isTouchDown = false;
    private Cursor cursor = new Cursor();
    private String action = "";

    public int x = 0;
    public int y = 0;

    private int bg = Color.BLACK;

    private Shape activeObj;

    private Context context;

    public CanvasView(Context context) {
        super(context);
        this.context = context;
        cursor.setCanvasView(this);
        init();
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        cursor.setCanvasView(this);
        init();
    }

    @Override
    public void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        paint.setColor(bg);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

        if (isTouchDown) cursor.draw(canvas, paint);

        for (Shape object : objects) {
            if (!isTouchDown) object.reset();
            object.draw(canvas, paint);
        }

        paint.setColor(Color.WHITE);
        paint.setTextSize(24);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(action.toUpperCase(), 24, 40, paint);

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

    /**
     * "Termination flick"
     * @param e
     */
    public void onTouchEnd(MotionEvent e) {

        isTouchDown = false;
        cancel(e);

//        for (Shape object : objects) object.reset();
//
//        invalidate();
//        requestLayout();
    }

    public void cancel(MotionEvent e) {
        update(e, true);
        toggleAction("");
        if (activeObj instanceof Compound) {
            ((Compound)activeObj).complete();
        }
        activeObj = null;
    }

    public void update(MotionEvent e, boolean isFinal) {

        int x = (int)e.getRawX();
        int y = (int)e.getRawY();

        // update cursor
        cursor.x = x;
        cursor.y = y;
        cursor.off();

        Point p = cursor.clone();

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

        if (keyCode == 29) return clearCanvas();

        if (keyCode == 49) return loadFile1();
        if (keyCode == 50) return loadFile2();
        if (keyCode == 51) return loadFile3();
        if (keyCode == 52) return loadFile4();

        if (!isTouchDown) return false;

        switch (keyCode) {
            case 46:
                return createCircle();
            case 45:
                return createLine();
            case 41:
                return moveObject();
            case 42:
                return copyObject();
            case 43:
                return deleteObject();
            case 38:
                return makeRegular();
            case 37:
                return makeCompound();
            case 47:
                return createArc();
            case 32:
                return horizontalConstraint();
            case 36:
                return verticalConstraint();
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    public void addObject(Shape s) {
        objects.add(s);
    }

    public void knob(int which, int val) {

        // which = 1: knob one, = 2: knob two

        if (!isTouchDown) return;

        Point p = cursor.clone();
        float rotateValue = (val * 0.05f);
        float scaleValue = (val * 0.05f + 1.f);

        // find nearest object
        Shape nearest = null;
        for (Shape object : objects) {
            Shape near = object.near(p);
            if (near != null) nearest = near;
        }

        if (nearest == null) return;

        if (!nearest.isTruePoint()) {

            // nearest must be generic
            Generic g = (Generic) nearest;

            if (which == 1) {
                action = "rotating";
                g.original.rotate(rotateValue, cursor.target());
            } else if (which == 2) {
                action = "scaling";
                g.original.scale(scaleValue, cursor.target());
            }
        // nearest is a point, check out all its lines
        } else {
            Point n = (Point)nearest;
            for (Line l : n.lines) {
                if (which == 1) {
                    action = "rotating";
                    Point other = n == l.p1 ? l.p2 : l.p1;
                    other.rotate(rotateValue, cursor.target());
                } else if (which == 2) {
                    action = "scaling";
                    Point other = n == l.p1 ? l.p2 : l.p1;
                    other.scale(scaleValue, cursor.target());
                }
            }
        }

        invalidate();
    }

    public boolean createCircle() {

        if (this.is("drawing")) return false;

        toggleAction("drawing");

        Point pt = cursor.target();
        Circle c = new Circle(pt.x, pt.y, 0);
        addObject(c);
        activeObj = c;

        return true;
    }

    public boolean createArc() {

        // if (this.is("drawing")) return false;

        // toggleAction("drawing");
        action = "drawing";

        Point pt = cursor.target();

        if (activeObj instanceof Arc) {

            Arc a = (Arc)activeObj;

            if (!a.hasRadius && !a.hasStart) {
                a.setRadius( (int) Utils.distance(a, pt) );
                a.setStart( (float) Utils.angle(a, pt) );
            }

            return true;
        }

        Arc a = new Arc(pt.x, pt.y, 0);

        addObject(a);
        activeObj = a;

        return true;
    }

    public boolean createLine() {

        if (is("drawing")) return false;

        toggleAction("drawing");

        Point p1 = cursor.target();
        Point p2 = p1.clone();

//        if (cursor.target() instanceof Generic && ((Generic)(cursor.target())).original instanceof Circle) {
//            Circle cir = ((Circle)((Generic)cursor.target()).original);
//            cir.addPoint(cursor.target(), (float) Utils.angle(cir, cursor.target(), Utils.RADIANS));
//        }

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

        for (Shape object : objects) {
            Shape near = object.near(p);
            if (near != null) {
                activeObj = near;
                break;
            }
        }

        if (activeObj != null) activeObj.update(cursor, false);

        return true;
    }

    public boolean copyObject() {

        if (!cursor.isOn()) return false;

        toggleAction("moving");

        if (!is("moving")) return false;

        Point p = cursor.clone();

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

        objects.add(copy);
        activeObj = genericCopy;

        return true;
    }

    public boolean deleteObject() {

        if (is("moving") || is("drawing")) return false;

        List<Shape> remainingObjects = new ArrayList<Shape>();

        Point p = cursor.clone();

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

    /**
     * Basically a depth-first search to look for a Polygon
     * from a given Point
     * @param start
     * @return
     */
    public Polygon seek(Point start) {

        // all the points connected to this point --
        // guaranteed to form a connected graph
        List<Point> points = new ArrayList<>();

        // DFS...
        Stack<Point> S = new Stack<>();

        start.mark = true;
        S.push(start);
        points.add(start);

        Point p;

        while (!S.empty()) {

            p = S.pop();

            for (Point q : p.getNeighboringPoints()) {

                if (!q.mark) {
                    q.mark = true;
                    S.push(q);
                    points.add(q);
                }
            }
        }

        // reset points
        for (Point q : points) q.mark = false;

        Polygon poly = new Polygon(points);

        return poly;
    }

    public boolean makeRegular() {

        if (is("moving") || is("drawing")) return false;

        Point p = cursor.clone();
        Polygon poly = null;

        for (Shape object : objects) {

            Shape near = object.near(p);
            if (near == null) continue;

            if (near instanceof Generic) { // circle or line

                Generic g = (Generic)near;
                if (g.original instanceof Circle) return false;

                poly = seek(((Line)(g.original)).p1);

            } else if (near instanceof Point) {
                poly = seek((Point) near);
            }
        }

        if (poly == null) return false;

        poly.regularize();

        for (int i = 0; i < poly.points.size() - 1; i++) {

            Point p1 = poly.points.get(i);
            Point p2 = poly.points.get(i + 1);

            boolean areConnected = false;

            for (Line l : p1.lines) {
                Point other = l.p1 == p1 ? l.p2 : l.p1;
                if (other == p2) areConnected = true;
            }

            if (!areConnected) {
                addObject(new Line(p1, p2));
            }
        }

        invalidate();
        requestLayout();

        return true;
    }

    public boolean makeCompound() {

        if (is("moving") || is("drawing")) return false;

        Point p = cursor.clone();

        Shape nearest = null;

        for (Shape object : objects) {
            Shape near = object.near(p);
            if (near != null) nearest = near;
        }

        if (nearest == null || nearest.isTruePoint()) return false;

        // guaranteed a generic object now
        Generic g = (Generic)nearest;

        // if we're just starting, add a new compound
        if (!is("making compound")) {

            Compound c = new Compound(p.x, p.y);
            c.addShape(g.original);
            addObject(c);

            activeObj = c;

        } else {
            ((Compound)activeObj).addShape(g.original);
        }

        objects.remove(g.original);

        action = "making compound";

        invalidate();
        requestLayout();

        return true;
    }

    public boolean clearCanvas() {
        objects = new ArrayList<Shape>();
        invalidate();
        requestLayout();
        return true;
    }

    public void lineConstraint(int which) {

        // 0 = horizontal, 1 = vertical

        if (is("moving") || is("drawing")) return;

        Point p = cursor.clone();

        for (Shape object : objects) {

            Shape near = object.near(p);

            // if near an object, and object is not a point
            if (near == null || near.isTruePoint()) continue;

            Generic g = (Generic)near;

            if (g.original instanceof Line) {

                Line l = (Line)(g.original);

                float mid;

                if (which == 0) {
                    mid = (l.p1.y + l.p2.y) / 2;
                    l.p1.y = mid;
                    l.p2.y = mid;
                } else if (which == 1) {
                    mid = (l.p1.x + l.p2.x) / 2;
                    l.p1.x = mid;
                    l.p2.x = mid;
                }

                // make short lines not so short
                if (Utils.distance(l.p1, l.p2) < 30) {
                    if (which == 0) {
                        while (Utils.distance(l.p1, l.p2) < 30) {
                            l.p1.x--;
                            l.p2.x++;
                        }
                    } else {
                        while (Utils.distance(l.p1, l.p2) < 30) {
                            l.p1.y--;
                            l.p2.y++;
                        }
                    }
                }
            }
        }

        invalidate();
        requestLayout();
    }

    public boolean horizontalConstraint() {
        lineConstraint(0);
        return true;
    }

    public boolean verticalConstraint() {
        lineConstraint(1);
        return false;
    }

    public boolean loadFile1() {

        clearCanvas();

        Arc a = new Arc(getWidth() / 2, getHeight() / 2, 400);
        a.setStart(315);
        a.setEnd(225);

        addObject(a);

        float x1 = getWidth() / 2 - (float) Math.sqrt(2) * 200;
        float x2 = x1 + (float) Math.sqrt(2) * 400;
        float y1 = getHeight() / 2 - (float) Math.sqrt(2) * 200;
        float y2 = y1 + (float) Math.sqrt(2) * 400;

        Point p1 = new Point(x1, y1);
        Point p2 = new Point(x2, y1);
        Point p3 = new Point(x1, y2);
        Point p4 = new Point(x2, y2);

        addObject(new Line(p1, p2));
        addObject(new Line(p1, p3));
        addObject(new Line(p2, p4));
        addObject(new Line(p3, p4));

        addObject(new Line(p2, p3));
        addObject(new Line(p1, p4));

        invalidate();
        requestLayout();

        return true;
    }

    public boolean loadFile2() {

        clearCanvas();

        float x1 = getWidth() / 2 - 300;
        float x2 = x1 + 600;
        float y1 = getHeight() / 2 - 300;
        float y2 = y1 + 300;

        Point p1 = new Point(x1, y1);
        Point p2 = new Point(x2, y1);
        Point p3 = new Point(x1, y2);
        Point p4 = new Point(x2, y2);

        addObject(new Line(p1, p2));
        addObject(new Line(p1, p3));
        addObject(new Line(p2, p4));
        addObject(new Line(p3, p4));

        Arc a = new Arc(x1, y2, 300);
        a.setStart(110);
        a.setEnd(70);
        addObject(a);

        Arc b = new Arc(x2, y2, 300);
        b.setStart(110);
        b.setEnd(70);
        addObject(b);

        Point p3c = p3.clone();

        addObject(new Line(p3c, a.startPoint()));
        addObject(new Line(p3c, a.endPoint()));

        Point p4c = p4.clone();

        addObject(new Line(p4c, b.startPoint()));
        addObject(new Line(p4c, b.endPoint()));

        return true;
    }

    public boolean loadFile3() {

        clearCanvas();

        // PACMAN

        float cx = getWidth() / 2;
        float cy = getHeight() / 2;

        Point p = new Point(cx + 200, cy);

        Arc a = new Arc(p.x, p.y, 300);
        a.setStart(160);
        a.setEnd(200);
        addObject(a);

        addObject(new Line(p, a.startPoint()));
        addObject(new Line(p, a.endPoint()));

        addObject(new Circle(p.x - 200, p.y, 14));
        addObject(new Circle(p.x - 300, p.y, 14));
        addObject(new Circle(p.x - 400, p.y, 14));
        addObject(new Circle(p.x - 500, p.y, 14));
        addObject(new Circle(p.x - 600, p.y, 14));
        addObject(new Circle(p.x - 700, p.y, 14));
        addObject(new Circle(p.x - 800, p.y, 14));

        return true;
    }

    public boolean loadFile4() {

        clearCanvas();

        float x1 = getWidth() / 2 - 300;
        float x2 = getWidth() / 2 - 50;
        float x3 = getWidth() / 2 + 50;
        float x4 = getWidth() / 2 + 300;

        float y1 = getHeight() / 2 - 400;
        float y2 = getHeight() / 2 - 200;
        float y3 = getHeight() / 2;
        float y4 = getHeight() / 2 + 200;
        float y5 = getHeight() / 2 + 400;

        Point p1 = new Point(x1, y2);
        Point p2 = new Point(x2, y1);
        Point p3 = new Point(x4, y2);
        Point p4 = new Point(x3, y3);

        Point p5 = new Point(x1, y4);
        Point p6 = new Point(x2, y3);
        Point p7 = new Point(x4, y4);
        Point p8 = new Point(x3, y5);

        // top square
        addObject(new Line(p1, p2));
        addObject(new Line(p2, p3));
        addObject(new Line(p3, p4));
        addObject(new Line(p4, p1));

        // connect top and bottom
        addObject(new Line(p1, p5));
        addObject(new Line(p2, p6));
        addObject(new Line(p3, p7));
        addObject(new Line(p4, p8));

        // bottom square
        addObject(new Line(p5, p6));
        addObject(new Line(p6, p7));
        addObject(new Line(p7, p8));
        addObject(new Line(p8, p5));

        return true;
    }
}