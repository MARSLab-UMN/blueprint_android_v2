package edu.umn.mars.blueprintandroidapp;

/**
 * Created by mars on 7/21/15.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
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

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private Integer[] PrepTrajPoses() {
        Integer[] poses = new Integer[MainActivity.traj_vertices.size()/3*2];

        for (int i = 0; i < poses.length; i+=2) {
            Double x = MainActivity.traj_vertices.get(3*i/2)*MainActivity.TrajScale;
            Double y = MainActivity.traj_vertices.get(3*i/2+1)*MainActivity.TrajScale;
            Double z = MainActivity.traj_vertices.get(3*i/2+2);


            Long L = Math.round(x);
            poses[i] = Integer.valueOf(L.intValue());
            L = Math.round(y);
            poses[i+1] = Integer.valueOf(L.intValue());
        }
        return poses;
    }

    @Override
    public void onDraw(Canvas canvas) {
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(3f);
        canvas.drawLine(100, 200, 400, 700, paint);

        paint.setColor(Color.RED);
        canvas.drawLine(400, 600, 400, 700, paint);

        Log.i("DrawView", MainActivity.traj_vertices.size() + "");

        Integer[] poses = PrepTrajPoses();
        paint.setColor(Color.GREEN);
        for (int i = 0; i < poses.length - 2; i += 2) {
            canvas.drawLine(poses[i], poses[i + 1],poses[i + 2], poses[i + 3], paint);
        }
    }

}