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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
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

    static int mNumberOfBlueprints;
    static final private String DEBUG_TAG = "BlueprintAndroidApp";
    private static Context context;
    private ArrayList<String> mFileList = new ArrayList<String>();
    static final int state_vec_size = 16;
    private String mChosenFile;
    private String mCurrentDir;
    private String mBlueprintFile;
    private LoadType mLoadType;
    private static final String FTYPE = ".txt";
    private static final String PNGTYPE = ".png";
    private static final String JPGTYPE = ".jpg";
    private static final String JPEGTYPE = ".jpeg";
    private ImageView.ScaleType mScaleType = ImageView.ScaleType.FIT_CENTER;


    // Views
    DrawView drawView;
    ImageView blueprintImageView;
    static public CheckBox lockXCheckBox;
    static public CheckBox lockYCheckBox;
    static public CheckBox lockRotationCheckBox;
    static public CheckBox lockScaleCheckBox;
    static public TextView measurementTextView;

    // alignment classes
    class BlueprintAlignmentData {
        public int x;
        public int y;
    }

    // Alignment parameters and variables
    static final float InitialTrajPosX = 0;
    static final float InitialTrajPosY = 0;
    static final float InitialTrajRot = 0;
    static final float InitialTrajScale = 100.0f;
    static float TrajPosX = InitialTrajPosX;
    static float TrajPosY = InitialTrajPosY;
    static float TrajRot = InitialTrajRot;
    static float TrajScale = InitialTrajScale;
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
        lockXCheckBox = (CheckBox) findViewById(R.id.lock_x);
        lockYCheckBox = (CheckBox) findViewById(R.id.lock_y);
        lockRotationCheckBox = (CheckBox) findViewById(R.id.lock_rotation);
        lockScaleCheckBox = (CheckBox) findViewById(R.id.lock_scale);
        drawView = (DrawView) findViewById(R.id.draw_view);
        blueprintImageView = (ImageView) findViewById(R.id.imageview);
        blueprintImageView.setScaleType(mScaleType);
        scaleDetector = new ScaleGestureDetector(getAppContext(), new ScaleListener());
        requestNumberOfBlueprints();
    }


    void requestNumberOfBlueprints() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Number of Blueprints")
                .setMessage("How many blueprints are you aligning for this building?")
                .setNegativeButton("1", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.mNumberOfBlueprints = 1;
                    }

                })
                .setNeutralButton("2", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.mNumberOfBlueprints = 2;
                    }

                })
                .setPositiveButton("More", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

                        alert.setTitle("Select Number of Blueprints");

                        // Set an EditText view to get user input
//                        final EditText input = new EditText(getApplicationContext());
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

                if (!lockXCheckBox.isChecked()) {
                    TrajPosX += dx;
                }
                if (!lockYCheckBox.isChecked()) {
                    TrajPosY += dy;
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

                    TrajRot += (dx / 2f) * (Math.PI / 180f);

                    if (TrajRot >= 2 * Math.PI) {
                        TrajRot -= 2 * Math.PI;
                    }

                    if (TrajRot < 0) {
                        TrajRot += 2 * Math.PI;
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
        String imageInSD = mCurrentDir + mChosenFile;
        Bitmap bitmap = BitmapFactory.decodeFile(imageInSD);
        try {
            ImageView myImageView = (ImageView) findViewById(R.id.imageview);
            myImageView.setImageBitmap(bitmap);
            mBlueprintFile = mChosenFile;
        } catch (Exception e) {

            Toast.makeText(getBaseContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
     readDimensions();
    }

    private void readDimensions(){

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(mCurrentDir + mChosenFile, options);
        int bwidth = options.outWidth;
        int bheight = options.outHeight;
        int actualHeight, actualWidth;
        int imageViewHeight = blueprintImageView.getHeight(), imageViewWidth = blueprintImageView.getWidth();
        int bitmapHeight =bheight, bitmapWidth = bwidth;
        if (imageViewHeight * bitmapWidth <= imageViewWidth * bitmapHeight) {
            actualWidth = bitmapWidth * imageViewHeight / bitmapHeight;
            actualHeight = imageViewHeight;
        } else {
            actualHeight = bitmapHeight * imageViewWidth / bitmapWidth;
            actualWidth = imageViewWidth;
        }

        Log.i(DEBUG_TAG, "It works !");
        Context context = getApplicationContext();
        CharSequence text = "Bw = "+bwidth+"|Iw = "+imageViewWidth+"|Aw = "+actualWidth;
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        Log.i(DEBUG_TAG,"Bw = "+bwidth+"|Iw = "+imageViewWidth+"|Aw = "+actualWidth);

    }

    private void readAlignmentData() {
        try {
            File myFile = new File(mCurrentDir + mChosenFile);
            Scanner scan = new Scanner(myFile);

            TrajPosX = scan.nextFloat();
            TrajPosY = scan.nextFloat();
            TrajRot = scan.nextFloat();
            TrajScale = scan.nextFloat();

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

    boolean doWriteToFile(File file, boolean shouldAppend) {
        boolean success = false;
        try {
            success = file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file, shouldAppend);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            myOutWriter.append(Float.toString(TrajPosX) + " ");
            myOutWriter.append(Float.toString(TrajPosY) + " ");
            myOutWriter.append(Float.toString(TrajRot) + " ");
            myOutWriter.append(Float.toString(TrajScale) + " ");
            myOutWriter.append(System.getProperty("line.separator"));
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
//                        SaveCurrentAlignment();
                        success = doWriteToFile(file, false);
                        return;

                    } else {
                        boolean shouldAppend = false;
                        success = doWriteToFile(file, shouldAppend);
                    }
                    if (success) {
                        Log.i(DEBUG_TAG, "Created alignment file: " + newFile);
                        Toast.makeText(getBaseContext(),"Created alignment file: " + newFile,
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
                                readImageData();
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

    public void SaveAlignment(View view) {
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

    public void SelectToggle(View view) {

        if (mScaleType == ImageView.ScaleType.CENTER) {
            mScaleType = ImageView.ScaleType.FIT_CENTER;
        } else {
            mScaleType = ImageView.ScaleType.CENTER;
        }
        blueprintImageView.setScaleType(mScaleType);

        readDimensions();





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
