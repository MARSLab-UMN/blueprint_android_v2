package edu.umn.mars.blueprintandroidapp;

import android.content.Context;
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
import java.io.InputStreamReader;


public class MainActivity extends ActionBarActivity {

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

    public void LoadAlignment() {
        PrintNotYetImplemented("LoadAlignment");
    }

    public void SaveAlignment() {
        PrintNotYetImplemented("SaveAlignment");
    }

    public void SelectTrajectory(View view) {
        PrintNotYetImplemented("SelectTrajectory");

        // write on SD card file data in the text box
        try {
            File myFile = new File("/sdcard/test.txt");
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
                    "Done reading SD 'test.txt'",
                    Toast.LENGTH_SHORT).show();
            Toast.makeText(getBaseContext(),
                    aBuffer,
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void SelectBlueprint(View view) {
        PrintNotYetImplemented("SelectBlueprint");
    }
}
