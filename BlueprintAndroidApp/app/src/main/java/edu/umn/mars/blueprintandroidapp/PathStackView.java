package edu.umn.mars.blueprintandroidapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PathStackView extends AppCompatActivity {

    //for file selector dialog
    static final public String DEBUG_TAG = "BlueprintAndroidApp";
    private ArrayList<String> mFileList = new ArrayList<String>();
    private String mChosenFile;
    private String mCurrentDir;
    private static final String DATATYPE = ".txt";

    //parameter values
    private String paramDataPath;
    private int paramNumImgs;
    private int paramImgSubsamp;
    private int paramStartStairs;
    private int paramStartRot;

    //UI elements
    Button btnMapPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_stack_view);

        btnMapPath = (Button) findViewById(R.id.map_path_button);

        btnMapPath.setVisibility(View.INVISIBLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_path_stack_view, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //open a dialog to load the parameters file
    public void LoadParameterFile(View view) {
        createFileSelectorDialog(Environment.getExternalStorageDirectory() + "/");
        //set parameter values
        //make mapPath button visible
    }

    //finish activity and return to previous screen
    public void GoToPreviousScreen(View view) {
        finish();
    }

    //map out image path with dijikstra's algorithm
    public void MapPath(View view) {
        DijkstraThread newThread = new DijkstraThread(paramDataPath,
                paramNumImgs,
                paramImgSubsamp,
                paramStartStairs,
                paramStartRot,
                MainActivity.blueprint_data);
        newThread.start();
    }










    protected Dialog createFileSelectorDialog(String baseFolderPath) {
        mCurrentDir = baseFolderPath;
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
                                createFileSelectorDialog(mCurrentDir);
                            } else {
                                createFileSelectorDialog(up_a_level);
                            }
                        } else {
                            mChosenFile = mFileList.get(which);
                            Log.i(DEBUG_TAG, "Selected: " + mChosenFile);

                            if (FileIsData()) {
                                Log.i(DEBUG_TAG, "You have selected a parameter data file.");
                                return;
                            } else if (FileIsDir()) {
                                Log.i(DEBUG_TAG, "Selected a directory");
                                dialog.dismiss();
                                createFileSelectorDialog(mCurrentDir + mChosenFile + "/");
                            } else {
                                Context context = getApplicationContext();
                                CharSequence text = "Invalid file selection. Select another.";
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                                createFileSelectorDialog(mCurrentDir);
                            }
                        }
                    }
                });

        dialog = builder.show();
        dialog.setTitle("Select a parameter file");
        return dialog;
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


    private boolean FileIsDir() {
        File sel = new File(mCurrentDir, mChosenFile);
        return sel.isDirectory();
    }


    private boolean FileIsData() {
        return mChosenFile.toLowerCase().contains(DATATYPE) && !FileIsDir();
    }

}
