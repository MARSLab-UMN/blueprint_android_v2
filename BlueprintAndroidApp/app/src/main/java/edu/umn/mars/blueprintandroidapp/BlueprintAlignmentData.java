package edu.umn.mars.blueprintandroidapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Deque;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mars on 7/23/15.
 */
public class BlueprintAlignmentData {
    public float TrajPosX = 0;
    public float TrajPosY = 0;
    public float TrajRot = 0;
    public float TrajScale = 20;

    public TrajectoryPoint trajPoint = null;
    public TrajectoryPoint curTrajPoint = null;

    public String blueprintFileLocation;
    public Bitmap imageBitmap;
    public int bitmapHeight;
    public int bitmapWidth;

    private int startBitmapX_FITCENTER;
    private int startBitmapY_FITCENTER;
    private final int startBitmapX_FITXY = 0; // for completeness
    private final int startBitmapY_FITXY = 0; // for completeness

    private float blueprintToImageViewPixels_FITCENTER;
    private float blueprintToImageViewPixelsX_FITXY;
    private float blueprintToImageViewPixelsY_FITXY;

    void ResetAlignmentData() {
        TrajPosX = 0;
        TrajPosY = 0;
        TrajRot = 0;
        TrajScale = 50;
    }


    void LoadBlueprintFile(String location, ImageView iv) {
        blueprintFileLocation = location;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false; // will decode/return bitmap if false

        imageBitmap = BitmapFactory.decodeFile(blueprintFileLocation, options);
        bitmapWidth = options.outWidth;
        bitmapHeight = options.outHeight;

        blueprintToImageViewPixelsX_FITXY = bitmapWidth * 1f / iv.getWidth();
        blueprintToImageViewPixelsY_FITXY = bitmapHeight * 1f / iv.getHeight();

        if (bitmapWidth*1f/bitmapHeight > iv.getWidth()*1f/iv.getHeight()) {
            blueprintToImageViewPixels_FITCENTER = blueprintToImageViewPixelsX_FITXY;
        } else {
            blueprintToImageViewPixels_FITCENTER = blueprintToImageViewPixelsY_FITXY;
        }

        Log.i(MainActivity.DEBUG_TAG, "Dimensions of image: " + bitmapWidth + " " + bitmapHeight);
        Log.i(MainActivity.DEBUG_TAG,blueprintToImageViewPixelsX_FITXY + " " + blueprintToImageViewPixelsY_FITXY + " " + blueprintToImageViewPixels_FITCENTER);

        startBitmapX_FITCENTER = Math.round(iv.getWidth() / 2f - (bitmapWidth / blueprintToImageViewPixels_FITCENTER) / 2f);
        startBitmapY_FITCENTER = Math.round(iv.getHeight() / 2f - (bitmapHeight / blueprintToImageViewPixels_FITCENTER) / 2f);

//        InitialTrajPosX = bitmapWidth/2f;
//        InitialTrajPosY = bitmapHeight/2f;
//        ResetAlignmentData();
    }


    private <T> T ReturnFirstOrSecondForType(T fitcenter, T fitxy) {
        if (MainActivity.blueprintImageView.getScaleType() == ImageView.ScaleType.FIT_CENTER) {
            return fitcenter;
        } else if (MainActivity.blueprintImageView.getScaleType() == ImageView.ScaleType.FIT_XY) {
            return fitxy;
        } else {
            Log.e(MainActivity.DEBUG_TAG, "This should never be reached. Incorrect ImageView type used.");
            return (T) null;
        }
    }


    public int getUpperCornerX() {
        return ReturnFirstOrSecondForType(startBitmapX_FITCENTER, startBitmapX_FITXY);
    }


    public int getUpperCornerY() {
        return ReturnFirstOrSecondForType(startBitmapY_FITCENTER, startBitmapY_FITXY);
    }


    public float getBlueprintToImageViewPixelsX() {
        return ReturnFirstOrSecondForType(blueprintToImageViewPixels_FITCENTER, blueprintToImageViewPixelsX_FITXY);
    }


    public float getBlueprintToImageViewPixelsY() {
        return ReturnFirstOrSecondForType(blueprintToImageViewPixels_FITCENTER, blueprintToImageViewPixelsY_FITXY);
    }


    public String createConfigFileString(int i, int renderable_idx) {
        String renderableIndxStr = "_"+renderable_idx;

        String blueprint_portion = "";
        blueprint_portion +="### floor " + (i+1)+System.getProperty("line.separator");
        blueprint_portion += "  px_per_meter_"+i+renderableIndxStr+" = " + Float.toString(TrajScale)+ System.getProperty("line.separator");
        blueprint_portion += "  theta_"+i+renderableIndxStr+" = " + Double.toString(TrajRot*180/Math.PI) +  System.getProperty("line.separator");
        blueprint_portion += "  origin_"+i+renderableIndxStr+" = [" + Integer.toString(Math.round(TrajPosX)) + ", " + Integer.toString(Math.round(TrajPosY)) + "]" + System.getProperty("line.separator");
        blueprint_portion += "  blueprint_file_"+i+renderableIndxStr+" = " + blueprintFileLocation + System.getProperty("line.separator");

        return blueprint_portion;
    }


    public void loadFromConfigProperties(Properties prop, int blueprint_index, int renderable_number) {
        String suffix = "_" + blueprint_index + "_" + renderable_number;

        blueprintFileLocation = prop.getProperty("blueprint_file" + suffix);
        LoadBlueprintFile(blueprintFileLocation, MainActivity.blueprintImageView); // this should be called first so that it doesn't overwrite the following data

        String doubleArrayPatternString = "\\[(.*),\\s*(.*)\\]";
        Pattern pattern = Pattern.compile(doubleArrayPatternString);


        Matcher matcher = pattern.matcher(prop.getProperty("origin" + suffix));
        if (matcher.find()) {
            TrajPosX = Float.parseFloat(matcher.group(1));
            TrajPosY = Float.parseFloat(matcher.group(2));
        }
        TrajRot = (float) (Float.parseFloat(prop.getProperty("theta" + suffix))*Math.PI/180f); // convert to radians
        Log.i(MainActivity.DEBUG_TAG, "ROT: " + TrajRot);
        TrajScale = Float.parseFloat(prop.getProperty("px_per_meter" + suffix));

    }
}
