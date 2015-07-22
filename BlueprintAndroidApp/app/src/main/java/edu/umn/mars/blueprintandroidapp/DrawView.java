package edu.umn.mars.blueprintandroidapp;

/**
 * Created by mars on 7/21/15.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.TextView;

public class DrawView extends View {
    Paint paint = new Paint();
    static final String DEBUG_TAG = "DrawView";

    private void init() {
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(3f);
    }

    public DrawView(Context context) {
        super(context);
        init();
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private Integer[] PrepTrajPoses() {
        Integer[] poses = new Integer[MainActivity.traj_vertices.size() / 3 * 2];

        for (int i = 0; i < poses.length; i += 2) {
            Double x = MainActivity.traj_vertices.get(3 * i / 2);
            Double y = MainActivity.traj_vertices.get(3 * i / 2 + 1);
            Double z = MainActivity.traj_vertices.get(3 * i / 2 + 2);

            x *= MainActivity.TrajScale;
            y *= MainActivity.TrajScale;

            double temp = Math.cos(MainActivity.TrajRot) * x - Math.sin(MainActivity.TrajRot) * y;
            y = Math.sin(MainActivity.TrajRot) * x + Math.cos(MainActivity.TrajRot) * y;
            x = temp;

            x += MainActivity.TrajPosX;
            y += MainActivity.TrajPosY;


            Long Lx = Math.round(x);
            Long Ly = Math.round(y);

            poses[i] = Integer.valueOf(Lx.intValue());
            poses[i + 1] = Integer.valueOf(Ly.intValue());
        }
        return poses;
    }

    @Override
    public void onDraw(Canvas canvas) {
        Integer[] poses = PrepTrajPoses();
        for (int i = 0; i < poses.length - 2; i += 2) {
            canvas.drawLine(poses[i], poses[i + 1], poses[i + 2], poses[i + 3], paint);
        }

        if (poses.length > 0) {
            String measStr = "";
            measStr += "Translate X: " + MainActivity.TrajPosX + ", ";
            measStr += "Translate Y: " + MainActivity.TrajPosY + ", ";
            measStr += "Rotation: " + MainActivity.TrajRot + ", ";
            measStr += "Scale: " + MainActivity.TrajScale + "";
            MainActivity.measurementTextView.setText(measStr);
        } else {
            MainActivity.measurementTextView.setText("Please load a trajectory.");
        }
    }

}