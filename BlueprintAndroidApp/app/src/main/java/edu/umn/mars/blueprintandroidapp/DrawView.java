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

        double cosVal = Math.cos(MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajRot);
        double sinVal = Math.sin(MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajRot);

        for (int i = 0; i < poses.length; i += 2) {
            Double x = MainActivity.traj_vertices.get(3 * i / 2);
            Double y = MainActivity.traj_vertices.get(3 * i / 2 + 1);
            Double z = MainActivity.traj_vertices.get(3 * i / 2 + 2);

            x *= MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajScale;
            y *= MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajScale;

            double temp = cosVal * x - sinVal * y;
            y = sinVal * x + cosVal * y;
            x = temp;

            x += MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajPosX;
            y += MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajPosY;


            Long Lx = Math.round(x);
            Long Ly = Math.round(y);

            poses[i] = Integer.valueOf(Lx.intValue());
            poses[i + 1] = Integer.valueOf(Ly.intValue());
        }
        return poses;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (MainActivity.measurementTextView == null) {
            return;
        }

        if (MainActivity.traj_vertices.size() > 0) {
            Integer[] poses = PrepTrajPoses();
            for (int i = 0; i < poses.length - 2; i += 2) {
                canvas.drawLine(poses[i], poses[i + 1], poses[i + 2], poses[i + 3], paint);
            }
            String measStr = "";
            measStr += "Translate X: " + MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajPosX + ", ";
            measStr += "Translate Y: " + MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajPosY + ", ";
            measStr += "Rotation: " + MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajRot + ", ";
            measStr += "Scale: " + MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajScale + "";
            MainActivity.measurementTextView.setText(measStr);
        } else {
            MainActivity.measurementTextView.setText("Please load a trajectory.");
        }
    }

}