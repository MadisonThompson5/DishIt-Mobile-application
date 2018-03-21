package com.clarifai.android.starter.api.v2.activity;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toolbar;
import android.widget.TextView;
import android.widget.EditText;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import com.clarifai.android.starter.api.v2.R;
import com.google.gson.Gson;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static java.util.Arrays.asList;

public class menuActivity extends AppCompatActivity {
    //preferences
    ArrayList<String> preferences = new ArrayList<String>();
    ArrayList<String> favorites = new ArrayList<String>();
    ArrayList<String> dislikes = new ArrayList<String>();
    private String TAG = "Preferences";

    //checklist
    private List<Integer> checkList = asList(R.id.chkAmerican, R.id.chkAsian, R.id.chkBrunch, R.id.chkBurgers, R.id.chkChinese, R.id.chkFrench, R.id.chkItalian, R.id.chkMexican, R.id.chkOrganic, R.id.chkPizza, R.id.chkSteakhouse, R.id.chkVeg);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        android.support.v7.widget.Toolbar toolbar  = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //create profile if not exist
        if(!fileExists(this, "test_profile"))
            createProfile("Test Profile", "Male", 170, false);

        //load profile
        JSONObject jo = readJsonFile();
        String name = jo.get("name").toString();
        Double weight = Double.valueOf(jo.get("weight").toString());
        String gender = jo.get("gender").toString();
        String savedPref = jo.get("preferences").toString();
        for(int id : checkList) {
            CheckBox checkBox = (CheckBox) findViewById(id);
            if(savedPref.contains(checkBox.getText().toString()))
                checkBox.setChecked(true);
        }

        String textStr = "Name: " + name + "\nGender: " + gender + "\nWeight: " + weight;

        TextView txtView = (TextView)findViewById(R.id.profile);
        txtView.setText(textStr);
        txtView.setTextSize(18);

        //save preferences
        Button save=(Button)findViewById(R.id.okProfile);
        save.setOnClickListener(new View.OnClickListener()

            {
                //what happens when button is clicked
                @Override
                public void onClick (View v){
                //Log.e(TAG, "button clicked!");
                //for each CheckBox, check if it was checked, then add to prefernces
                    for (int id : checkList) {
                        CheckBox checkBox = (CheckBox) findViewById(id);
                        if(checkBox.isChecked() && !preferences.contains(checkBox.getText().toString()))
                            preferences.add(checkBox.getText().toString());
                    }

                    Log.e(TAG, "preferences " + String.valueOf(preferences));

                    EditText edit = (EditText)findViewById(R.id.Favs);
                    EditText edit1 = (EditText)findViewById(R.id.Dis);

                    String result = edit.getText().toString();
                    result.split(",");
                    favorites.add(result);

                    String result1 = edit1.getText().toString();
                    result1.split(",");
                    dislikes.add(result1);

                    Log.e(TAG, "favorites " + String.valueOf(favorites));
                    Log.e(TAG, "dislikes " + String.valueOf(dislikes));

                    createProfile("Test Profile", "Male", 170, true);

                }


            });
        Log.e(TAG, "preferences " + String.valueOf(preferences));
        Log.e(TAG, "favorites " + String.valueOf(favorites));
        Log.e(TAG, "dislikes " + String.valueOf(dislikes));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void createProfile(String name, String gender, double weight, boolean addList) {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("gender", gender);
        obj.put("weight", weight);

        if(addList) {
            //add preferences, favorites, and dislikes
            obj.put("preferences", preferences);
            obj.put("favorites", favorites);
            obj.put("dislikes", dislikes);
        }
        else {
            //initialize preferences, favorites, and dislikes
            obj.put("preferences", "[]");
            obj.put("favorites", "[]");
            obj.put("dislikes", "[]");
        }

        writeToFile("test_profile", obj.toString(), false);
    }

    public JSONObject readJsonFile(){
        Context context = this;
        String path = context.getFilesDir().getPath();
        //Read text from file
        StringBuilder text = new StringBuilder();

        File file = new File(path+"/test_profile");
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //do something
        }
        Log.e(TAG, text.toString());

        JSONObject jo = new JSONObject();
        JSONParser parser = new JSONParser();
        try {
            jo = (JSONObject)parser.parse(text.toString());
        }
        catch(ParseException e) {
            e.printStackTrace();
            Log.e(TAG, "ParseException");
        }
        return jo;
    }

    public void writeToFile(String file_name, String data, boolean append) {
        int mode;
        if(append)
            mode = Context.MODE_APPEND;
        else
            mode = Context.MODE_PRIVATE;

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(file_name, mode));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if(file == null || !file.exists()) {
            return false;
        }
        return true;
    }
}
