package com.ohanacodedev.ocurlpinger;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN_ACT";

    private boolean serviceStarted = false;
    private ListView urlList;
    private List<String> urlStringList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton addUrl = findViewById(R.id.add_url_btn);
        addUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getUserDefinedUrl();
            }
        });

        urlList = findViewById(R.id.url_list_widget);
        urlStringList = new ArrayList<>();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, urlStringList );
        urlList.setAdapter(arrayAdapter);
        // register onClickListener to handle click events on each item
        urlList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v,int position, long arg3)
            {
                changeTableEntry(position);
            }
        });

        getSavedSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.activate).setChecked(serviceStarted);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.activate) {
            serviceStarted = !item.isChecked();
            item.setChecked(serviceStarted);
            saveSettings();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void changeTableEntry(int id){
        final int selectedId = id;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String entry = urlStringList.get(id);

        builder.setTitle("Edit url");

        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);
        input.setText(entry, TextView.BufferType.EDITABLE);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String usrText = input.getText().toString();

                if(usrText.length() > 8){
                    urlStringList.set(selectedId, usrText);
                }else{
                    urlStringList.remove(selectedId);
                }

                saveSettings();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private void getUserDefinedUrl(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input a new url");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                urlStringList.add(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void getSavedSettings(){
        try {
            Context context = getApplicationContext();

            SharedPreferences prefs= context.getSharedPreferences("com.ohanacodedev.ocurlpinger", Context.MODE_PRIVATE);
            serviceStarted = prefs.getBoolean("persistent", false);

            urlStringList.clear();
            for (String item: prefs.getString("urllist", "").split("~|~")) {
                urlStringList.add(item);
            }

            AlarmSchedule backgroundService = new AlarmSchedule();
            backgroundService.cancelAlarm(context);
            if(serviceStarted) {
                backgroundService.setAlarm(context);
            }

        } catch (NullPointerException e) {
            Log.e(TAG, "error reading preferences: " + e.getMessage());
        }
    }

    private void saveSettings(){
        try {

            Context context = getApplicationContext();

            StringBuilder savedUrlList = new StringBuilder();

            for (String item: urlStringList) {
                savedUrlList.append(item + "~|~");
            }

            SharedPreferences prefs= context.getSharedPreferences("com.ohanacodedev.ocurlpinger", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("persistent", serviceStarted);
            editor.putString("urllist", savedUrlList.toString());
            editor.apply();

            AlarmSchedule backgroundService = new AlarmSchedule();
            backgroundService.cancelAlarm(context);
            String msg = "Service disabled.";
            if(serviceStarted) {
                backgroundService.setAlarm(context);
                msg = "Service started.";
            }

            View view = findViewById(R.id.main_layout);
            Snackbar.make( view, msg, Snackbar.LENGTH_LONG).setAction("Starting service.", null).show();

        } catch (NullPointerException e) {
            Log.e(TAG, "error reading preferences: " + e.getMessage());
        }
    }
}
