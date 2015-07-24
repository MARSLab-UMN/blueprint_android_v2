package edu.umn.mars.blueprintandroidapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends ActionBarActivity {
    //In an Activity
    static public List<Double> traj_vertices = new ArrayList<Double>();
    static public List<Integer> traj_vertices_buckets = new ArrayList<Integer>();
    static public float MaxZ, MinZ;
    static public int MaxBucket;
    static final float BucketPrecision = 50;
    static public List<BlueprintAlignmentData> blueprint_data = new ArrayList<BlueprintAlignmentData>();

    static int mNumberOfBlueprints;
    static final public String DEBUG_TAG = "BlueprintAndroidApp";
    private static Context context;
    private ArrayList<String> mFileList = new ArrayList<String>();
    static final int state_vec_size = 16;
    private String mChosenFile;
    private String mCurrentDir;
    private String mBlueprintFile;
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
    static public CheckBox lockXCheckBox;
    static public CheckBox lockYCheckBox;
    static public CheckBox lockRotationCheckBox;
    static public CheckBox lockScaleCheckBox;
    static public CheckBox lockMinZ;
    static public CheckBox lockMaxZ;
    static public CheckBox lockZCheckBox;
    static public TextView measurementTextView;
    static public TextView currentBlueprintTextView;
    static public ZHeightDoubleSeekBar maxHeightSeekBar;
    static public LinearLayout zSelectionGroup;

    // Alignment parameters and variables
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
        measurementTextView = (TextView) findViewById(R.id.current_alignment_measurements);
        currentBlueprintTextView = (TextView) findViewById(R.id.current_blueprint_label);
        lockXCheckBox = (CheckBox) findViewById(R.id.lock_x);
        lockYCheckBox = (CheckBox) findViewById(R.id.lock_y);
        lockRotationCheckBox = (CheckBox) findViewById(R.id.lock_rotation);
        lockScaleCheckBox = (CheckBox) findViewById(R.id.lock_scale);
        lockZCheckBox = (CheckBox) findViewById(R.id.lock_z_height);
        lockMinZ = (CheckBox) findViewById(R.id.lock_min_inf);
        lockMaxZ = (CheckBox) findViewById(R.id.lock_max_inf);
        drawView = (DrawView) findViewById(R.id.draw_view);
        blueprintImageView = (ImageView) findViewById(R.id.imageview);
        maxHeightSeekBar = (ZHeightDoubleSeekBar) findViewById(R.id.z_height_seek_bar);
        zSelectionGroup = (LinearLayout) findViewById(R.id.z_selection_group);
        zSelectionGroup.setVisibility(View.INVISIBLE);
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
            next_button.setText("Save Config File");
        } else {
            next_button.setText("Next Blueprint");
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

        maxHeightSeekBar.setLowerValue(blueprint_data.get(mCurrentBlueprintIdx).MinZ);
        maxHeightSeekBar.setUpperValue(blueprint_data.get(mCurrentBlueprintIdx).MaxZ);

        lockMinZ.setChecked(blueprint_data.get(mCurrentBlueprintIdx).LockMinZ);
        lockMaxZ.setChecked(blueprint_data.get(mCurrentBlueprintIdx).LockMaxZ);

        drawView.invalidate();
        drawView.requestLayout();
        maxHeightSeekBar.invalidate();
        maxHeightSeekBar.requestLayout();
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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        switch (id) {
//            case R.id.action_settings:
//                PrintNotYetImplemented("SelectSettings");
//                return true;
//            case R.id.action_load_alignment:
//                LoadAlignment();
//                return true;
//            case R.id.action_save_alignment:
//                SaveAlignment();
//                return true;
//            default:
//                break;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (lockScaleCheckBox.isChecked()) {
                return true;
            }

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
                float trajScale = MainActivity.blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajScale;
                final float dx = (x - mLastTouchX)*blueprint_data.get(MainActivity.mCurrentBlueprintIdx).getBlueprintToImageViewPixelsX();
                final float dy = (y - mLastTouchY)*blueprint_data.get(MainActivity.mCurrentBlueprintIdx).getBlueprintToImageViewPixelsY();

                if (!lockXCheckBox.isChecked()) {
                    blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajPosX += dx;
                }
                if (!lockYCheckBox.isChecked()) {
                    blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajPosY += dy;
                }

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

                if (!lockRotationCheckBox.isChecked()) {

                    blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajRot += (dx / 2f) * (Math.PI / 180f);

                    if (blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajRot >= 2 * Math.PI) {
                        blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajRot -= 2 * Math.PI;
                    }

                    if (blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajRot < 0) {
                        blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajRot += 2 * Math.PI;
                    }
                }

                drawView.invalidate();
                drawView.requestLayout();

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
        traj_vertices_buckets.clear();

        try {
            File myFile = new File(mCurrentDir + mChosenFile);
            Scanner scan = new Scanner(myFile);

            int count = 0;
            while (scan.hasNextDouble()) {
                if (count % state_vec_size == 13 || count % state_vec_size == 14 || count % state_vec_size == 15) {
                    double d = scan.nextDouble();
                    traj_vertices.add(d);
                    if (count == 15) {
                        MaxZ = (float) d;
                        MinZ = (float) d;
                    }
                    if (count % state_vec_size == 15) {
                        if (MaxZ < d) {
                            MaxZ = (float) d;
                        }
                        if (MinZ > d) {
                            MinZ = (float) d;
                        }
                    }
                } else {
                    scan.nextDouble();
                }
                count++;
            }

            int numberOfBuckets = (int) Math.ceil((MainActivity.MaxZ - MainActivity.MinZ) * BucketPrecision) + 1;
            for (int i = 0; i < numberOfBuckets; i++) {
                traj_vertices_buckets.add(0);
            }
            MaxBucket = 0;
            for (int i = 2; i < MainActivity.traj_vertices.size(); i+=3) {
                int bucket = (int) Math.ceil((traj_vertices.get(i) - MinZ) * BucketPrecision);
                traj_vertices_buckets.set(bucket, traj_vertices_buckets.get(bucket) + 1);
                if (MaxBucket < traj_vertices_buckets.get(bucket)) {
                    MaxBucket = traj_vertices_buckets.get(bucket);
                }
            }

            zSelectionGroup.setVisibility(View.VISIBLE);
            maxHeightSeekBar.invalidate();
            maxHeightSeekBar.requestLayout();
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




    private void readAlignmentData() {
        try {
            File myFile = new File(mCurrentDir + mChosenFile);
            Scanner scan = new Scanner(myFile);

            blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajPosX = scan.nextFloat();
            blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajPosY = scan.nextFloat();
            blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajRot = scan.nextFloat();
            blueprint_data.get(MainActivity.mCurrentBlueprintIdx).TrajScale = scan.nextFloat();

            drawView.invalidate();
            drawView.requestLayout();

        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFileList(String baseFolderPath) {
        File mPath = new File(baseFolderPath);
        if (mLoadType == LoadType.SAVE_ALIGNMENT) {
            mFileList.add("SAVE IN CURRENT DIRECTORY");
            mFileList.add("CREATE NEW DIRECTORY");
        }

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

    boolean doWriteToFile(File file) {
        boolean success = false;
        try {
            success = file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file, false);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            String opening = "num_renderables = 5" + System.getProperty("line.separator") +
                    "renderable_0 = image" +System.getProperty("line.separator") +
                    "size_0 = [1920, 1080]" +System.getProperty("line.separator") +
                    "position_0 = [0,0]" +System.getProperty("line.separator") +
                    "page_0 = 0" +System.getProperty("line.separator") +
                     System.getProperty("line.separator") +
                    "renderable_1 = points" +System.getProperty("line.separator") +
                    "size_1 = [1920, 1080]" +System.getProperty("line.separator") +
                    "position_1 = [0,0]" +System.getProperty("line.separator") +
                    "page_1 = 0" +System.getProperty("line.separator") +
                     System.getProperty("line.separator") +
                    "renderable_2 = trajectory" +System.getProperty("line.separator") +
                    "size_2 = [1920, 1080]" +System.getProperty("line.separator") +
                    "position_2 = [0,0]" +System.getProperty("line.separator") +
                    "page_2 = 0" +System.getProperty("line.separator") +
                     System.getProperty("line.separator") +
                     System.getProperty("line.separator") +
                     System.getProperty("line.separator")
                    ;

            String blueprint_portion = "renderable_3 = blueprint" + System.getProperty("line.separator");
            blueprint_portion += "floors_3 = "+ mNumberOfBlueprints+ System.getProperty("line.separator");
            for (int i = 0; i < mNumberOfBlueprints; i++) {
                blueprint_portion +="### floor " + (i+1)+System.getProperty("line.separator");
                blueprint_portion += "px_per_meter_"+i+"_3 = " + Float.toString(blueprint_data.get(i).TrajPosX)+ System.getProperty("line.separator");
                blueprint_portion += "theta_"+i+"_3 = " + Float.toString(blueprint_data.get(i).TrajPosX) + System.getProperty("line.separator");
                blueprint_portion += "z_range_"+i+"_3 = " + Float.toString(blueprint_data.get(i).TrajPosY) + System.getProperty("line.separator");
                blueprint_portion += "origin_"+i+"_3 = " + Float.toString(blueprint_data.get(i).TrajRot) + System.getProperty("line.separator");
                blueprint_portion += "blueprint_file_"+i+"_3 = " +Float.toString(blueprint_data.get(i).TrajScale)+ System.getProperty("line.separator");

               /* blueprint_portion += Float.toString(blueprint_data.get(i).TrajPosX) + System.getProperty("line.separator") ;
                blueprint_portion += Float.toString(blueprint_data.get(i).TrajPosY) + System.getProperty("line.separator") ;
                blueprint_portion += Float.toString(blueprint_data.get(i).TrajRot) + System.getProperty("line.separator") ;
                blueprint_portion += Float.toString(blueprint_data.get(i).TrajScale) + System.getProperty("line.separator") ;
                */
                blueprint_portion +=System.getProperty("line.separator");
            }

            String closing_portion = "size_3 = [1920, 1080]" +System.getProperty("line.separator") +
                    "position_3 = [0, 0]" +System.getProperty("line.separator") +
                    "page_3 = 1" +System.getProperty("line.separator") +
                    System.getProperty("line.separator") +
                    System.getProperty("line.separator") +
                    "renderable_4 = image" + System.getProperty("line.separator") +
                    "size_4 = [160,120]" + System.getProperty("line.separator") +
                    "position_4 = [640, 480]" + System.getProperty("line.separator") +
                    "page_4 = 1";

            myOutWriter.append(opening);
            myOutWriter.append(blueprint_portion);
            myOutWriter.append(closing_portion);

                    myOutWriter.close();
            fOut.close();

        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }

        return success;
    }

    private void SaveCurrentAlignment() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Currently saving in: " + mCurrentDir.replace(Environment.getExternalStorageDirectory().toString(), "/sdcard"));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        if (mBlueprintFile != null && !mBlueprintFile.isEmpty()) {
            input.setText(mBlueprintFile.substring(0, mBlueprintFile.lastIndexOf('.')) + "_alignment.txt");
        }
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                if (value.isEmpty() || value.contains(" ")) {
                    CharSequence text = "Please enter a file name without any spaces.";

                    Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                    toast.show();
                    SaveCurrentAlignment();
                } else {
                    String newFile = mCurrentDir + value;

                    File file = new File(newFile);
                    boolean success = true;
                    if (file.exists()) {
                        String message = "File '" + newFile + "' already exists. Please choose another name.";
                        Log.e(DEBUG_TAG, message);
                        Toast.makeText(getBaseContext(), message,
                                Toast.LENGTH_SHORT).show();
                        SaveCurrentAlignment();
                        return;

                    } else {
                        success = doWriteToFile(file);
                    }
                    if (success) {
                        Log.i(DEBUG_TAG, "Created alignment file: " + newFile);
                        Toast.makeText(getBaseContext(), "Created alignment file: " + newFile,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Do something else on failure
                        Log.e(DEBUG_TAG, "Unable to create new alignment file: " + newFile);
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
                                readTrajData();
                                displayTrajData();
                            } else if (FileIsData() && mLoadType == LoadType.LOAD_ALIGNMENT) {
                                readAlignmentData();
                            } else if (mLoadType == LoadType.SAVE_ALIGNMENT && which == 0) {
                                // selected to save the file in this directory
                                SaveCurrentAlignment();
                            } else if (mLoadType == LoadType.SAVE_ALIGNMENT && which == 1) {
                                // selected to create new directory
                                CreateNewDirectory();
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

    public void LoadAlignment(View view) {
        Dialog dialog = createFileSelectorDialog(Environment.getExternalStorageDirectory() + "/", LoadType.LOAD_ALIGNMENT);
        dialog.show();
    }

    public void NextBlueprint(View view) {
        if (mCurrentBlueprintIdx >= mNumberOfBlueprints-1) {
            Dialog fileSelectDialog = createFileSelectorDialog(Environment.getExternalStorageDirectory() + "/", LoadType.SAVE_ALIGNMENT);
            fileSelectDialog.show();
        } else {
            GoToBlueprintAtIdx(mCurrentBlueprintIdx+1);
        }
    }

    public void PreviousBlueprint(View view) {
            GoToBlueprintAtIdx(mCurrentBlueprintIdx - 1);
    }

    public void SelectTrajectory(View view) {
        Dialog dialog = createFileSelectorDialog(Environment.getExternalStorageDirectory() + "/", LoadType.TRAJECTORY);
        dialog.show();
    }

    public void SelectBlueprint(View view) {
        Dialog dialog = createFileSelectorDialog(Environment.getExternalStorageDirectory() + "/", LoadType.BLUEPRINT);
        dialog.show();
    }

    public void ToggleZSelection (View view) {
        if (lockZCheckBox.isChecked() || traj_vertices.isEmpty()) {
            zSelectionGroup.setVisibility(View.INVISIBLE);
        } else {
            zSelectionGroup.setVisibility(View.VISIBLE);
        }
    }

    public void LockMaxHeight(View view) {
        blueprint_data.get(mCurrentBlueprintIdx).LockMaxZ = lockMaxZ.isChecked();;
        maxHeightSeekBar.invalidate();
        maxHeightSeekBar.requestLayout();
        drawView.invalidate();
        drawView.requestLayout();
    }

    public void LockMinHeight(View view) {
        blueprint_data.get(mCurrentBlueprintIdx).LockMinZ = lockMinZ.isChecked();;
        maxHeightSeekBar.invalidate();
        maxHeightSeekBar.requestLayout();
        drawView.invalidate();
        drawView.requestLayout();
    }

    public void ResetAlignmentData() {
        blueprint_data.get(mCurrentBlueprintIdx).ResetAlignmentData();

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

    public void SelectToggle(View view) {
        // if you change these, you will likely have to change how the scale is calculated
        // We assume it fills the parent
        if (mScaleType == ImageView.ScaleType.FIT_XY) {
            mScaleType = ImageView.ScaleType.FIT_CENTER;
        } else {
            mScaleType = ImageView.ScaleType.FIT_XY;
        }
        blueprintImageView.setScaleType(mScaleType);

        blueprintImageView.invalidate();
        blueprintImageView.requestLayout();

        drawView.invalidate();
        drawView.requestLayout();
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
                        traj_vertices_buckets.clear();
                        blueprint_data.clear();

                        blueprintImageView.setImageResource(android.R.color.transparent);
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
}
