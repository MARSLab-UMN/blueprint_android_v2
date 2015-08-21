package edu.umn.mars.blueprintandroidapp;

/**
 * Created by Owner on 8/21/2015.
 */
public class TrajectoryPoint {
    private float x;
    private float y;
    private TrajectoryPoint nextPoint = null;

    public TrajectoryPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public void setNextPoint(TrajectoryPoint point) {
        this.nextPoint = point;
    }

    public TrajectoryPoint getNextPoint() {
        return this.nextPoint;
    }

}
