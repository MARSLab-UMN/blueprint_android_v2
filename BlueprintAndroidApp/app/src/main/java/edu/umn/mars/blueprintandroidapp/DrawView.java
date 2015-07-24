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
import android.widget.CalendarView;
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

    private Integer[] PrepTrajPoses(Canvas canvas) {
        float lowerZ = MainActivity.maxHeightSeekBar.getLowerValue();
        float upperZ = MainActivity.maxHeightSeekBar.getUpperValue(); // this should be changed to use the blueprint value
        int numberOfCorrectHeightPoints = 0;
        int numVertices = MainActivity.traj_vertices.size();
        for (int i = 0; i < numVertices; i+=3) {
            Double z = MainActivity.traj_vertices.get(i + 2);
            if (z >= lowerZ && z <= upperZ) {
                numberOfCorrectHeightPoints++;
            }
        }

        Integer[] poses = new Integer[numberOfCorrectHeightPoints * 2];

        float trajRot = MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).BaseRotation;
        trajRot += MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajRot;
        double cosVal = Math.cos(trajRot);
        double sinVal = Math.sin(trajRot);
        int upperCornerX = MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).getUpperCornerX();
        int upperCornerY = MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).getUpperCornerY();

        float trajScale = MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajScale;
        float blueprintToIVPixelScaleX = MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).getBlueprintToImageViewPixelsX();
        float blueprintToIVPixelScaleY = MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).getBlueprintToImageViewPixelsY();
        float posX = MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajPosX;
        float posY = MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajPosY;

        boolean breakPoint = true; // so we always miss the fist iteration
        for (int i = 0, renderIdx = 0; i < poses.length; renderIdx += 3) {
            // if out of range, repeat the previous or next location so we don't render missing floors

            Double x = MainActivity.traj_vertices.get(renderIdx);
            Double y = -MainActivity.traj_vertices.get(renderIdx + 1); // negate to rotate around x axis
            Double z = MainActivity.traj_vertices.get(renderIdx + 2);
            if (!(z >= lowerZ && z <= upperZ)) {
                breakPoint = true;
                continue;
            }

            double temp = cosVal * x - sinVal * y;
            y = sinVal * x + cosVal * y;
            x = temp;
            
            x *= trajScale;
            y *= trajScale;

            x += posX;
            y += posY;

            x /= blueprintToIVPixelScaleX;
            y /= blueprintToIVPixelScaleY;

            x += upperCornerX;
            y += upperCornerY;


            Long Lx = Math.round(x);
            Long Ly = Math.round(y);


            poses[i] = Integer.valueOf(Lx.intValue());
            poses[i + 1] = Integer.valueOf(Ly.intValue());
            if (!breakPoint) {
                canvas.drawLine(poses[i - 2], poses[i - 1], poses[i], poses[i + 1], paint);
            } else {
                breakPoint = false;
            }
            i += 2;
        }
        return poses;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (MainActivity.measurementTextView == null) {
            return;
        }

        if (MainActivity.traj_vertices.size() > 0) {
            Integer[] poses = PrepTrajPoses(canvas);
            String measStr = "";
            measStr += "Translate X: " + MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajPosX + ", ";
            measStr += "Translate Y: " + MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajPosY + ", ";
            measStr += "Rotation: " + MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajRot*180/Math.PI + ", ";
            measStr += "Scale: " + MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajScale + "";
            MainActivity.measurementTextView.setText(measStr);
        } else {
            MainActivity.measurementTextView.setText("Please load a trajectory.");
        }
    }

}