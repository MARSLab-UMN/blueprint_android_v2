package edu.umn.mars.blueprintandroidapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by mars on 7/23/15.
 */
public class BlueprintAlignmentData {
    static final float InitialTrajPosX = 0;
    static final float InitialTrajPosY = 0;
    static final float InitialTrajRot = 0;
    static final float InitialTrajScale = 10.0f;

    public float TrajPosX;
    public float TrajPosY;
    public float TrajRot;
    public float TrajScale;
    public float MinZ = -1e6f;
    public float MaxZ = 1e6f;

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


    BlueprintAlignmentData() {
        ResetAlignmentData();
    }

    public void ResetAlignmentData() {
        TrajScale = InitialTrajScale;
        TrajPosX = InitialTrajPosX;
        TrajPosY = InitialTrajPosY;
        TrajRot = InitialTrajRot;
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
}
