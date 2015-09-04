package edu.umn.mars.blueprintandroidapp;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Owner on 8/28/2015.
 */
public class DijkstraThread extends Thread {
    private static List<BlueprintAlignmentData> blueprints;
    private List<Integer> waypoints = new LinkedList<>();
    private String paramDataPath;
    private int paramNumImgs;
    private int paramImgSubsamp;
    private int paramStartStairs;
    private int paramStartRot;

    //jni stuff
    public static native String stringFromJNI();

    static {
        System.loadLibrary("DijkstraMain");
    }

    //constructor
    public DijkstraThread(String paramDataPath,
                          int paramNumImgs,
                          int paramImgSubsamp,
                          int paramStartStairs,
                          int paramStartRot,
                          List<BlueprintAlignmentData> blueprints) {

        Log.i("JNI", stringFromJNI());
        this.paramDataPath = paramDataPath;
        this.paramNumImgs = paramNumImgs;
        this.paramImgSubsamp = paramImgSubsamp;
        this.paramStartStairs = paramStartStairs;
        this.paramStartRot = paramStartRot;
        this.blueprints = blueprints;
    }


    @Override
    public void run() {
        Log.i("Timing", "Started mapping");
        mapTrajectory();
        Log.i("Timing", "Ended mapping");

        for (Integer cur : waypoints) {
            Log.i("Path", "Point: " + cur.toString());
        }
    }


    //map drawn path points to image waypoints
    private void mapTrajectory() {
        for (BlueprintAlignmentData curBlueprint : blueprints) {
            if (curBlueprint.imagePoints.size() == 0) {
                continue;
            } else if (curBlueprint.trajPoint == null) {
                continue;
            }

            float shiftX = curBlueprint.TrajPosX;
            shiftX += curBlueprint.getUpperCornerX();
            float shiftY = curBlueprint.TrajPosY;
            shiftY += curBlueprint.getUpperCornerY();

            float trajRot = curBlueprint.TrajRot;
            double cosVal = Math.cos(-trajRot);
            double sinVal = Math.sin(-trajRot);

            float trajScaleX = curBlueprint.getBlueprintToImageViewPixelsX();
            trajScaleX /= curBlueprint.TrajScale;
            float trajScaleY = curBlueprint.getBlueprintToImageViewPixelsY();
            trajScaleY /= curBlueprint.TrajScale;

            int curWaypoint;
            int prevWaypoint = 0;
            TrajectoryPoint curPoint = curBlueprint.trajPoint;
            List<ImagePoint> images = curBlueprint.imagePoints;
            int size = images.size();

            float minDistance;

            while (curPoint != null) {
                float curX = curPoint.getX();
                float curY = curPoint.getY();
                curX -= shiftX;
                curY -= shiftY;
                curX *= trajScaleX;
                curY *= trajScaleY;
                double tempX = cosVal*curX - sinVal*curY;
                curY = (float) (sinVal*curX + cosVal*curY);
                curX = (float) tempX;

                minDistance = images.get(0).getDistance(curX, curY);
                curWaypoint = images.get(0).getId();

                for (int i=0; i<size; i++) {
                    ImagePoint curImage = images.get(i);
                    float curDist = curImage.getDistance(curX, curY);
                    if (curDist < minDistance) {
                        curWaypoint = curImage.getId();
                        minDistance = curDist;
                    }
                }

                if (curWaypoint != prevWaypoint) {
                    waypoints.add(curWaypoint);
                    prevWaypoint = curWaypoint;
                }

                curPoint = curPoint.getNextPoint();
            }
        }
    }

}
