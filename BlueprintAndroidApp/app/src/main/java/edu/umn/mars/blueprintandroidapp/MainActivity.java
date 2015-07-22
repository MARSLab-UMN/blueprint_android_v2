package edu.umn.mars.blueprintandroidapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Color;
import android.nfc.Tag;

import android.os.Environment;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends ActionBarActivity {
    //In an Activity
    static public List<Double> traj_vertices = new ArrayList<Double>();

    static final private String DEBUG_TAG = "BlueprintAndroidApp";
    private static Context context;
    private ArrayList<String> mFileList = new ArrayList<String>();
    static final int state_vec_size = 16;
    private String mChosenFile;
    private String mCurrentDir;
    private LoadType mLoadType;
    private static final String FTYPE = ".txt";
    private static final String PNGTYPE = ".png";
    private static final String JPGTYPE = ".jpg";
    private static final String JPEGTYPE = ".jpeg";

    // Views
    DrawView drawView;
    ImageView blueprintImageView;

    // Alignment parameters and variables
    static final float InitialTrajScale = 100.0f;
    static final float InitialTrajPosX = 0;
    static final float InitialTrajPosY = 0;
    static final float InitialTrajRot = 0;
    static float TrajScale = InitialTrajScale;
    static float TrajPosX = InitialTrajPosX;
    static float TrajPosY = InitialTrajPosY;
    static float TrajRot = InitialTrajRot;
    private int mActivePointerId = MotionEvent.INVALID_POINTER_ID;
    ScaleGestureDetector scaleDetector;
    private float mLastRot, mLastTouchX, mLastTouchY;


    public enum LoadType {
        BLUEPRINT, TRAJECTORY, LOAD_ALIGNMENT, SAVE_ALIGNMENT
    }

    public static Context getAppContext() {
        return MainActivity.context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);
        drawView = (DrawView) findViewById(R.id.draw_view);
        blueprintImageView = (ImageView) findViewById(R.id.imageview);
        scaleDetector = new ScaleGestureDetector(getAppContext(), new ScaleListener());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                PrintNotYetImplemented("SelectSettings");
                return true;
            case R.id.action_load_alignment:
                LoadAlignment();
                return true;
            case R.id.action_save_alignment:
                SaveAlignment();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            MainActivity.TrajScale *= detector.getScaleFactor();
            MainActivity.TrajScale = Math.max(0.1f, MainActivity.TrajScale);

            drawView.invalidate();
            drawView.requestLayout();

            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;

        int num_pointers = event.getPointerCount();

        switch (num_pointers) {
            case 1:
                result = handleOnePointer(event);
                break;
            case 2:
                result = handleTwoPointers(event);
                break;
            default:
                result = super.onTouchEvent(event);
        }

        Log.i(DEBUG_TAG, "Change in X: " + TrajPosX + " Y: " + TrajPosY + " Rot: " + TrajRot + " Scale: " + TrajScale);

        return result;
    }

    private boolean handleTwoPointers(MotionEvent event) {
        boolean result = false;

        result |= scaleDetector.onTouchEvent(event);

        final int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                // really, should not hit this, because there are two pointers
                result = true;
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN: {
//                final int pointerIndex = MotionEventCompat.getActionIndex(event);
                final float x0 = MotionEventCompat.getX(event, 0);
                final float y0 = MotionEventCompat.getY(event, 0);
                final float x1 = MotionEventCompat.getX(event, 1);
                final float y1 = MotionEventCompat.getY(event, 1);
                final float x = (x1 + x0) / 2;
                final float y = (y1 + y0) / 2;

                // Remember where we started (for dragging)
                mLastTouchX = x;
                mLastTouchY = y;
                // Save the ID of this pointer (for dragging)
                mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                result = true;
                break;
            }


            case MotionEvent.ACTION_MOVE: {
                // Find the index of the active pointer and fetch its position
//                final int pointerIndex =
//                        MotionEventCompat.findPointerIndex(event, mActivePointerId);

                final float x0 = MotionEventCompat.getX(event, 0);
                final float y0 = MotionEventCompat.getY(event, 0);
                final float x1 = MotionEventCompat.getX(event, 1);
                final float y1 = MotionEventCompat.getY(event, 1);
                final float x = (x1 + x0) / 2;
                final float y = (y1 + y0) / 2;

                // Calculate the distance moved
                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;

                TrajPosX += dx;
                TrajPosY += dy;

                drawView.invalidate();

                // Remember this touch position for the next move event
                mLastTouchX = x;
                mLastTouchY = y;
                result = true;

                break;
            }

            case MotionEvent.ACTION_UP: {
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                result = true;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                result = true;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {

                final int pointerIndex = MotionEventCompat.getActionIndex(event);
                final int pointerId = MotionEventCompat.getPointerId(event, pointerIndex);

                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                mLastRot = MotionEventCompat.getX(event, newPointerIndex);
                mActivePointerId = MotionEventCompat.getPointerId(event, newPointerIndex);

                result = true;
                break;
            }
        }
        return result;
    }

    private boolean handleOnePointer(MotionEvent event) {
        boolean result = false;

        final int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int pointerIndex = MotionEventCompat.getActionIndex(event);
                final float x = MotionEventCompat.getX(event, pointerIndex);

                // Remember where we started (for dragging)
                mLastRot = x;
                // Save the ID of this pointer (for dragging)
                mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                result = true;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // Find the index of the active pointer and fetch its position
                final int pointerIndex =
                        MotionEventCompat.findPointerIndex(event, mActivePointerId);
                if (pointerIndex == MotionEvent.INVALID_POINTER_ID) {
                    return result = true;
                }

                final float x = MotionEventCompat.getX(event, pointerIndex);

                // Calculate the distance moved
                final float dx = x - mLastRot;

                TrajRot += (dx / 2f) * (Math.PI / 180f);

                if (TrajRot >= 2 * Math.PI) {
                    TrajRot -= 2 * Math.PI;
                }

                if (TrajRot < 0) {
                    TrajRot += 2 * Math.PI;
                }

                drawView.invalidate();

                // Remember this touch position for the next move event
                mLastRot = x;
                result = true;

                break;
            }

            case MotionEvent.ACTION_UP: {
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                result = true;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                result = true;
                break;
            }
        }
        return result;
    }

    private void PrintNotYetImplemented(CharSequence functionName) {
        Context context = getApplicationContext();
        CharSequence text = functionName + " not yet implemented";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void readTrajData() {
        traj_vertices.clear();
        // write on SD card file data in the text box
        try {
            File myFile = new File(mCurrentDir + mChosenFile);
            Scanner scan = new Scanner(myFile);

            int count = 0;
            while (scan.hasNextDouble()) {
                if (count % state_vec_size == 13 || count % state_vec_size == 14 || count % state_vec_size == 15) {
                    traj_vertices.add(scan.nextDouble());
                } else {
                    scan.nextDouble();
                }
                count++;
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void displayTrajData() {
        if (traj_vertices.isEmpty()) {
            Log.e(DEBUG_TAG, "No vertices loaded");
            return;
        }

        ResetAlignmentData();
    }

    private void readImageData() {
        // PrintNotYetImplemented("readImageData");
        //String imageInSD = Environment.getExternalStorageDirectory().getAbsolutePath() +"/house_map/"  + ".JPG";
        String imageInSD = "/storage/emulated/0/Download/house_map.JPG";
        Bitmap bitmap = BitmapFactory.decodeFile(imageInSD);
        try {
            ImageView myImageView = (ImageView) findViewById(R.id.imageview);
            myImageView.setImageBitmap(bitmap);
        } catch (Exception e) {

            Toast.makeText(getBaseContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }


    }

    private void readAlignmentData() {
        PrintNotYetImplemented("readAlignmentData");
    }

    private void loadFileList(String baseFolderPath) {
        File mPath = new File(baseFolderPath);

        try {
            mPath.mkdirs();
        } catch (SecurityException e) {
            Log.e(DEBUG_TAG, "unable to write on the sd card " + e.toString());
        }
        if (mPath.exists()) {
            FilenameFilter dirFilter = new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return sel.isDirectory();
                }

            };
            FilenameFilter fileFilter = new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return sel.isFile();
                }

            };
            List<String> dirList = Arrays.asList(mPath.list(dirFilter));
            mFileList.addAll(dirList);
            if (mLoadType != LoadType.SAVE_ALIGNMENT) {
                List<String> fileList = Arrays.asList(mPath.list(fileFilter));
                mFileList.addAll(fileList);
            }
        } else {
            mFileList.clear();
        }
        mFileList.add("MOVE UP ONE DIRECTORY LEVEL");
    }

    private boolean FileIsImage() {
        return mChosenFile.toLowerCase().contains(PNGTYPE) || mChosenFile.toLowerCase().contains(JPGTYPE) || mChosenFile.toLowerCase().contains(JPEGTYPE);
    }

    private boolean FileIsDir() {
        File sel = new File(mCurrentDir, mChosenFile);
        return sel.isDirectory();
    }

    private boolean FileIsData() {
        return !FileIsImage() && !FileIsDir();
    }

    protected Dialog createFileSelectorDialog(String baseFolderPath, LoadType loadType) {
        mCurrentDir = baseFolderPath;
        mLoadType = loadType;
        mFileList.clear();
        loadFileList(mCurrentDir); // populates mFileList
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Choose your file");
        if (mFileList.isEmpty()) {
            Log.e(DEBUG_TAG, "Showing file picker before loading the file list");
            dialog = builder.create();
            return dialog;
        }
        builder.setItems(mFileList.toArray(new String[mFileList.size()]),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == mFileList.size() - 1 && true) {
                            File file = new File(mCurrentDir + mChosenFile);
                            String parent_folder = file.getParentFile().getName() + "/";
                            String up_a_level = mCurrentDir.substring(0, mCurrentDir.length() - parent_folder.length());
                            String top_level = Environment.getExternalStorageDirectory().toString();
                            if (!top_level.endsWith("/")) {
                                top_level += "/";
                            }
                            if (mCurrentDir.equals(top_level)) {
                                Log.e(DEBUG_TAG, "Already at top directory");
                                Context context = getApplicationContext();
                                CharSequence text = "Invalid file selection. Already at top level directory.";
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                                createFileSelectorDialog(mCurrentDir, mLoadType);
                            } else {
                                createFileSelectorDialog(up_a_level, mLoadType);
                            }
                        } else {
                            mChosenFile = mFileList.get(which);
                            Log.i(DEBUG_TAG, "Selected: " + mChosenFile);

                            if (FileIsImage() && mLoadType == LoadType.BLUEPRINT) {
                                Log.i(DEBUG_TAG, "You have selected an image.");
                                readImageData();
                            } else if (FileIsData() && mLoadType == LoadType.TRAJECTORY) {
                                Log.i(DEBUG_TAG, "You have selected a trajectory data file.");
                                readTrajData();
                                displayTrajData();
                            } else if (FileIsData() && mLoadType == LoadType.LOAD_ALIGNMENT) {
                                readAlignmentData();
                            } else if (mLoadType == LoadType.SAVE_ALIGNMENT) {
                                PrintNotYetImplemented("SaveAlignment");
                            } else if (FileIsDir()) {
                                Log.i(DEBUG_TAG, "Selected a directory");
                                dialog.dismiss();
                                createFileSelectorDialog(mCurrentDir + mChosenFile + "/", mLoadType);
                            } else {
                                Context context = getApplicationContext();
                                CharSequence text = "Invalid file selection. Select another.";
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                                createFileSelectorDialog(mCurrentDir, mLoadType);
                            }
                        }
                    }
                });

        dialog = builder.show();
        switch (mLoadType) {
            case BLUEPRINT:
                dialog.setTitle("Select a blueprint file");
                break;
            case TRAJECTORY:
                dialog.setTitle("Select a trajectory data file");
                break;
            case LOAD_ALIGNMENT:
                dialog.setTitle("Select an alignment data file");
                break;
            case SAVE_ALIGNMENT:
                dialog.setTitle("Select location to save alignment file");
                break;
            default:
                dialog.setTitle("Command not implemented");
                break;
        }
        return dialog;
    }

    public void LoadAlignment() {
        Dialog dialog = createFileSelectorDialog(Environment.getExternalStorageDirectory() + "/", LoadType.LOAD_ALIGNMENT);
        dialog.show();
    }

    public void SaveAlignment() {
        Dialog dialog = createFileSelectorDialog(Environment.getExternalStorageDirectory() + "/", LoadType.SAVE_ALIGNMENT);
        dialog.show();
    }

    public void SelectTrajectory(View view) {
        Dialog dialog = createFileSelectorDialog(Environment.getExternalStorageDirectory() + "/", LoadType.TRAJECTORY);
        dialog.show();
    }

    public void SelectBlueprint(View view) {
        Dialog dialog = createFileSelectorDialog(Environment.getExternalStorageDirectory() + "/", LoadType.BLUEPRINT);
        dialog.show();
    }

    public void ResetAlignmentData() {
        TrajScale = InitialTrajScale;
        TrajPosX = InitialTrajPosX;
        TrajPosY = InitialTrajPosY;
        TrajRot = InitialTrajRot;

        drawView.invalidate();
        drawView.requestLayout();
    }

    public void ResetAlignment(View view) {
        ResetAlignmentData();

        Context context = getApplicationContext();
        CharSequence text = "Alignment has been reset.";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void ResetAll(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset App")
                .setMessage("Are you sure you want to clear all data from this app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ResetAlignmentData();
                        traj_vertices.clear();

                        blueprintImageView.setImageResource(android.R.color.transparent);

                        Context context = getApplicationContext();
                        CharSequence text = "The app has been reset";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }

                })
                .setNegativeButton("No", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
