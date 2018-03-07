package com.clarifai.android.starter.api.v2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetMealCalories extends AppCompatActivity {

    private String TAG = "GetMealCalories";
    private String nutritionixAppID = "a3f8a39f";
    private String nutritionixAppKey = "d8fbcb00e51e12e332824467175de316";

    private ArrayList<String> concepts = new ArrayList<String>();
    private List<CheckBox> checkBoxes = new ArrayList<CheckBox>();

    private double mealCalories = 0;
    private int count = 0;
    private String httpResponse = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_meal_calories);

        ScrollView sv = new ScrollView(this);
        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        sv.addView(ll);

        concepts = getIntent().getStringArrayListExtra("concepts");
        Log.e(TAG, concepts.toString());

        TextView tv = new TextView(this);
        tv.setText("Choose the items that are within the meal");
        ll.addView(tv);

        for(int i = 0; i < concepts.size(); ++i) {
            CheckBox cb = new CheckBox(getApplicationContext());
            cb.setText(concepts.get(i));
            checkBoxes.add(cb);
            ll.addView(cb);
        }

        Button b = new Button(this);
        b.setText("Next");
        b.setBottom(10);
        ll.addView(b);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "button clicked!");
                mealCalories = 0; //reset meal count
                for(int i = 0; i < checkBoxes.size(); ++i) {
                    CheckBox cb = checkBoxes.get(i);
                    if(cb.isChecked()) {
                        nutritionixHttpRequest(cb.getText().toString());
                        ++count;
                    }
                }
            }
        });

        this.setContentView(sv);
    }

    public void calculateCalories() {
        JSONParser parser = new JSONParser();
        double cal = 0;

        try {
            JSONObject jo = (JSONObject) parser.parse(httpResponse);
            JSONArray foods = (JSONArray)jo.get("foods");

            for(Object food : foods) {
                JSONObject jsonFood = (JSONObject)food;
                cal = Double.valueOf(jsonFood.get("nf_calories").toString());
            }

            Log.e(TAG, "calories: " + String.valueOf(cal));
        }
        catch(ParseException e) {
            e.printStackTrace();
            Log.e(TAG, "ParseException");
        }

        mealCalories += cal;
        if(--count > 0)
            Log.e(TAG, "current meal total: " + String.valueOf(mealCalories));
        else {
            Log.e(TAG, "final meal total: " + String.valueOf(mealCalories));
            Intent intent = new Intent();
            intent.putExtra("mealCount", mealCalories);
            setResult(RESULT_OK, intent);
            finish(); //end activity
        }

    }

    public void nutritionixHttpRequest(final String query){
        String url = "https://trackapi.nutritionix.com/v2/natural/nutrients";
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        //writeToFile("nutritionix_response.json", response);
                        httpResponse = response;
                        calculateCalories();
                        Log.e(TAG, "getResponse" + response);
                    }},
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.e(TAG, "Error");
                    }})

        {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("query",query);

                return params;
            }

            @Override
            public Map<String, String> getHeaders () throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-app-id", nutritionixAppID);
                params.put("x-app-key", nutritionixAppKey);
                params.put("x-remote-user-id", "0");
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
