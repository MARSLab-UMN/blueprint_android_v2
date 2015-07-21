package edu.umn.mars.blueprintandroidapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {
    //In an Activity
    static final private String LOG_TAG = "BlueprintAndroidApp";
    private ArrayList<String> mFileList = new ArrayList<String>();;
    private String mChosenFile;
    private String mCurrentDir;
    private LoadType mLoadType;
    private static final String FTYPE = ".txt";
    private static final String PNGTYPE = ".png";
    private static final String JPGTYPE = ".jpg";
    private List<Float> traj_vertices;

    public enum LoadType {
        BLUEPRINT, TRAJECTORY
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    private void PrintNotYetImplemented(CharSequence functionName) {
        Context context = getApplicationContext();
        CharSequence text = functionName + " not yet implemented";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void readTrajData() {
        // write on SD card file data in the text box
        try {
            File myFile = new File(mCurrentDir + mChosenFile);
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(
                    new InputStreamReader(fIn));
            String aDataRow = "";
            String aBuffer = "";
            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow + "\n";
            }
            myReader.close();
            Toast.makeText(getBaseContext(),
                    "Done reading " + mChosenFile,
                    Toast.LENGTH_SHORT).show();
            Toast.makeText(getBaseContext(),
                    aBuffer,
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void readImageData() {
        PrintNotYetImplemented("readImageData");
    }

    private void loadFileList(String baseFolderPath) {
        File mPath = new File(baseFolderPath);

        try {
            mPath.mkdirs();
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "unable to write on the sd card " + e.toString());
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
                    return !sel.isDirectory();
                }

            };
            List<String> dirList = Arrays.asList(mPath.list(dirFilter));
            mFileList.addAll(dirList);
            List<String> fileList = Arrays.asList(mPath.list(fileFilter));
            mFileList.addAll(fileList);
        } else {
            mFileList.clear();
        }
    }

    private boolean FileIsImage() {
        return mChosenFile.contains(PNGTYPE) || mChosenFile.contains(JPGTYPE);
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
        loadFileList(mCurrentDir);
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Choose your file");
        if (mFileList.isEmpty()) {
            Log.e(LOG_TAG, "Showing file picker before loading the file list");
            dialog = builder.create();
            return dialog;
        }
        builder.setItems(mFileList.toArray(new String[mFileList.size()]),
                        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mChosenFile = mFileList.get((which));
                Log.i(LOG_TAG, "Selected: " + mChosenFile);

                if (FileIsImage() && mLoadType == LoadType.BLUEPRINT) {
                    Log.i(LOG_TAG, "You have selected an image.");
                    readImageData();
                } else if (FileIsData() && mLoadType == LoadType.TRAJECTORY) {
                    Log.i(LOG_TAG, "You have selected a data file.");
                    readTrajData();
                } else if (FileIsDir()) {
                    Log.i(LOG_TAG, "Selected a directory");
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
        });

        dialog = builder.show();
        if (mLoadType == LoadType.BLUEPRINT) {
            dialog.setTitle("Select a blueprint file");
        } else if (mLoadType == LoadType.TRAJECTORY) {
            dialog.setTitle("Select a trajectory data file");
        }
        return dialog;
    }

    public void LoadAlignment() {
        PrintNotYetImplemented("LoadAlignment");
    }

    public void SaveAlignment() {
        PrintNotYetImplemented("SaveAlignment");
    }

    public void SelectTrajectory(View view) {
        Dialog dialog = createFileSelectorDialog(Environment.getExternalStorageDirectory() + "/", LoadType.TRAJECTORY);
        dialog.show();
    }

    public void SelectBlueprint(View view) {
        Dialog dialog = createFileSelectorDialog(Environment.getExternalStorageDirectory() + "/", LoadType.BLUEPRINT);
        dialog.show();
    }
}
