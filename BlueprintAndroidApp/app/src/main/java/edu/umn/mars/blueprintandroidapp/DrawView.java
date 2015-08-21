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
    Paint activePaint = new Paint();
    Paint passivePaint = new Paint();
    static final String DEBUG_TAG = "DrawView";

    private void init() {
        passivePaint.setColor(Color.BLUE);
        passivePaint.setStrokeWidth(10f);
        activePaint.setColor(Color.GREEN);
        activePaint.setStrokeWidth(10f);
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


    private void drawImages(Canvas canvas, Paint paint) {

        float trajRot = MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajRot;
        float shiftX = MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajPosX;
        float shiftY = MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajPosY;
        double cosVal = Math.cos(trajRot);
        double sinVal = Math.sin(trajRot);
        int upperCornerX = MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).getUpperCornerX();
        int upperCornerY = MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).getUpperCornerY();

        float trajScale = MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajScale;
        float blueprintToIVPixelScaleX = MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).getBlueprintToImageViewPixelsX();
        float blueprintToIVPixelScaleY = MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).getBlueprintToImageViewPixelsY();

        for (ImagePoint curPoint : MainActivity.imagePoints) {
            Double curX = curPoint.getX();
            Double curY = curPoint.getY();

            double tempX = cosVal*curX - sinVal*curY;
            curY = sinVal*curX + cosVal*curY;
            curX = tempX;

            curX *= trajScale;
            curY *= trajScale;
            curX /= blueprintToIVPixelScaleX;
            curY /= blueprintToIVPixelScaleY;

            curX += upperCornerX;
            curY += upperCornerY;
            curX += shiftX;
            curY += shiftY;

            int drawX = Math.round(curX.floatValue());
            int drawY = Math.round(curY.floatValue());
            canvas.drawCircle(drawX, drawY, 10, paint);
        }
    }

    private void drawPath(Canvas canvas, Paint paint) {
        TrajectoryPoint curPoint = MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).trajPoint;
        TrajectoryPoint nextPoint;

        if (curPoint == null) {
            return;
        }

        while (curPoint.getNextPoint() != null) {
            nextPoint = curPoint.getNextPoint();
            canvas.drawLine(curPoint.getX(), curPoint.getY(), nextPoint.getX(), nextPoint.getY(),  paint);
            curPoint = nextPoint;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (MainActivity.measurementTextView == null) {
            return;
        }

        if (MainActivity.imagePoints.size() > 0) {
            //Integer[] poses = PrepTrajPoses(canvas);
            if (MainActivity.drawPath) {
                drawImages(canvas, passivePaint);
                drawPath(canvas, activePaint);
            } else {
                drawImages(canvas, activePaint);
                drawPath(canvas, passivePaint);
            }

            String measStr = "";
            measStr += "Translate X: " + MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajPosX + ", ";
            measStr += "Translate Y: " + MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajPosY + ", ";
            measStr += "Rotation: " + (int)Math.round(MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajRot*180/Math.PI) + ", ";
            measStr += "Scale: " + MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajScale + "";
            MainActivity.measurementTextView.setText(measStr);
        } else {
            MainActivity.measurementTextView.setText("Please load a trajectory.");
        }
    }
}
