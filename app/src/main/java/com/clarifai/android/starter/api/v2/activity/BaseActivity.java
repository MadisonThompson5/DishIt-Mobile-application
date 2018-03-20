package com.clarifai.android.starter.api.v2.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

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

import clarifai2.dto.prediction.Concept;
import rx.functions.Action1;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.SYSTEM_ALERT_WINDOW;

import com.clarifai.android.starter.api.v2.Food; //import custom Food class
import com.clarifai.android.starter.api.v2.Restaurant; //import custom Restaurant class

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * A common class to set up boilerplate logic for
 */
public abstract class BaseActivity extends AppCompatActivity implements LocationListener {

  private static final String INTENT_EXTRA_DRAWER_POSITION = "IntentExtraDrawerPosition";

  @BindView(R.id.content_root) protected View root;
  private String TAG = "BaseActivity";
  private String apiKeyUSDA = "cx1fM06Z6s7RK8iMOiFl2nsw9pkDSnG54OtAuEe7";
  private String apiYelp = "9Xfu6IHr38vID5QOuTw0eviQFHjc_F4nr9tg-WYDksOAcECrSbmhw4GyInQKbxUgk0A4YrhTogCRSS6eA8ShwjOClcVI-kC4tT7S6gDNp8UrHjqG3dFv6S4XYs2MWnYx";
  private String nutritionixAppID = "a3f8a39f";
  private String nutritionixAppKey = "d8fbcb00e51e12e332824467175de316";
  private Unbinder unbinder;

  public static double calorieLimit = 2000;
  public static double mealCalories = 0;   //total calories of a meal
  public String httpResponse = ""; //response from httpRequests

  private static TextView textView;

  private SwipeRefreshLayout mySwipeRefreshLayout;

  //Food items
  public ArrayList<Food> foods = new ArrayList<Food>();
  public ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
  private int restaurantCount = 0;
  public ArrayList<Restaurant> yelpBusinesses = new ArrayList<Restaurant>();
  private ArrayList<String> food_preferences = new ArrayList<String>();
  //private ListView mDrawerList;
  //private ArrayAdapter<String> mAdapter; //may change
  ;

  //gps location
  protected LocationManager locationManager;
  protected LocationListener locationListener;
  protected Context context;
  private double l_lat, l_lon;
  private boolean gps_enabled, network_enabled;

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

    //Draw Side bar
    //mDrawerList = (ListView)findViewById(R.id.navigatorList);

    //gps location
    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    requestLocation();

    getLocation();
    textView = (TextView)findViewById(R.id.calorieView);
    textView.setText("Calorie Limit: " + String.valueOf(calorieLimit));
    //nutritionixLocationRequest(String.valueOf(l_lat), String.valueOf(l_lon), "2");


