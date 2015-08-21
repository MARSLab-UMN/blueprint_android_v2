package edu.umn.mars.blueprintandroidapp;

/**
 * Created by Owner on 8/18/2015.
 */
public class ImagePoint {

    private double x;
    private double y;
    private int Id;

    public ImagePoint(double x, double y, int id) {
        this.x = x;
        this.y = y;
        this.Id = id;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public int getId() {
        return this.Id;
    }


    // get the squared distance between this point and a set of coordinates
    public float getDistance(float x, float y) {
        // get difference between x and y coordinates
        float xdiff = x - (float)this.x;
        float ydiff = y - (float)this.y;

        // get squared distance between points
        float d = xdiff*xdiff + ydiff*ydiff;

        // return squared distance
        return d;
    }

}
