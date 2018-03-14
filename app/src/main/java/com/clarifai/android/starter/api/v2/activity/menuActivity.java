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
                switch(v.getId()) {
                    case R.id.chkAmerican:
                    final CheckBox checkBox = (CheckBox) findViewById(R.id.chkAmerican);
                    if (checkBox.isChecked()) {
                        preferences.add("Amercian");
                    }
                    case R.id.chkItalian:
                        final CheckBox checkBox1 = (CheckBox) findViewById(R.id.chkAmerican);
                        if (checkBox1.isChecked()) {
                            preferences.add("Italian");
                        }



                }

                Log.e(TAG, "preferences " + String.valueOf(preferences));

//                EditText Fav = (EditText) findViewById(R.id.Favorites);
//                @Override
//                public boolean onEditorAction(TextView Fav,int i ,  KeyEvent keyevnet)
//
//                android.widget.TextView Fav = (android.widget.TextView) findViewById(R.id.Favorites);
//
//                String input = Fav.getText().toString();
//                String favs[] =input.split(",");
//                for(int i = 0; i < favs.length; ++i) {
//                    favorites.add(favs[i]);
//                }
//                android.widget.TextView Dis = (android.widget.TextView) findViewById(R.id.Favorites);
//                String input1 = Fav.getText().toString();
//                String dis[] =input1.split(",");
//                for(int i = 0; i < dis.length; ++i) {
//                        dislikes.add(dis[i]);
//                    }


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