    mySwipeRefreshLayout = (SwipeRefreshLayout)this.findViewById(R.id.swiperefresh);
    mySwipeRefreshLayout.setOnRefreshListener(
            new SwipeRefreshLayout.OnRefreshListener() {
              @Override
              public void onRefresh() {
                nutritionixLocationRequest(String.valueOf(l_lat), String.valueOf(l_lon), "2");
              }
            }
    );
  }

  @Override
  protected void onDestroy() {
    unbinder.unbind();
    super.onDestroy();

    if(googleApiReceiver != null)
      googleApiReceiver.release();
  }



  //GPS section start*********************************
  @Override
  public void onLocationChanged(Location location)
  //Each time the location changed, do something here
  {
    l_lat = location.getLatitude();
    l_lon = location.getLongitude();
    System.out.print("On location change: ");
    System.out.println("Lat: " + Double.valueOf(l_lat).toString() + ", Lon: " + Double.valueOf(l_lon).toString() );

  }

  @Override
  public void onProviderDisabled(String provider){
    Log.e("GPS", "disable");
  }
  @Override
  public void onProviderEnabled(String provider){
    Log.e("GPS", "enable");
  }
  @Override
  public  void onStatusChanged(String provider, int status, Bundle extras){
    Log.e("GPS", "status changed" );
  }

  public void requestLocation()
  {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED  ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED){
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0L,0.0F,this );
    }

  }



  //GPS section end*************************************

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

  public void getLocation()
  {
    System.out.println("There is something =====================================");
//
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED  ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED   )
    {
      System.out.println("permission checked");
//      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0L,0.0F,this );

      Location l =locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
      l_lat = l.getLatitude();
      l_lon = l.getLongitude();
      System.out.println("Got location");

      System.out.println("Lat: " + Double.valueOf(l_lat).toString() + ", Lon: " + Double.valueOf(l_lon).toString() );
    }
    else
    {
      System.out.println("permission failed");

    }
  }

  public void startGoogleApi() {

    googleApiReceiver = new GoogleApiReceiver(this);

  }

  public static void UpdateActivity() {
    textView.setText("Calorie Limit: " + String.valueOf(calorieLimit - mealCalories));
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

  /*
  To get recommendations, all that needs to be called is nutritionixLocationRequest.
  In the program, the order of function calls is:

  nutritionixLocationRequest -> parseNutritionixLocations -> linkToYelp -> parseYelpResponse ->
  getMealItems -> nutritionixMealRequest -> parseNutritionixMeal

  To get all meal items, nutritionixLocationRequest is called first. After the response is parsed,
  a request is sent to Yelp to get more info on the restaurants. Finally, once all the restaurant
  info is gathered, nutritionixMealRequest is called on every restaurant to create a final list
  containing all the meal items from those restaurants.

  After parseNutritonixMeal is complete, it then creates its recommendations and sends them to the app
  */

  public boolean checkRestaurantList(String n) {
    //check if a restaurant is already in the restaurant list
    for (Restaurant ret : restaurants) {
      if(ret.name.equals(n))
        return true;
    }
    return false;
  }

  public void nutritionixLocationRequest(final String lat, final String lon, String distance){
    String url = "https://trackapi.nutritionix.com/v2/locations?";
    url += "ll=" + lat + "," + lon + "&";
    url += "distance=" + distance + "mi";

    RequestQueue queue = Volley.newRequestQueue(this);

    // Request a string response from the provided URL.
    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
              @Override
              public void onResponse(String response) {
                // Display the first 500 characters of the response string.
                httpResponse = response;
                String getResponse = response;

                System.out.println("nutritionixLocationRequest\n");
                Log.e(TAG, "location getResponse" + getResponse);
                parseNutritionixLocations(lat, lon);
              }},
            new Response.ErrorListener() {
              @Override
              public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e(TAG, "Error: nutritionixLocationRequest");
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

  public void parseNutritionixLocations(String lat, String lon) {
    //parses Nutritionix location response for name, brand_id, and distance_km
    JSONParser parser = new JSONParser();
    try {
      JSONObject jo = (JSONObject) parser.parse(httpResponse);

      //begin parsing JSON response to Nutritionix location search
      JSONArray locations = (JSONArray)jo.get("locations");

      for(Object location : locations) {
        JSONObject jsonLocation = (JSONObject)location;
        Restaurant restaurantItem = new Restaurant();
        restaurantItem.name = to_string(jsonLocation, "name");

        //ignore restaurant if already in list to avoid duplicates
        if(!checkRestaurantList(restaurantItem.name)) {
          restaurantItem.brand_id = to_string(jsonLocation, "brand_id");
          restaurantItem.distanceFromUser = to_double(to_string(jsonLocation, "distance_km")) * 1000;
          restaurantItem.phoneNumber = to_string(jsonLocation, "phone");
          restaurantItem.phoneDisplay = restaurantItem.phoneNumber;
          Log.e(TAG, restaurantItem.name + " " + restaurantItem.brand_id + " " + restaurantItem.distanceFromUser);
          restaurants.add(restaurantItem);
        }
      }
      System.out.println("End of Nutritionix");

      linkToYelp(lat, lon);
    }
    catch(ParseException e) {
      e.printStackTrace();
      Log.e(TAG, "ParseException");
    }
  }

  public void linkToYelp(String lat, String lon) {
    //iterate through restaurant list and get Yelp info
    yelpHttpRequest(lat, lon);
  }

  //Huu modified
  public void yelpHttpRequest( String lat, String lon){
    String url = "https://api.yelp.com/v3/businesses/search?sort_by=distance&limit=50&";
    url += "radius="+ String.valueOf(2 * 1610);
    url += "&categories=food,restaurants";
    url += "&latitude="+lat+"&longitude="+lon;

    RequestQueue queue = Volley.newRequestQueue(this);

    // Request a string response from the provided URL.
    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
              @Override
              public void onResponse(String response) {
                // Display the first 500 characters of the response string.
                httpResponse = response;
                String getResponse = response;
                Log.e(TAG, "yelp getResponse" + getResponse);
                parseYelpResponse();
              }},
            new Response.ErrorListener() {
              @Override
              public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e(TAG, "Error YelpHttpRequest");
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

  //Huu modified
  public void parseYelpResponse() {
    //parses Yelp response for is_closed, display_address, price, display_phone, rating, and categories
    JSONParser parser = new JSONParser();
    try {
      JSONObject jo = (JSONObject) parser.parse(httpResponse);

      //begin parsing JSON response to Yelp
      JSONArray businesses = (JSONArray) jo.get("businesses");
      for (Object business : businesses) {
        Restaurant ret = new Restaurant();
        JSONObject jsonBusiness = (JSONObject) business;
        ret.closed = (boolean) jsonBusiness.get("is_closed");

        //get address from JSONArray of strings
        JSONObject jsonLocation = (JSONObject) jsonBusiness.get("location");
        JSONArray jsonAddress = (JSONArray) jsonLocation.get("display_address");
        for (Object temp : jsonAddress)
          ret.address += temp.toString() + ",";
        ret.address = ret.address.substring(0, ret.address.length() - 1);

        ret.name  = to_string(jsonBusiness, "name");
        ret.price = to_string(jsonBusiness, "price");
        ret.phoneNumber = to_string(jsonBusiness,"phone");
        ret.rating = to_double(to_string(jsonBusiness, "rating"));
        ret.distanceFromUser = to_double(to_string(jsonBusiness, "distance"));
        ret.phoneDisplay = to_string(jsonBusiness, "display_phone");

        //get categories from JSONArray of strings
        JSONArray jsonCategories = (JSONArray) jsonBusiness.get("categories");
        for (Object temp : jsonCategories) {
          JSONObject JSONtemp = (JSONObject) temp;
          ret.categories.add(JSONtemp.get("title").toString());
        }
        yelpBusinesses.add(ret);
      }
    } catch (ParseException e) {
      e.printStackTrace();
      Log.e(TAG, "ParseException");
    }
//    System.out.println("Nutritionix restaurant: " + String.valueOf(restaurants.size()));
//    System.out.println("Yelp restaurant: " + String.valueOf(yelpBusinesses.size()));

    getMealItems();
  }

  public void getMealItems() {
    //iterate through restaurant list and get all menu items
    //Also update information that found on Yelp API
    int length = restaurants.size();

    for (int i = 0; i < length; i++) {
      Restaurant temp = restaurants.get(i);
      int index = is_contained(temp, yelpBusinesses);
      if (index >= 0)
      {
        update_ret(temp, yelpBusinesses.get(index));
      }

      //filter for rating  < 2.5
      if (!temp.closed && temp.rating >= 2.5 && !temp.categories.contains("Coffee & Tea")) { //don't add restaurant to list if closed
        nutritionixMealRequest(temp);
        ++restaurantCount;
      }
    }
  }

  public void nutritionixMealRequest(final Restaurant ret){
    //only get first word of restaurant name
    String query = ret.name.toLowerCase();
    if(query.contains(" ")) {
      query = query.substring(0, query.indexOf(" "));
    }

    //set url
    String url = "https://trackapi.nutritionix.com/v2/search/instant?common=false&";
    url += "query=" + query + "&";
    url += "brand_ids=" + ret.brand_id;

    RequestQueue queue = Volley.newRequestQueue(this);

    // Request a string response from the provided URL.
    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
              @Override
              public void onResponse(String response) {
                // Display the first 500 characters of the response string.
                httpResponse = response;
                String getResponse = response;
                Log.e(TAG, "meal getResponse" + getResponse);
                writeToFile("Nutri items.txt", getResponse);

                parseNutritionixMeal(ret);
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

  public void parseNutritionixMeal(Restaurant ret) {
    //parse Nutritionix search for food_name and calories. Save restaurant to the food as well
    JSONParser parser = new JSONParser();
    try {
      JSONObject jo = (JSONObject) parser.parse(httpResponse);

      //begin parsing JSON response to Nutritionix meal search
      JSONArray menu = (JSONArray)jo.get("branded");

      for(Object menuItem : menu) {
        JSONObject jsonItem = (JSONObject)menuItem;
        Food foodItem = new Food();
        foodItem.name = to_string(jsonItem, "food_name");
        foodItem.calories = to_double(to_string(jsonItem,"nf_calories"));
        foodItem.place = ret;
        //only add foodItem if it falls within the user's calorie limit
        if(foodItem.calories >= 250 && foodItem.calories <= (calorieLimit - mealCalories))
          foods.add(foodItem);
      }
      --restaurantCount;
    }
    catch(ParseException e) {
      e.printStackTrace();
      Log.e(TAG, "ParseException");
    }

    if(restaurantCount == 0) {
      //if 0, all restaurants have been iterated. Final food list should be finished
      Log.e(TAG, "Finished getting all meal items");
      for (Food food : foods) {
        Log.e(TAG, food.toString() + "\n" + food.place.toString());
      }

      //food_preferences is for testing only
      food_preferences.add("Fast Food");
      food_preferences.add("Sandwiches");
      food_preferences.add("Pizza");

      sortByCalories(foods);
      sortByDistance(foods);
      sortByPreferences(foods, food_preferences);

      setNewsFeed();
    }
  }

  public void setNewsFeed() {
    ScrollView sv = (ScrollView)findViewById(R.id.newsfeed);
    sv.removeAllViews(); //clear scroll view

    //set linear layout
    LinearLayout ll = new LinearLayout(this);
    ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
    ll.setOrientation(LinearLayout.VERTICAL);
    sv.addView(ll);

    TextView[] textViews = new TextView[foods.size()];
    for (int i = 0; i < foods.size(); ++i) {
      Food f = foods.get(i);
      textViews[i] = new TextView(this);
      String s = String.format("Name: %s\n\tCal: %f\n\t", f.name, f.calories);
      Restaurant r = f.place;
      s += String.format("Name:  %s\n\tPhone:  %s\n\tRating:   %s\n\tCategories:  ",r.name, r.phoneDisplay, String.valueOf(r.rating), r.categories.toString());
      for (String str : r.categories)
        s += str + ", ";
      s += String.format("\n\tDistance: %f\n", f.place.distanceFromUser);

      //set layout
      textViews[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
      textViews[i].setLines(8);
      textViews[i].setText(s);
      ll.addView(textViews[i], i, new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
    }
    System.out.println("Finished\n");

    //finish refreshing
    if (mySwipeRefreshLayout.isRefreshing()) {
      mySwipeRefreshLayout.setRefreshing(false);
    }
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

  public void menuButton(View v)
  {
    Intent n_intent = new Intent(getApplicationContext(), menuActivity.class);
    startActivity(n_intent);
  }

  public void writeToFile(String file_name, String data) {
    try {
      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(file_name, Context.MODE_PRIVATE));
      outputStreamWriter.write(data);
      outputStreamWriter.close();
    }
    catch (IOException e) {
      Log.e("Exception", "File write failed: " + e.toString());
    }
  }

  public void sortByPreferences(ArrayList<Food> fl, final ArrayList<String> preferences){
    Collections.sort(fl, new Comparator<Food>() {
      @Override
      public int compare(Food o1, Food o2) {
        int c1, c2;
        c1 = countPreferences(o1, preferences);
        c2 = countPreferences(o2, preferences);
        return c2 - c1;
      }
    });
  }
  private int countPreferences(Food f, ArrayList<String> preferences){
    int count =0;
    for (String s : f.place.categories)
    {
      if (preferences.contains(s))
        count++;
    }
    return count;
  }

  public void sortByDistance(ArrayList<Food> fl)
  {
    Collections.sort(fl, new Comparator<Food>() {
      @Override
      public int compare(Food o1, Food o2) {
        int d1, d2;
        d1 = (int)(o1.place.distanceFromUser * 1000);
        d2 = (int)(o2.place.distanceFromUser * 1000);
        return d1 -d2;
      }
    });
  }

  public void sortByCalories(ArrayList<Food> fl)
  {
    Collections.sort(fl, new Comparator<Food>() {
      @Override
      public int compare(Food o1, Food o2) {
        return (int)(o1.calories - o2.calories);
      }
    });
  }

  private void ratingFilter(Double min)
  {
    for (Restaurant r : restaurants)
    {
      if (r.rating < min && r.rating != 0.0)
        restaurants.remove(r);
    }
  }

  private void printList(ArrayList<Food> f_list)
  {
    for (Food f : f_list){
      System.out.print(f.name + "\t" + f.place.categories + "\n");
    }
    System.out.print('\n');
  }

  private String to_string(JSONObject oj, String key)
  {
    if (oj.get(key) == null)
      return "";
    else
      return oj.get(key).toString();
  }

  private Double to_double(String s)
  {
    if (s.equals(""))
      return -1.0;
    else
      return Double.valueOf(s);
  }

  private int is_contained(Restaurant r, ArrayList<Restaurant> l)
  {
    int length = l.size();
    for (int i = 0; i < length; i++)
    {
      if (r.equals(l.get(i)))
      {
        return i;
      }
      else
        System.out.printf("%s====%s\n", r.name, l.get(i).name);

    }
    return -1;
  }

  private void update_ret(Restaurant des, Restaurant n_val)
  {
    des.closed = n_val.closed;
    des.rating = n_val.rating;
    des.distanceFromUser = n_val.distanceFromUser;

    if (!n_val.address.isEmpty() )
      des.address = n_val.address;

    if (!n_val.price.isEmpty() )
      des.price = n_val.price;

    if (!n_val.phoneDisplay.isEmpty() )
      des.phoneDisplay= n_val.phoneDisplay;

    if (!n_val.categories.isEmpty())
      des.categories = new ArrayList<String>(n_val.categories);
  }

  private void print_ret(Restaurant r)
  {
    System.out.printf("Name:  %s\n\tPhone:  %s\n\tRating:   %s\n\tCategories:  ",r.name, r.phoneDisplay, String.valueOf(r.rating));
    for (String s : r.categories)
      System.out.print(s + ", ");
    System.out.println();
  }

  private void print_food(Food f)
  {
    System.out.printf("Name: %s\n\tCal: %f\n\t", f.name, f.calories);
    print_ret(f.place);
  }
}
//END OF BASIC ACTIVITY CLASS
