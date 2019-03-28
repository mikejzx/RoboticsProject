package io.mikejzx.github.roboticsproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NodeView extends View {

    private Paint paint = new Paint();
    private boolean initialised = false;
    public static NodeView instance;

    public static byte nodeScale = 26; // Around 10 m by default
    
    private Handler handler = new Handler();
    private Vector2 oldTouch = new Vector2(),
            touchDelta = new Vector2();

    private static final int colourBg =  Color.parseColor("#EEEEEE");
    private static final int colourText =  Color.parseColor("#111111");
    private static final int colourText2 =  Color.parseColor("#333333");
    private static final int colourNodeLine = Color.parseColor("#002288");
    private static final int colourNode =  Color.parseColor("#EEEECC");
    private static final int colourNodeSelected =  Color.parseColor("#AAFFFF");
    
    private static final int colourScaleBg =  Color.parseColor("#888888");
    private static final int colourScaleFg =  Color.parseColor("#AAAAAA");

    private static final float touchDistanceThreshold = 100.0f;
    

    public NodeView (Context context) {
        super(context);
        initialise();
    }

    public NodeView (Context context, AttributeSet a) {
        super(context, a);
        initialise();
    }

    public NodeView (Context context, AttributeSet a, int defStyle) {
        super(context, a, defStyle);
        initialise();
    }

    private void initialise () {

        paint.setAntiAlias(true);
        System.out.println("NODEVIEW CTOR");

        if (instance == null) {
            instance = this;
        }
    }



    @Override
    public void onDraw (Canvas canvas) {
        super.onDraw(canvas);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(colourBg);
        canvas.drawPaint(paint);

        List<Node> nodes = MainActivity.nodes;
        int nodeCount = nodes.size();
        Node selectedNode = null;

        // Draw text
        paint.setColor(colourText);
        paint.setTextSize(30.0f);
        String strAdd = nodeCount > 99 ? " (Maximum)" : "";
        if (nodeCount == 2) { strAdd = " (Minimum)"; }
        canvas.drawText("Node count: " + nodeCount + strAdd, 10, 30, paint);

        for (int i = 0; i < nodeCount; i++) {
            Node node = nodes.get(i);
            short x = node.position.getXClamped((short)getWidth()),
                    y = node.position.getYClamped((short)getHeight());

            // Skip selected node.
            if (node.selected) {
                selectedNode = node;

                // Draw selected node's line at regular time.
                if (i < nodeCount - 1) {
                    DrawNodeLine(node, nodes.get(i + 1), x, y, canvas);
                }
                continue;
            }

            // Draw line to next node
            if (i < nodeCount - 1) {
                DrawNodeLine(node, nodes.get(i + 1), x, y, canvas);
            }

            // Draw the actual node itself
            DrawNode(node, x, y, canvas);
        }

        // Selected node is drawn last so it is overlapping all :
        if (selectedNode != null) {
            DrawSelectedNode(selectedNode, canvas);
        }
        
        int w = getWidth(), h = getHeight();
        
        // Draw scale button (+)
        paint.setColor(colourScaleBg);
        canvas.drawRect(100, 100, 100, 100, paint);
        paint.setColor(colourScaleFg);
        
        // Draw side text representing scale.
        paint.setColor(colourText2);
        paint.setTextSize(30.0f);
        canvas.rotate(-90.0f);
        canvas.translate(-getWidth(), (getHeight() / 2) - getWidth() + 60);
        int realLength = Math.round(((float)nodeScale / 255.0f) * 100.0f);
        canvas.drawText(String.format("~%d m", realLength), getWidth() / 2, getHeight() / 2, paint);
        //canvas.rotate(90.0f);
    }

    private void DrawSelectedNode (Node node, Canvas canvas) {
        float x = node.position.getXClamped((short)getWidth()),
                y = node.position.getYClamped((short)getHeight());

        // Selected only: Outline A
        if (node.selected) {
            paint.setColor(Color.parseColor("#00FFFF"));
            canvas.drawCircle(x, y, 32, paint);
        }
        //DrawNode(node, x, y, canvas);

        // Node outline
        paint.setColor(Color.parseColor("#000000"));
        canvas.drawCircle(x, y, 25, paint);

        // Node colour foreground
        paint.setColor(colourNodeSelected);
        canvas.drawCircle(x, y, 20, paint);
    }

    private void DrawNode (Node node, float x, float y, Canvas canvas) {
        // Node outline
        paint.setColor(Color.parseColor("#000000"));
        canvas.drawCircle(x, y, 25, paint);

        // Node colour foreground
        paint.setColor(colourNode);
        canvas.drawCircle(x, y, 20, paint);
    }

    private void DrawNodeLine (Node cur, Node next, float x, float y, Canvas canvas) {
        float xNext = next.position.getXClamped((short)getWidth());
        float yNext = next.position.getYClamped((short)getHeight());
        paint.setColor(colourNodeLine);
        canvas.drawLine(x, y, xNext, yNext, paint);
    }

    public static void setSelectedNode (Node node, int idx) {
        List<Node> nodes = MainActivity.nodes;
        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).selected = false;
        }
        node.selected = true;
        MainActivity.selectedNode = node;
        MainActivity.selectedNodeIndex = idx;

        instance.invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final Vector2 touchPos = new Vector2((short)event.getX(), (short)event.getY());
        touchDelta = touchPos.sub(oldTouch);
        oldTouch = touchPos;

        switch (event.getAction()) {
            case (MotionEvent.ACTION_DOWN): {
                // Touch down checks for a nearby node and selects it.
            	// (ARRAYLIST *MUST* BE INSTANTIATED SO THE ORIGINAL REF DOESNT GET SORTED.)
                List<Node> nodes = new ArrayList<Node>(MainActivity.nodes);
                // Sort by distance, so shortest distance node is selected first.
                // Instead of iterating through the node list (was old method...)
                Collections.sort(nodes, new Comparator<Node>() {
					@Override
					public int compare(Node lhs, Node rhs) {
						lhs.setComparer(lhs.position.distance(touchPos));
						rhs.setComparer(rhs.position.distance(touchPos));
						return lhs.getComparer().compareTo(rhs.getComparer());
					}
                });
                Node n = nodes.get(0);
                if (n.getComparer() < touchDistanceThreshold) {
                    setSelectedNode(n, 0);
                }
            } break;

            case (MotionEvent.ACTION_MOVE): {
                // Touch move pans the selected node. (Removed for usability reasons. Was multiplied with touchDelta)
                float dist = MainActivity.selectedNode.position.distance(touchPos);
                float multiplier = 0.5f + (1.0f - Utils.clamp(dist / 100.0f, 0.0f, 1.0f)) * 0.5f;
                multiplier = Utils.lerp(multiplier, 1.0f, 1.0f - Utils.clamp01(dist / 90.0f));
                Node n = MainActivity.selectedNode;
                n.position.addTo(touchDelta.mul(multiplier));
                n.position.x = (short)n.position.getXClamped((short)getWidth());
                n.position.y = (short)n.position.getYClamped((short)getHeight());
                //System.out.println(touchDelta.getY() + ", " + touchDelta.getY());

                // Refresh control
                invalidate();

            } break;
        }

        // Used for restoring touch delta.
        // https://stackoverflow.com/questions/38134818/android-simplest-way-to-calculate-delta-of-touch-position
        handler.removeCallbacksAndMessages(null);
        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    touchDelta = Vector2.zero;
                }
            }, 500);
        }

        return true;
        //return super.onTouchEvent(event);
    }
}
