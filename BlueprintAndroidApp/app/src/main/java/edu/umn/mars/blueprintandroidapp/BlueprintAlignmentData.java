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
    static final float InitialTrajScale = 100.0f;

    public float TrajPosX;
    public float TrajPosY;
    public float TrajRot;
    public float TrajScale;

    public String blueprintFileLocation;
    public Bitmap imageBitmap;
    public int bitmapHeight;
    public int bitmapWidth;

    public int startBitmapFITCENTERX;
    public int startBitmapFITCENTERY;

    public float blueprintToImageViewPixelsX_FITCENTER;
    public float blueprintToImageViewPixelsY_FITCENTER;
    public float blueprintToImageViewPixelsX_FITXY;
    public float blueprintToImageViewPixelsY_FITXY;


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
            blueprintToImageViewPixelsX_FITCENTER = blueprintToImageViewPixelsX_FITXY;
            blueprintToImageViewPixelsY_FITCENTER = blueprintToImageViewPixelsY_FITXY * (iv.getWidth()*1f/bitmapWidth);
        } else {
            blueprintToImageViewPixelsY_FITCENTER = blueprintToImageViewPixelsY_FITXY;
            blueprintToImageViewPixelsX_FITCENTER = blueprintToImageViewPixelsX_FITXY * (iv.getHeight()*1f/bitmapHeight);
        }

        Log.i(MainActivity.DEBUG_TAG,blueprintToImageViewPixelsX_FITXY + " " + blueprintToImageViewPixelsY_FITXY + " " +blueprintToImageViewPixelsX_FITCENTER + " " + blueprintToImageViewPixelsY_FITCENTER);

        startBitmapFITCENTERX = Math.round(bitmapWidth / blueprintToImageViewPixelsX_FITCENTER / 2f - iv.getWidth() / 2f);
        startBitmapFITCENTERY = Math.round(iv.getHeight() / 2f - bitmapHeight / blueprintToImageViewPixelsY_FITCENTER / 2f);

        Log.i(MainActivity.DEBUG_TAG, "Starting " + startBitmapFITCENTERX + " " + startBitmapFITCENTERY);
    }


    private void readDimensions(){
//        int actualHeight, actualWidth;
//        int imageViewHeight = blueprintImageView.getHeight(), imageViewWidth = blueprintImageView.getWidth();
//        int bitmapHeight =bheight, bitmapWidth = bwidth;
//        if (imageViewHeight * bitmapWidth <= imageViewWidth * bitmapHeight) {
//            actualWidth = bitmapWidth * imageViewHeight / bitmapHeight;
//            actualHeight = imageViewHeight;
//        } else {
//            actualHeight = bitmapHeight * imageViewWidth / bitmapWidth;
//            actualWidth = imageViewWidth;
//
//        }
//
//        Log.i(DEBUG_TAG, "It works !");
//        Context context = getApplicationContext();
//        CharSequence text = "Bw = "+bwidth+"|Iw = "+imageViewWidth+"|Aw = "+actualWidth;
//        int duration = Toast.LENGTH_LONG;
//
//        Toast toast = Toast.makeText(context, text, duration);
//        toast.show();
//
//        Log.i(DEBUG_TAG,"Bw = "+bwidth+"|Iw = "+imageViewWidth+"|Aw = "+actualWidth+"BH = "+bheight+"|Ih = "+imageViewHeight+"|Aw = "+actualHeight);

    }
}
