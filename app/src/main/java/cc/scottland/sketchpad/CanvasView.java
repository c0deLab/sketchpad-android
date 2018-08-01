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

    private Canvas canvas;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public List<Shape> objects = new ArrayList<Shape>();

    private boolean isTouchDown = false;
    private Cursor cursor = new Cursor();
    private String action = "";

    public int x = 0;
    public int y = 0;

    private Point p = null;
    private boolean isFinal = false;

    private int bg = Color.BLACK;

    public int whichKnob = -1;
    public int knobVal = 0;

    public static final int KNOB_LEFT = 1;
    public static final int KNOB_RIGHT = 2;

    private Shape activeObj;

    public CanvasView(Context context) {
        super(context);
        cursor.setCanvasView(this);
        init();
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        cursor.setCanvasView(this);
        init();
    }

    final Thread drawThread = new Thread(new Runnable() {

        public void run() {

            if (canvas == null) return;

            paint.setColor(bg);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

            if (isTouchDown) cursor.draw(canvas, paint);

            for (Shape object : objects) object.draw(canvas, paint);
        }
    });

    final Thread updateThread = new Thread(new Runnable() {

        public void run() {

            if (p == null) return;

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

            p = null;
        }
    });

    @Override
    public void onDraw(Canvas canvas) {

        this.canvas = canvas;

        drawThread.run();
    }

    public void init() {
        invalidate();
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
        invalidate();
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

        p = cursor.clone();
        this.isFinal = isFinal;

        updateThread.run();

        invalidate();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == 29) return clearCanvas();
        if (keyCode == 30) {
            System.exit(0);
            return false;
        }

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
                moveObject.run();
                return true;
            case 42:
                copyObject.run();
                return true;
            case 43:
                deleteObject.run();
                return true;
            case 38:
                return makeRegular();
            case 37:
                makeCompound.run();
                return true;
            case 47:
                return createArc();
            case 32:
                horizontalConstraint.run();
                return true;
            case 36:
                verticalConstraint.run();
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    public void addObject(Shape s) {
        objects.add(s);
    }

    public final Thread knob = new Thread(new Runnable() {

        public void run() {

            if (!isTouchDown) return;

            Point p = cursor.clone();
            float rotateValue = (knobVal * 0.05f);
            float scaleValue = (knobVal * 0.05f + 1.f);

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

                if (whichKnob == KNOB_LEFT) {
                    action = "rotating";
                    g.original.rotate(rotateValue, cursor.target());
                } else if (whichKnob == KNOB_RIGHT) {
                    action = "scaling";
                    g.original.scale(scaleValue, cursor.target());
                }
                // nearest is a point, check out all its lines
            } else {
                Point n = (Point)nearest;
                for (Line l : n.lines) {
                    if (whichKnob == KNOB_LEFT) {
                        action = "rotating";
                        Point other = n == l.p1 ? l.p2 : l.p1;
                        other.rotate(rotateValue, cursor.target());
                    } else if (whichKnob == KNOB_RIGHT) {
                        action = "scaling";
                        Point other = n == l.p1 ? l.p2 : l.p1;
                        other.scale(scaleValue, cursor.target());
                    }
                }
            }

            invalidate();
        }
    });

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

    final Thread moveObject = new Thread(new Runnable() {

        public void run() {

            if (!cursor.isOn()) return;

            toggleAction("moving");

            if (!is("moving") || !cursor.isOn()) return;

            p = cursor.clone();
            isFinal = false;

            for (Shape object : objects) {
                Shape near = object.near(p);
                if (near != null) {
                    activeObj = near;
                    break;
                }
            }

            if (activeObj != null) activeObj.update(cursor, false);

            p = null;
        }
    });

    final Thread copyObject = new Thread(new Runnable() {

        public void run() {

            if (!cursor.isOn()) return;

            toggleAction("moving");

            if (!is("moving")) return;

            p = cursor.clone();

            for (Shape object : objects) {
                Shape near = object.near(p);
                if (near == null) continue;
                activeObj = near;
            }

            // must copy a Generic resulting from near object
            if (activeObj == null || !(activeObj instanceof Generic)) return;

            Shape copy = ((Generic)activeObj).original.clone();

            Generic genericCopy = new Generic(
                    cursor.target().x,
                    cursor.target().y,
                    copy
            );

            objects.add(copy);
            activeObj = genericCopy;

            return;
        }
    });

    final Thread deleteObject = new Thread(new Runnable() {

        public void run() {

            if (is("moving") || is("drawing")) return;

            List<Shape> remainingObjects = new ArrayList<Shape>();

            p = cursor.clone();

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
        }
    });

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

        return true;
    }

    final Thread makeCompound = new Thread(new Runnable() {

        public void run() {
            if (is("moving") || is("drawing")) return;

            p = cursor.clone();

            Shape nearest = null;

            for (Shape object : objects) {
                Shape near = object.near(p);
                if (near != null) nearest = near;
            }

            if (nearest == null || nearest.isTruePoint()) return;

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
        }
    });

    public boolean clearCanvas() {
        objects = new ArrayList<Shape>();
        invalidate();
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
    }

    final Thread horizontalConstraint = new Thread(new Runnable() {
        public void run() { lineConstraint(0); }
    });

    final Thread verticalConstraint = new Thread(new Runnable() {
        public void run() { lineConstraint(1); }
    });

    public boolean loadFile1() {

        clearCanvas();

        int cx = getWidth () / 2;
        int cy = getHeight() / 3;

        Arc a = new Arc(cx, cy + 20, 400);
        a.setStart(315);
        a.setEnd(225);

        float x1 = cx - (float) Math.sqrt(2) * 200;
        float x2 = x1 + (float) Math.sqrt(2) * 400;
        float y1 = cy - (float) Math.sqrt(2) * 200 + 20;
        float y2 = y1 + (float) Math.sqrt(2) * 400 + 20;

        Point p1 = new Point(x1, y1);
        Point p2 = new Point(x2, y1);
        Point p3 = new Point(x1, y2);
        Point p4 = new Point(x2, y2);

        Line l1 = new Line(p1, p2);
        Line l2 = new Line(p1, p3);
        Line l3 = new Line(p2, p4);
        Line l4 = new Line(p3, p4);

        Line l5 = new Line(p2, p3);
        Line l6 = new Line(p1, p4);

        Compound c = new Compound(0, 0);
        c.addShape(a);
        c.addShape(l1);
        c.addShape(l2);
        c.addShape(l3);
        c.addShape(l4);
        c.addShape(l5);
        c.addShape(l6);

        c.complete();

        addObject(c);

        return true;
    }

    public boolean loadFile2() {

        clearCanvas();

        int cx = getWidth() / 2;
        int cy = getHeight() / 3;

        float x1 = cx - 300;
        float x2 = x1 + 600;
        float y1 = cy - 300 + 20;
        float y2 = y1 + 300 + 20;

        Point p1 = new Point(x1, y1);
        Point p2 = new Point(x2, y1);
        Point p3 = new Point(x1, y2);
        Point p4 = new Point(x2, y2);

        Line l1 = new Line(p1, p2);
        Line l2 = new Line(p1, p3);
        Line l3 = new Line(p2, p4);
        Line l4 = new Line(p3, p4);

        Arc a = new Arc(x1, y2, 300);
        a.setStart(110);
        a.setEnd(70);

        Arc b = new Arc(x2, y2, 300);
        b.setStart(110);
        b.setEnd(70);

        Point p3c = p3.clone();

        Line l5 = new Line(p3c, a.startPoint());
        Line l6 = new Line(p3c, a.endPoint());

        Point p4c = p4.clone();

        Line l7 = new Line(p4c, b.startPoint());
        Line l8 = new Line(p4c, b.endPoint());

        Compound c1 = new Compound(0, 0);
        c1.addShape(l1);
        c1.addShape(l2);
        c1.addShape(l3);
        c1.addShape(l4);
        c1.complete();
        addObject(c1);

        Compound c2 = new Compound(0, 0);
        c2.addShape(a);
        c2.addShape(l5);
        c2.addShape(l6);
        c2.complete();
        addObject(c2);

        Compound c3 = new Compound(0, 0);
        c3.addShape(b);
        c3.addShape(l7);
        c3.addShape(l8);
        c3.complete();
        addObject(c3);

        return true;
    }

    public boolean loadFile3() {

        clearCanvas();

        int cx = getWidth() / 2;
        int cy = getHeight() / 3;

        float x1 = cx - 300;
        float x2 = x1 + 200;
        float x3 = x1 + 600;
        float y1 = cy - 300 + 20;
        float y2 = y1 + 400 + 20;
        float y3 = y1 + 600 + 20;

        Point p1 = new Point(x1, y1);
        Point p2 = new Point(x2, y1);
        Point p3 = new Point(x3, y1);
        Point p4 = new Point(x2, y2);
        Point p5 = new Point(x3, y2);
        Point p6 = new Point(x1, y3);
        Point p7 = new Point(x3, y3);

        Compound c = new Compound(0, 0);
        c.addShape(new Line(p1, p2));
        c.addShape(new Line(p2, p3));
        c.addShape(new Line(p1, p6));
        c.addShape(new Line(p2, p4));
        c.addShape(new Line(p3, p5));
        c.addShape(new Line(p2, p5));
        c.addShape(new Line(p3, p4));
        c.addShape(new Line(p4, p5));
        c.addShape(new Line(p5, p7));
        c.addShape(new Line(p6, p7));
        c.complete();
        addObject(c);

        return true;
    }

    public boolean loadFile4() {

        clearCanvas();

        int cx = getWidth() / 2;
        int cy = getHeight() / 3;

        float x1 = cx - 300;
        float x2 = cx - 50;
        float x3 = cx + 50;
        float x4 = cx + 300;

        float y1 = cy - 400;
        float y2 = cy - 200;
        float y3 = cy;
        float y4 = cy + 200;
        float y5 = cy + 400;

        Point p1 = new Point(x1, y2);
        Point p2 = new Point(x2, y1);
        Point p3 = new Point(x4, y2);
        Point p4 = new Point(x3, y3);

        Point p5 = new Point(x1, y4);
        Point p6 = new Point(x2, y3);
        Point p7 = new Point(x4, y4);
        Point p8 = new Point(x3, y5);

        Compound c = new Compound(0, 0);
        // top square
        c.addShape(new Line(p1, p2));
        c.addShape(new Line(p2, p3));
        c.addShape(new Line(p3, p4));
        c.addShape(new Line(p4, p1));

        // connect top and bottom
        c.addShape(new Line(p1, p5));
        c.addShape(new Line(p2, p6));
        c.addShape(new Line(p3, p7));
        c.addShape(new Line(p4, p8));

        // bottom square
        c.addShape(new Line(p5, p6));
        c.addShape(new Line(p6, p7));
        c.addShape(new Line(p7, p8));
        c.addShape(new Line(p8, p5));
        c.complete();
        addObject(c);

        return true;
    }
}