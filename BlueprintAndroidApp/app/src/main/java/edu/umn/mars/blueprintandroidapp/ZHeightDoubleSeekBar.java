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
    Paint textPaint = new Paint();
    static final String DEBUG_TAG = "ZHeightDoubleSeekBar";

    private boolean isFirstTouch = true;

    double currentMax = 1e10;
    double currentMin = -1e10;
    float currentLower = 0;
    float currentUpper = 100000f; // will be reset on first touch
    final float thickness = 100;

    private void init() {
        barPaint.setColor(Color.parseColor("#555aaa"));
        barPaint.setStrokeWidth(3f);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(20);
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
            isFirstTouch = false;
        }
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

                if (currentLower < 0) {
                    currentLower = 0;
                }

                if (currentUpper > getHeight()) {
                    currentUpper = getHeight();
                }


                break;
        }

        invalidate();

        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (isFirstTouch) {
            currentUpper = getHeight();
            isFirstTouch = false;
        }

        canvas.drawRect(getWidth() - thickness, getHeight() - currentUpper, getWidth(), getHeight() - currentLower, barPaint);


        canvas.drawText(String.format("%.3f", getValueAtPercentage(upperPercentOfHeight())), 25, 20, textPaint);
        canvas.drawText(String.format("%.3f", getValueAtPercentage(lowerPercentOfHeight())), 25, getHeight() - 20, textPaint);
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
}
