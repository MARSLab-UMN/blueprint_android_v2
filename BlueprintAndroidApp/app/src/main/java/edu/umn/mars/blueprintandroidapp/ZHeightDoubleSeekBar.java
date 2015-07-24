package edu.umn.mars.blueprintandroidapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by mars on 7/23/15.
 */
public class ZHeightDoubleSeekBar extends View {
    Paint barPaint = new Paint();
    Paint outOfRangeBarPaint = new Paint();
    Paint textPaint = new Paint();
    Paint textStrokePaint = new Paint();
    static final String DEBUG_TAG = "ZHeightDoubleSeekBar";

    private boolean isFirstTouch = true;

    float currentMax = 1e7f;
    float currentMin = -1e7f;
    float currentLower = 0;
    float currentUpper = 100000f; // will be reset on first touch
    final float thickness = 100;

    private void init() {
        barPaint.setColor(Color.parseColor("#555aaa"));
        barPaint.setStrokeWidth(3f);
        outOfRangeBarPaint.setColor(Color.parseColor("#dddddd"));
        outOfRangeBarPaint.setStrokeWidth(3f);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(20);
        textStrokePaint.setColor(Color.BLACK);
        textStrokePaint.setTextSize(30);
        textStrokePaint.setStyle(Paint.Style.STROKE);
    }

    public ZHeightDoubleSeekBar(Context context) {
        super(context);
        init();
    }

    public ZHeightDoubleSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ZHeightDoubleSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (isFirstTouch) {
            currentUpper = getHeight();
            currentLower = 0;
            isFirstTouch = false;
        }

        CheckLocks();

        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();
        float increasingY = getHeight() - y;

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                double distance_up = Math.abs(increasingY - currentUpper);
                double distance_down = Math.abs(increasingY - currentLower);

                if (distance_up < distance_down) {
                    currentUpper = increasingY;
                } else {
                    currentLower = increasingY;
                }

                if (currentLower > currentUpper) {
                    float temp = currentLower;
                    currentLower = currentUpper;
                    currentUpper = temp;
                }

                break;
        }

        invalidate();
        MainActivity.drawView.invalidate();
        MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).MinZ = getLowerValue();
        MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).MaxZ = getUpperValue();


        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (isFirstTouch) {
            currentUpper = getHeight();
            currentLower = 0;
            isFirstTouch = false;
        }

        CheckLocks();

        float height = getHeight();

        canvas.drawRect(getWidth() - thickness, height - currentUpper, getWidth(), height - currentLower, barPaint);

        int numberBuckets = MainActivity.traj_vertices_buckets.size();
        float sizePerBucket = getHeight() *1.0f / numberBuckets;
        for (int i = 0; i < numberBuckets; i++) {
            float inCurrBucket = MainActivity.traj_vertices_buckets.get(i);
            if (i*sizePerBucket < currentLower || i*sizePerBucket > currentUpper) {
                canvas.drawRect((getWidth() - thickness)*(1f-inCurrBucket/MainActivity.MaxBucket), sizePerBucket*(numberBuckets-i-1), getWidth() - thickness, sizePerBucket*(numberBuckets-i), outOfRangeBarPaint);
            } else {
                canvas.drawRect((getWidth() - thickness)*(1f-inCurrBucket/MainActivity.MaxBucket), sizePerBucket*(numberBuckets-i-1), getWidth() - thickness, sizePerBucket*(numberBuckets-i), barPaint);
            }

        }


        int fromEdge = 75;
//        canvas.drawText(String.format("%.3f", getValueAtPercentage(upperPercentOfHeight())), getWidth() - fromEdge, 20, textStrokePaint);
//        canvas.drawText(String.format("%.3f", getValueAtPercentage(lowerPercentOfHeight())), getWidth() - fromEdge, getHeight() - 10, textStrokePaint);

        String minText = String.format("%.3f", getValueAtPercentage(lowerPercentOfHeight()));
        String maxText = String.format("%.3f", getValueAtPercentage(upperPercentOfHeight()));
        if (MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).LockMinZ) {
            minText = "-Inf";
        }

        if (MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).LockMaxZ) {
            maxText = "Inf";
        }

        canvas.drawText(minText, getWidth() - fromEdge, getHeight() - 10, textPaint);
        canvas.drawText(maxText, getWidth() - fromEdge, 20, textPaint);
    }

    private void CheckLocks() {
        if (currentLower < 0 && !MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).LockMinZ) {
            currentLower = 0;
        } else if (MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).LockMinZ) {
            currentLower = currentMin;
        }

        if (currentUpper > getHeight() && !MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).LockMaxZ) {
            currentUpper = getHeight();
        }else if (MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).LockMaxZ) {
            currentUpper = currentMax;
        }
    }

    private float getValueAtPercentage(float percent) {
        return percent * (MainActivity.MaxZ - MainActivity.MinZ) + MainActivity.MinZ;
    }

    private float lowerPercentOfHeight() {
        return currentLower / getHeight();
    }

    private float upperPercentOfHeight() {
        return currentUpper / getHeight();
    }

    public float getUpperValue() {
        CheckLocks();
        return getValueAtPercentage(upperPercentOfHeight());
    }

    public float getLowerValue() {
        CheckLocks();
        return getValueAtPercentage(lowerPercentOfHeight());
    }

    public void setUpperValue(float value) {
        currentUpper = (value - MainActivity.MinZ) * getHeight() / (MainActivity.MaxZ - MainActivity.MinZ);
        Log.i(DEBUG_TAG, "Current upper set to " + currentUpper);
    }

    public void setLowerValue(float value) {
        currentLower = (value - MainActivity.MinZ) * getHeight() / (MainActivity.MaxZ - MainActivity.MinZ);
        Log.i(DEBUG_TAG, "Current lower set to " + currentLower);

    }
}
