package com.clarifai.android.starter.api.v2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class GetMealCalories extends AppCompatActivity {

    private String TAG = "GetMealCalories";
    private ArrayList<String> concepts = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_meal_calories);

        Intent i = getIntent();
        concepts = i.getStringArrayListExtra("concepts");
        Log.e(TAG, concepts.get(0));
    }
}
