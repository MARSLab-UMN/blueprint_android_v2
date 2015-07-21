package edu.umn.mars.blueprintandroidapp;

/**
 * Created by mars on 7/21/15.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

public class DrawView extends View {
    Paint paint = new Paint();

    private void init() {
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(3f);
    }

    public DrawView(Context context) {
        super(context);
        init();
    }

    @Override
    public void onDraw(Canvas canvas) {
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(3f);
        canvas.drawLine(100, 200, 400, 700, paint);

        paint.setColor(Color.RED);
        canvas.drawLine(400, 600, 400, 700, paint);

        Log.i("DrawView", MainActivity.traj_vertices.size() + "");
    }

}