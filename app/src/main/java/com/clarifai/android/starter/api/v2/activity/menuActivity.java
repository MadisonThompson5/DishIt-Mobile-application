package com.clarifai.android.starter.api.v2.activity;

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


import java.util.ArrayList;


import com.clarifai.android.starter.api.v2.R;

public class menuActivity extends AppCompatActivity {
    //preferences
    ArrayList<String> preferences = new ArrayList<String>();
    ArrayList<String> favorites = new ArrayList<String>();
    ArrayList<String> dislikes = new ArrayList<String>();
    private String TAG = "Preferences";



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

        Button save=(Button)findViewById(R.id.okProfile);

        save.setOnClickListener(new View.OnClickListener()

            {
                //what happens when button is clicked
                @Override
                public void onClick (View v){
                //Log.e(TAG, "button clicked!");
                //for each CheckBox, check if it was checked, then add to prefernces
                    final CheckBox checkBox = (CheckBox) findViewById(R.id.chkAmerican);

                    if (checkBox.isChecked()&&   !preferences.contains("American")) {
                        preferences.add("Amercian");
                    }
                    final CheckBox checkBox1 = (CheckBox) findViewById(R.id.chkItalian);
                    if (checkBox1.isChecked() &&!preferences.contains("Italian")) {
                            preferences.add("Italian");
                        }
                    final CheckBox checkBox2 = (CheckBox) findViewById(R.id.chkChinese);
                    if (checkBox2.isChecked() && !preferences.contains("Chinese")) {
                        preferences.add("Chinese");
                    }
                    final CheckBox checkBox3 = (CheckBox) findViewById(R.id.chkMexican);
                    if (checkBox3.isChecked()&& !preferences.contains("Mexican")) {
                        preferences.add("Mexican");
                    }
                    final CheckBox checkBox4 = (CheckBox) findViewById(R.id.chkAsian);
                    if (checkBox4.isChecked()&& !preferences.contains("Asian")) {
                        preferences.add("Asian");
                    }
                    final CheckBox checkBox5 = (CheckBox) findViewById(R.id.chkBrunch);
                    if (checkBox5.isChecked()&& !preferences.contains("Brunch")) {
                        preferences.add("Brunch");
                    }
                    final CheckBox checkBox6 = (CheckBox) findViewById(R.id.chkFrench);
                    if (checkBox6.isChecked()&& !preferences.contains("French")) {
                        preferences.add("French");
                    }
                    final CheckBox checkBox7 = (CheckBox) findViewById(R.id.chkSteakhouse);
                    if (checkBox7.isChecked()&& !preferences.contains("Steakhouse")) {
                        preferences.add("Steakhouse");
                    }
                    final CheckBox checkBox8 = (CheckBox) findViewById(R.id.chkBurgers);
                    if (checkBox8.isChecked()&& !preferences.contains("Burgers")) {
                        preferences.add("Burgers");
                    }
                    final CheckBox checkBox9 = (CheckBox) findViewById(R.id.chkPizza);
                    if (checkBox9.isChecked()&& !preferences.contains("Pizza")) {
                        preferences.add("Pizza");
                    }
                    final CheckBox checkBox10 = (CheckBox) findViewById(R.id.chkOrganic);
                    if (checkBox10.isChecked()&& !preferences.contains("Organic")) {
                        preferences.add("Organic");
                    }
                    final CheckBox checkBox11 = (CheckBox) findViewById(R.id.chkVeg);
                    if (checkBox11.isChecked()&& !preferences.contains("Vegetarian/Vegan")) {
                        preferences.add("Vegetarian/Vegan");
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






}
