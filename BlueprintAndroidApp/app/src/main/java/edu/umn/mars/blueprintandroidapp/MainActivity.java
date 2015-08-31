package edu.umn.mars.blueprintandroidapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class MainActivity extends ActionBarActivity {
    ////Henry


    //In an Activity
    static public List<BlueprintAlignmentData> blueprint_data = new ArrayList<BlueprintAlignmentData>();

    static int mNumberOfBlueprints;
    static final public String DEBUG_TAG = "BlueprintAndroidApp";
    private static Context context;
    private ArrayList<String> mFileList = new ArrayList<String>();
    static final int state_vec_size = 16;
    static final float dist_between_points = 50.0f;
    private String mChosenFile;
    private String mCurrentDir;
    public static int mCurrentBlueprintIdx;
    private LoadType mLoadType;
    private static final String FTYPE = ".txt";
    private static final String PNGTYPE = ".png";
    private static final String JPGTYPE = ".jpg";
    private static final String JPEGTYPE = ".jpeg";
    private ImageView.ScaleType mScaleType = ImageView.ScaleType.FIT_CENTER;

    // Views
    static public DrawView drawView;
    static public ImageView blueprintImageView;
    static public Button enterScaleButton;
    static public TextView measurementTextView;
    static public TextView currentBlueprintTextView;
    static public Button drawPathButton;
    static public Button loadImagesButton;
    static public Button mapPathButton;

    // Alignment parameters and variables
    private int mActivePointerId = MotionEvent.INVALID_POINTER_ID;
    ScaleGestureDetector scaleDetector;
    //private float mLastRot, mLastTouchX, mLastTouchY;
    private float lastX, lastY;
    private double lastDist, origDist;
    private boolean onePointer = false;
    static public boolean drawPath = false;


    public enum LoadType {
        BLUEPRINT, TRAJECTORY
    }


    public static Context getAppContext() {
        return MainActivity.context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);

        measurementTextView = (TextView) findViewById(R.id.current_alignment_measurements);
        currentBlueprintTextView = (TextView) findViewById(R.id.current_blueprint_label);
        drawView = (DrawView) findViewById(R.id.draw_view);
        blueprintImageView = (ImageView) findViewById(R.id.imageview);
        enterScaleButton = (Button) findViewById(R.id.enter_scale_button);
        drawPathButton = (Button) findViewById(R.id.draw_path_button);
        loadImagesButton = (Button) findViewById(R.id.btn_load_images);
        mapPathButton = (Button) findViewById(R.id.next_screen_button);

        enterScaleButton.setVisibility(View.INVISIBLE);
        drawPathButton.setVisibility(View.INVISIBLE);
        loadImagesButton.setVisibility(View.INVISIBLE);
        mapPathButton.setVisibility(View.INVISIBLE);

        blueprintImageView.setScaleType(mScaleType);

        scaleDetector = new ScaleGestureDetector(getAppContext(), new ScaleListener());
        requestNumberOfBlueprints();
    }


    void GoToBlueprintAtIdx(int idx) {
        if (idx >= mNumberOfBlueprints) {
            mCurrentBlueprintIdx = mNumberOfBlueprints - 1;
        } else if (idx < 0) {
            mCurrentBlueprintIdx = 0;
        } else {
            mCurrentBlueprintIdx = idx;
        }

        currentBlueprintTextView.setText("Blueprint " + (mCurrentBlueprintIdx+1) + " of " + mNumberOfBlueprints);

        Button next_button = (Button) findViewById(R.id.next_blueprint_button);
        if (mCurrentBlueprintIdx >= mNumberOfBlueprints-1) {
            next_button.setEnabled(false);
        } else {
            next_button.setEnabled(true);
        }

        Button previous_button = (Button) findViewById(R.id.back_blueprint_button);
        if (mCurrentBlueprintIdx == 0) {
            previous_button.setEnabled(false);
        } else {
            previous_button.setEnabled(true);
        }


        try {
            blueprintImageView.setImageBitmap(blueprint_data.get(mCurrentBlueprintIdx).imageBitmap);
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }

        BlueprintAlignmentData curBlueprint = blueprint_data.get(mCurrentBlueprintIdx);

        if (curBlueprint.blueprintFileLocation == null) {
            loadImagesButton.setVisibility(View.INVISIBLE);
            enterScaleButton.setVisibility(View.INVISIBLE);
            drawPathButton.setVisibility(View.INVISIBLE);
            drawPath = false;
            drawPathButton.setText("Draw Path");
        } else if (curBlueprint.imagePoints.size() == 0) {
            loadImagesButton.setVisibility(View.VISIBLE);
            enterScaleButton.setVisibility(View.INVISIBLE);
            drawPathButton.setVisibility(View.INVISIBLE);
            drawPath = false;
            drawPathButton.setText("Draw Path");
        } else {
            loadImagesButton.setVisibility(View.VISIBLE);
            enterScaleButton.setVisibility(View.VISIBLE);
            drawPathButton.setVisibility(View.VISIBLE);
        }

        drawView.invalidate();
        drawView.requestLayout();
    }


    void setBlueprintClasses() {
        blueprint_data.clear();
        for (int i = 0; i < mNumberOfBlueprints; i++) {
            blueprint_data.add(new BlueprintAlignmentData());
        }

        GoToBlueprintAtIdx(0);
    }


    void requestNumberOfBlueprints() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Number of Blueprints")
                .setMessage("How many blueprints are you aligning for this building?")
                .setNegativeButton("1", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.mNumberOfBlueprints = 1;
                        setBlueprintClasses();
                    }

                })
                .setNeutralButton("2", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.mNumberOfBlueprints = 2;
                        setBlueprintClasses();
                    }

                })
                .setPositiveButton("More", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                        alert.setTitle("Select Number of Blueprints");

                        final NumberPicker np = new NumberPicker(MainActivity.this);
                        np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                        np.setMinValue(1);
                        np.setMaxValue(20);
                        np.setValue(3);
                        alert.setView(np);

                        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
//                                int value = Integer.getInteger(input.getText().toString());
                                MainActivity.mNumberOfBlueprints = np.getValue();
                                setBlueprintClasses();
                                Log.i(DEBUG_TAG, "num: " + mNumberOfBlueprints);
                            }
                        });
                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                requestNumberOfBlueprints();
                            }
                        });
                        alert.setCancelable(false);
                        alert.show();
                    }

                });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajScale *= detector.getScaleFactor();
            MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajScale = Math.max(0.1f, MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajScale);

            drawView.invalidate();
            drawView.requestLayout();

            return true;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;

        int num_pointers = event.getPointerCount();

        if (blueprint_data.get(mCurrentBlueprintIdx).imagePoints.size() == 0) {
            result = super.onTouchEvent(event);
            return result;
        }

        switch (num_pointers) {
            case 1:
                if (drawPath) {
                    result = handleDrawPath(event);
                } else {
                    result = handleOnePointer(event);
                }
                break;
            case 2:
                if (!drawPath) {
                    result = handleTwoPointers(event);
                }
                break;
            default:
                result = super.onTouchEvent(event);
        }

        return result;
    }


    // Two finger touch and drag
    private boolean handleTwoPointers(MotionEvent event) {
        boolean result = false;

        onePointer = false;

        final int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                // really, should not hit this, because there are two pointers
                result = true;
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN: {
                float x0 = MotionEventCompat.getX(event, 0);
                float y0 = MotionEventCompat.getY(event, 0);
                float x1 = MotionEventCompat.getX(event, 1);
                float y1 = MotionEventCompat.getY(event, 1);
                float xDist = x1 - x0;
                float yDist = y1 - y0;

                lastDist = Math.sqrt(xDist * xDist + yDist * yDist);
                origDist = Math.max(lastDist, 500);

                // Save the ID of this pointer (for dragging)
                mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                result = true;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                float x0 = MotionEventCompat.getX(event, 0);
                float y0 = MotionEventCompat.getY(event, 0);
                float x1 = MotionEventCompat.getX(event, 1);
                float y1 = MotionEventCompat.getY(event, 1);
                float xDist = x1 - x0;
                float yDist = y1 - y0;

                double curDist = Math.sqrt(xDist*xDist + yDist*yDist);
                double dDist = curDist - lastDist;
                dDist /= (origDist/6f);
                lastDist = curDist;

                blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajRot += dDist;

                drawView.invalidate();

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


    // Single finger touch and drag
    private boolean handleOnePointer(MotionEvent event) {
        boolean result = false;

        final int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int pointerIndex = MotionEventCompat.getActionIndex(event);

                onePointer = true;
                lastX = MotionEventCompat.getX(event, pointerIndex);
                lastY = MotionEventCompat.getY(event, pointerIndex);

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
                if (!onePointer) {
                    return true;
                }

                float curX = MotionEventCompat.getX(event, pointerIndex);
                float curY = MotionEventCompat.getY(event, pointerIndex);

                float dx = curX - lastX;
                float dy = curY - lastY;

                lastX = curX;
                lastY = curY;

                blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajPosX += dx;
                blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajPosY += dy;

                drawView.invalidate();
                drawView.requestLayout();

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

    private boolean handleDrawPath(MotionEvent event) {
        boolean result = false;
        final int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int pointerIndex = MotionEventCompat.getActionIndex(event);

                if (blueprint_data.get(MainActivity.mCurrentBlueprintIdx).trajPoint == null) {
                    float curX = MotionEventCompat.getX(event, pointerIndex);
                    float curY = MotionEventCompat.getY(event, pointerIndex);

                    int[] corners = new int[2];
                    drawView.getLocationOnScreen(corners);

                    TrajectoryPoint curPoint = new TrajectoryPoint(curX - corners[0], curY - corners[1]);
                    blueprint_data.get(MainActivity.mCurrentBlueprintIdx).trajPoint = curPoint;
                    blueprint_data.get(MainActivity.mCurrentBlueprintIdx).curTrajPoint = curPoint;
                } else {
                    TrajectoryPoint currentPoint = blueprint_data.get(MainActivity.mCurrentBlueprintIdx).curTrajPoint;
                    int[] corners = new int[2];
                    drawView.getLocationOnScreen(corners);

                    float curX = MotionEventCompat.getX(event, pointerIndex);
                    float curY = MotionEventCompat.getY(event, pointerIndex);

                    float distX = Math.abs(curX - currentPoint.getX());
                    float distY = Math.abs(curY - currentPoint.getY());

                    if ((distX >= dist_between_points) || (distY >= dist_between_points)) {
                        TrajectoryPoint curPoint = new TrajectoryPoint(curX - corners[0], curY - corners[1]);
                        currentPoint.setNextPoint(curPoint);
                        blueprint_data.get(MainActivity.mCurrentBlueprintIdx).curTrajPoint = curPoint;
                    }
                }

                drawView.invalidate();
                drawView.requestLayout();
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

                TrajectoryPoint currentPoint = blueprint_data.get(MainActivity.mCurrentBlueprintIdx).curTrajPoint;
                int[] corners = new int[2];
                drawView.getLocationOnScreen(corners);

                float curX = MotionEventCompat.getX(event, pointerIndex);
                float curY = MotionEventCompat.getY(event, pointerIndex);

                float distX = Math.abs(curX - currentPoint.getX());
                float distY = Math.abs(curY - currentPoint.getY());

                if ((distX >= dist_between_points) || (distY >= dist_between_points)) {
                    TrajectoryPoint curPoint = new TrajectoryPoint(curX - corners[0], curY - corners[1]);
                    currentPoint.setNextPoint(curPoint);
                    blueprint_data.get(MainActivity.mCurrentBlueprintIdx).curTrajPoint = curPoint;
                }

                drawView.invalidate();
                drawView.requestLayout();

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


    // Read in image locations from text file
    private void readImageData() {
        try {
            File myFile = new File(mCurrentDir + mChosenFile);
            Scanner scan = new Scanner(myFile);

            List<ImagePoint> imagePoints = blueprint_data.get(mCurrentBlueprintIdx).imagePoints;

            int count = 0;
            int imageID = 1;
            while (scan.hasNextDouble()) {
                if (count % state_vec_size == 13) {
                    double xval = scan.nextDouble();
                    double yval = scan.nextDouble();
                    count++;
                    ImagePoint newPoint = new ImagePoint(xval, yval, imageID);
                    imagePoints.add(newPoint);
                    imageID++;
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


    private void displayImageData() {
        List<ImagePoint> imagePoints = blueprint_data.get(mCurrentBlueprintIdx).imagePoints;

        if (imagePoints.isEmpty()) {
            Log.e(DEBUG_TAG, "No vertices loaded");
            return;
        }

        enterScaleButton.setVisibility(View.VISIBLE);
        GoToBlueprintAtIdx(mCurrentBlueprintIdx);
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
            List<String> fileList = Arrays.asList(mPath.list(fileFilter));
            mFileList.addAll(fileList);
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


    private void CreateNewDirectory() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Create New Directory");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setHint("New directory name (please avoid spaces)");
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                if (value.isEmpty() || value.contains(" ")) {
                    CharSequence text = "Please enter a directory name without any spaces.";

                    Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                    toast.show();
                    CreateNewDirectory();
                } else {
                    String newDir = mCurrentDir + value;
                    if (!newDir.endsWith("/")) {
                        newDir += "/";
                    }
                    File folder = new File(newDir);
                    boolean success = true;
                    if (!folder.exists()) {
                        success = folder.mkdir();
                    }
                    if (success) {
                        Log.i(DEBUG_TAG, "Created directory: " + newDir);
                        createFileSelectorDialog(mCurrentDir, mLoadType);
                    } else {
                        // Do something else on failure
                        Log.e(DEBUG_TAG, "Unable to create new directory: " + newDir);
                    }
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
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
                                blueprint_data.get(mCurrentBlueprintIdx).LoadBlueprintFile(mCurrentDir + mChosenFile, blueprintImageView);
                                GoToBlueprintAtIdx(mCurrentBlueprintIdx);
                            } else if (FileIsData() && mLoadType == LoadType.TRAJECTORY) {
                                Log.i(DEBUG_TAG, "You have selected a trajectory data file.");
                                readImageData();
                                displayImageData();
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
            default:
                dialog.setTitle("Command not implemented");
                break;
        }
        return dialog;
    }


    public void EnterScale(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Enter Scale of Current Blueprint: 1 foot = X pixels");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                float value;
                try {
                    value = Float.parseFloat(input.getText().toString());
                    blueprint_data.get(mCurrentBlueprintIdx).TrajScale = value;
                    GoToBlueprintAtIdx(mCurrentBlueprintIdx);
                } catch (NumberFormatException e) {
                    CharSequence text = "Please enter a floating point number.";
                    Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();

        drawPathButton.setVisibility(View.VISIBLE);
    }


    public void NextBlueprint(View view) {
        GoToBlueprintAtIdx(mCurrentBlueprintIdx + 1);
    }


    public void PreviousBlueprint(View view) {
            GoToBlueprintAtIdx(mCurrentBlueprintIdx - 1);
    }


    public void SelectImages(View view) {
        Dialog dialog = createFileSelectorDialog(Environment.getExternalStorageDirectory() + "/", LoadType.TRAJECTORY);
        dialog.show();
        enterScaleButton.setVisibility(View.VISIBLE);
    }


    public void SelectBlueprint(View view) {
        Dialog dialog = createFileSelectorDialog(Environment.getExternalStorageDirectory() + "/", LoadType.BLUEPRINT);
        dialog.show();
        loadImagesButton.setVisibility(View.VISIBLE);
    }


    public void ResetAlignmentData() {
        for (int i = 0; i < mNumberOfBlueprints; i++) {
            blueprint_data.get(i).ResetAlignmentData();
        }

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

    //clear drawn path for current blueprint
    public void ClearPath(View view) {
        blueprint_data.get(mCurrentBlueprintIdx).trajPoint = null;
        blueprint_data.get(mCurrentBlueprintIdx).curTrajPoint = null;
        drawView.invalidate();
        drawView.requestLayout();
    }


    public void ResetBlueprintData() {
        blueprint_data.clear();
        blueprintImageView.setImageResource(android.R.color.transparent);
    }


    public void ResetAll(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset App")
                .setMessage("Are you sure you want to clear all data from this app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ResetAlignmentData();

                        blueprint_data.get(mCurrentBlueprintIdx).imagePoints.clear();
                        drawPath = false;
                        enterScaleButton.setVisibility(View.INVISIBLE);
                        loadImagesButton.setVisibility(View.INVISIBLE);
                        drawPathButton.setVisibility(View.INVISIBLE);
                        mapPathButton.setVisibility(View.INVISIBLE);
                        drawPathButton.setText("Draw Path");
                        drawView.invalidate();
                        drawView.requestLayout();

                        ResetBlueprintData();
                        requestNumberOfBlueprints();
                        setBlueprintClasses();

                        Context context = getApplicationContext();
                        CharSequence text = "The app has been reset.";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();


                    }

                })
                .setNegativeButton("No", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void DrawPath(View view) {
        drawPath = !drawPath;
        mapPathButton.setVisibility(View.VISIBLE);

        if (drawPath) {
            enterScaleButton.setVisibility(View.INVISIBLE);
            loadImagesButton.setVisibility(View.INVISIBLE);
            drawPathButton.setText("Move Images");
            drawView.invalidate();
            drawView.requestLayout();
        } else {
            enterScaleButton.setVisibility(View.VISIBLE);
            loadImagesButton.setVisibility(View.VISIBLE);
            drawPathButton.setText("Draw Path");
            drawView.invalidate();
            drawView.requestLayout();
        }
        return;
    }


    public void GoToNextScreen(View view) {
        Intent i = new Intent(getApplicationContext(), PathStackView.class);
        startActivity(i);
    }
}
