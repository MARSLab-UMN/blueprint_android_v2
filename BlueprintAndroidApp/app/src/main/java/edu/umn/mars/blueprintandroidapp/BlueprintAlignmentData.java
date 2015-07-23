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

    BlueprintAlignmentData() {
        ResetAlignmentData();
    }

    public void ResetAlignmentData() {
        TrajScale = InitialTrajScale;
        TrajPosX = InitialTrajPosX;
        TrajPosY = InitialTrajPosY;
        TrajRot = InitialTrajRot;
    }

    void LoadBlueprintFile(String location) {
        blueprintFileLocation = location;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false; // will decode/return bitmap if false

        imageBitmap = BitmapFactory.decodeFile(blueprintFileLocation, options);
        bitmapWidth = options.outWidth;
        bitmapHeight = options.outHeight;

        Log.i(MainActivity.DEBUG_TAG,"Bw = "+bitmapWidth+"|Iw = "+"BH = "+bitmapHeight);
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
