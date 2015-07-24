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
    Paint paint = new Paint();
    static final String DEBUG_TAG = "ZHeightDoubleSeekBar";
    private float mPreviousX, mPreviousY;

    private boolean isFirstTouch = true;

    double currentMax = 1e10;
    double currentMin = -1e10;
    float currentLower = 0;
    float currentUpper = 100000f; // will be reset on first touch
    final float thickness = 100;

    private void init() {
        paint.setColor(Color.parseColor("#555aaa"));
        paint.setStrokeWidth(3f);
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

                if (currentLower <= 0) {
                    currentLower = 0;
                }

                break;

//                float dx = x - mPreviousX;
//                float dy = y - mPreviousY;
//
//                // reverse direction of rotation above the mid-line
//                if (y > getHeight() / 2) {
//                    dx = dx * -1 ;
//                }
//
//                // reverse direction of rotation to left of the mid-line
//                if (x < getWidth() / 2) {
//                    dy = dy * -1 ;
//                }
        }

//        mPreviousX = x;
//        mPreviousY = y;

        Log.i(DEBUG_TAG, x + " " + y);
        invalidate();

        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        Log.e(DEBUG_TAG, "upper: " + (getHeight() - currentUpper) + " lower: " + (getHeight() - currentLower));
        canvas.drawRect(getWidth() - thickness, getHeight() - currentUpper, getWidth(), getHeight() - currentLower, paint);
    }
}
