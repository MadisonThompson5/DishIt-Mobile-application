package com.clarifai.android.starter.api.v2.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.clarifai.android.starter.api.v2.R;
import com.clarifai.android.starter.api.v2.service.GoogleApiReceiver;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.json.JSONException;
//import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import clarifai2.dto.prediction.Concept;
import rx.functions.Action1;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;

/**
 * A common class to set up boilerplate logic for
 */
public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {

  private static final String INTENT_EXTRA_DRAWER_POSITION = "IntentExtraDrawerPosition";

  @BindView(R.id.content_root) protected View root;
  private String TAG = "BaseActivity";
  private String apiKeyUSDA = "cx1fM06Z6s7RK8iMOiFl2nsw9pkDSnG54OtAuEe7";
  private String apiYelp = "9Xfu6IHr38vID5QOuTw0eviQFHjc_F4nr9tg-WYDksOAcECrSbmhw4GyInQKbxUgk0A4YrhTogCRSS6eA8ShwjOClcVI-kC4tT7S6gDNp8UrHjqG3dFv6S4XYs2MWnYx";
  private String nutritionixAppID = "a3f8a39f";
  private String nutritionixAppKey = "d8fbcb00e51e12e332824467175de316";
  private Unbinder unbinder;

  public static List<Concept> concepts = new ArrayList<>();
  private String httpResponse = "";
  private double mealCalories = 0;

  private static final int RESULT_PERMS_INITIAL=1339;
  private GoogleApiReceiver googleApiReceiver;
  private static final String[] PERMISSIONS={
          ACCESS_FINE_LOCATION,
          ACCESS_COARSE_LOCATION,
          INTERNET
  };

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      RxPermissions.getInstance(this)
          .request(Manifest.permission.READ_EXTERNAL_STORAGE)
          .subscribe(new Action1<Boolean>() {
            @Override public void call(Boolean granted) {
              if (!granted) {
                new AlertDialog.Builder(BaseActivity.this)
                    .setCancelable(false)
                    .setMessage(R.string.error_external_storage_permission_not_granted)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                      @Override public void onClick(DialogInterface dialog, int which) {
                        moveTaskToBack(true);
                        finish();
                      }
                    })
                    .show();
              }
            }
          });
    }

    @SuppressLint("InflateParams") final View wrapper = getLayoutInflater().inflate(R.layout.activity_wrapper, null);
    final ViewStub stub = ButterKnife.findById(wrapper, R.id.content_stub);
    stub.setLayoutResource(layoutRes());
    stub.inflate();
    setContentView(wrapper);
    unbinder = ButterKnife.bind(this);

    final Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);

    final Drawer drawer = new DrawerBuilder()
        .withActivity(this)
        .withToolbar(toolbar)
        .withDrawerItems(drawerItems())
        .build();

    // Show the "hamburger"
    setSupportActionBar(toolbar);
    final ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(false);
    }
    drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);

    // Set the selected index to what the intent said we're in
    drawer.setSelectionAtPosition(getIntent().getIntExtra(INTENT_EXTRA_DRAWER_POSITION, 0));

    init();
  }

  @Override
  protected void onDestroy() {
    unbinder.unbind();
    super.onDestroy();

    if(googleApiReceiver != null)
      googleApiReceiver.release();
  }

  @NonNull
  protected List<IDrawerItem> drawerItems() {
    return Arrays.<IDrawerItem>asList(
        new PrimaryDrawerItem()
            .withName(R.string.drawer_item_recognize_tags)
            .withOnDrawerItemClickListener(goToActivityListener(RecognizeConceptsActivity.class))
    );
  }

  /**
   * @return the layout file to use. This is used in place of {@code R.id.content_stub} in the activity_wrapper.xml
   * file, by using a {@link ViewStub}.
   */
  @LayoutRes
  protected abstract int layoutRes();

  private Drawer.OnDrawerItemClickListener goToActivityListener(
      @NonNull final Class<? extends Activity> activityClass) {
    return new Drawer.OnDrawerItemClickListener() {
      @Override
      public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        // Don't open a new activity if we're already in the activity the user clicked on
        if (!drawerItem.isSelected()) {
          startActivity(new Intent(BaseActivity.this, activityClass).putExtra(INTENT_EXTRA_DRAWER_POSITION, position));
        }
        return true;
      }
    };
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.btnFoodSearch:
        requestForFoodSearch();
        break;
      case R.id.btnFoodInfo:
        requestForFoodInfo();
        break;
      case R.id.NutriAPI:
        nutritionixHttpRequest("one cup mashed potatoes");
        //nutritionixLocationRequest("33.645790,-117.842769", "2");
        //nutritionixMealRequest("panda", "513fbc1283aa2dc80c00002e");
        break;
      case R.id.btnFirebaseDB:
        requestForFireBaseDB();
        break;
      case R.id.YelpAPI:
        yelpHttpRequest();
        break;

    }
  }

  public void init() {
    findViewById(R.id.btnFoodSearch).setOnClickListener(this);
    findViewById(R.id.btnFoodInfo).setOnClickListener(this);
    findViewById(R.id.btnFirebaseDB).setOnClickListener(this);
    findViewById(R.id.YelpAPI).setOnClickListener(this);
    findViewById(R.id.NutriAPI).setOnClickListener(this);



//    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
//      requestPermissions(); // Android 6.0 + (runtime permission)
//    else
//      startGoogleApi();
  }

  public void startGoogleApi() {

    googleApiReceiver = new GoogleApiReceiver(this);

  }

  public void requestForFoodSearch() {
    String foodName = "pasta";
    String url ="https://api.nal.usda.gov/ndb/search/?format=json&q="+foodName+"&sort=n&max=25&offset=0&api_key="+apiKeyUSDA;

    httpRequest(url);
  }

  public void requestForFoodInfo() {

    String ndbno = "45032695";
    String url ="https://api.nal.usda.gov/ndb/reports/?ndbno="+ndbno+"&type=b&format=json&api_key="+apiKeyUSDA;

    httpRequest(url);
  }

  public void requestForFireBaseDB() {
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
            .child("CS125");

    Map<String, Object> hopperUpdates = new HashMap<>();

    hopperUpdates.put("test",
            new Indexing(
                    "HelloWorld"
            )
    );

    databaseReference.updateChildren(hopperUpdates, new DatabaseReference.CompletionListener() {
      @Override
      public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
        if (databaseError != null) {
          Log.e(TAG, "Data could not be saved " + databaseError.getMessage());
        } else {
          Log.e(TAG, "Data saved successfully.");
        }
      }
    });
  }

  public void httpRequest(String url){
    RequestQueue queue = Volley.newRequestQueue(this);

    // Request a string response from the provided URL.
    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
              @Override
              public void onResponse(String response) {
                // Display the first 500 characters of the response string
                String getResponse = response;
                Log.e(TAG, "getResponse" + getResponse);
              }
            }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        error.printStackTrace();
        Log.e(TAG, "Error");
      }
    });

    // Add the request to the RequestQueue.
    queue.add(stringRequest);
  }
 
  public void yelpHttpRequest(){
    //yelp API call
    String url = "https://api.yelp.com/v3/businesses/search?term=food&location=boston";
    RequestQueue queue = Volley.newRequestQueue(this);

    // Request a string response from the provided URL.
    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
              @Override
              public void onResponse(String response) {
                // Display the first 500 characters of the response string.
                writeToFile("yelp_response.json", response);
                String getResponse = response;
                Log.e(TAG, "getResponse" + getResponse);
              }},
            new Response.ErrorListener() {
              @Override
              public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e(TAG, "Error");
              }})

    {
      @Override
      public Map<String, String> getHeaders () throws AuthFailureError {
        Map<String, String> params = new HashMap<String, String>();
        params.put("Authorization", "Bearer "+apiYelp);
        return params;
      }
    };

    // Add the request to the RequestQueue.
    queue.add(stringRequest);
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
                //calculateCalories();
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

  public void nutritionixLocationRequest(String coordinates, String distance){
    String url = "https://trackapi.nutritionix.com/v2/locations?";
    url += "ll=" + coordinates + "&";
    url += "distance=" + distance + "mi";

    RequestQueue queue = Volley.newRequestQueue(this);

    // Request a string response from the provided URL.
    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
              @Override
              public void onResponse(String response) {
                // Display the first 500 characters of the response string.
                writeToFile("nutritionix_response.json", response);
                String getResponse = response;
                Log.e(TAG, "getResponse" + getResponse);
              }},
            new Response.ErrorListener() {
              @Override
              public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e(TAG, "Error");
              }})

    {
      @Override
      public Map<String, String> getHeaders () throws AuthFailureError {
        Map<String, String> params = new HashMap<String, String>();
        params.put("x-app-id", nutritionixAppID);
        params.put("x-app-key", nutritionixAppKey);
        return params;
      }
    };

    // Add the request to the RequestQueue.
    queue.add(stringRequest);
  }

  public void nutritionixMealRequest(String query, String brandID){
    String url = "https://trackapi.nutritionix.com/v2/search/instant?";
    url += "query=" + query + "&";
    url += "brand_ids=" + brandID;

    RequestQueue queue = Volley.newRequestQueue(this);

    // Request a string response from the provided URL.
    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
              @Override
              public void onResponse(String response) {
                // Display the first 500 characters of the response string.
                writeToFile("nutritionix_response.json", response);
                String getResponse = response;
                Log.e(TAG, "getResponse" + getResponse);
              }},
            new Response.ErrorListener() {
              @Override
              public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e(TAG, "Error");
              }})

    {
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

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    // TODO
    int[] results = grantResults;

    if (requestCode==RESULT_PERMS_INITIAL) {
      boolean isApprovedAll = true;
      for(int i = 0; i < results.length; i++) {
        if(results[i] == -1) {
          // popup - please allow all permissions
          isApprovedAll = false;
          final AlertDialog.Builder builder = new AlertDialog.Builder(this);
          builder.setTitle("System will start when you allow all permissions.");
          builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              requestPermissions();
            }
          });
          builder.show();
          return;
        }
      }

      if(isApprovedAll) {
        startGoogleApi();
      }
    }
  }


  public void requestPermissions() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
      List<String> permission_list = new ArrayList<>();

      for(int i = 0; i < PERMISSIONS.length; i++) {
        if(ContextCompat.checkSelfPermission(this,PERMISSIONS[i]) == -1)
          permission_list.add(PERMISSIONS[i]);
      }

      if(permission_list.size() == 0) {
        startGoogleApi();
      } else {
        String[] NEW_PERMISSIONS = new String[permission_list.size()];
        for(int i = 0; i < permission_list.size(); i ++)
          NEW_PERMISSIONS[i] = permission_list.get(i);

        requestPermissions(NEW_PERMISSIONS, RESULT_PERMS_INITIAL);
      }
    }
  }

  private void writeToFile(String file_name, String data) {
    try {
      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(file_name, Context.MODE_PRIVATE));
      outputStreamWriter.write(data);
      outputStreamWriter.close();
    }
    catch (IOException e) {
      Log.e("Exception", "File write failed: " + e.toString());
    }
  }
}
