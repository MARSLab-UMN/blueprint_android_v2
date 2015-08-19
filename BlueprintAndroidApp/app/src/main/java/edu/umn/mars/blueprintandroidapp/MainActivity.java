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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class MainActivity extends ActionBarActivity {
    ////Henry
    static public List<ImagePoint> imagePoints = new ArrayList<>();

    //In an Activity
    static public List<BlueprintAlignmentData> blueprint_data = new ArrayList<BlueprintAlignmentData>();

    static int mNumberOfBlueprints;
    static final public String DEBUG_TAG = "BlueprintAndroidApp";
    private static Context context;
    private ArrayList<String> mFileList = new ArrayList<String>();
    static final int state_vec_size = 16;
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

    // Alignment parameters and variables
    private int mActivePointerId = MotionEvent.INVALID_POINTER_ID;
    ScaleGestureDetector scaleDetector;
    //private float mLastRot, mLastTouchX, mLastTouchY;
    private float lastX, lastY;
    private double lastDist, origDist;
    private boolean onePointer = false;
    private boolean drawPath = false;


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
        drawView = (DrawView) findViewById(R.id.draw_view);
        blueprintImageView = (ImageView) findViewById(R.id.imageview);
        enterScaleButton = (Button) findViewById(R.id.enter_scale_button);
        enterScaleButton.setVisibility(View.INVISIBLE);
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

                lastDist = Math.sqrt(xDist*xDist + yDist*yDist);
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
        return true;
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

            int count = 0;
            while (scan.hasNextDouble()) {
                if (count % state_vec_size == 13) {
                    double xval = scan.nextDouble();
                    double yval = scan.nextDouble();
                    count++;
                    ImagePoint newPoint = new ImagePoint(xval, yval);
                    imagePoints.add(newPoint);
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
        if (imagePoints.isEmpty()) {
            Log.e(DEBUG_TAG, "No vertices loaded");
            return;
        }

        enterScaleButton.setVisibility(View.VISIBLE);
        GoToBlueprintAtIdx(mCurrentBlueprintIdx);
    }


    private void readAlignmentData() {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(mCurrentDir + mChosenFile);

            // load a properties file
            prop.load(input);

            // get the property value and print it out

            int blueprint_config_idx = -1;
            for (Properties.Entry<Object, Object> entry : prop.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                if (value.equals("blueprint")) {
                    String keySplit[] = key.trim().split("_");
                    blueprint_config_idx = Integer.parseInt(keySplit[keySplit.length-1]);
                    break;
                }
            }
            int numberOfBlueprints = Integer.parseInt(prop.getProperty("floors_" + blueprint_config_idx));
            ResetBlueprintData();
            mNumberOfBlueprints = numberOfBlueprints;
            setBlueprintClasses();

            for (int i = 0; i < mNumberOfBlueprints; i++) {
                blueprint_data.get(i).loadFromConfigProperties(prop, i, blueprint_config_idx);
            }

            GoToBlueprintAtIdx(0);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
                     System.getProperty("line.separator");

            int renderableIndx = 3;
            String blueprint_portion = "renderable_"+renderableIndx+" = blueprint" + System.getProperty("line.separator");
            blueprint_portion += "floors_"+renderableIndx+" = "+ mNumberOfBlueprints+ System.getProperty("line.separator");
            for (int i = 0; i < mNumberOfBlueprints; i++) {
                blueprint_portion += blueprint_data.get(i).createConfigFileString(i, renderableIndx);
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
        input.setText(".txt");

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
                                readImageData();
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


    public void EnterScale(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Enter Scale of Current Blueprint: 1 meter = X pixels");

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


    public void SelectImages(View view) {
        Dialog dialog = createFileSelectorDialog(Environment.getExternalStorageDirectory() + "/", LoadType.TRAJECTORY);
        dialog.show();
    }


    public void SelectBlueprint(View view) {
        Dialog dialog = createFileSelectorDialog(Environment.getExternalStorageDirectory() + "/", LoadType.BLUEPRINT);
        dialog.show();
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
                        imagePoints.clear();

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

        if (drawPath) {
            enterScaleButton.setVisibility(View.INVISIBLE);
        } else {
            enterScaleButton.setVisibility(View.VISIBLE);
        }
        return;
    }
}
