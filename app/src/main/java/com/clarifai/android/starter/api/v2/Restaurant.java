package com.clarifai.android.starter.api.v2;

import java.util.ArrayList;

public class Restaurant {
    //pull from Nutritionix Location
    public String name;
    public String brand_id;
    public String distanceFromUser;

    //pull from Yelp
    public boolean closed;
    public String address;
    public String price;
    public double rating;
    public String phoneNumber;
    public ArrayList<String> categories = new ArrayList<String>();

    @Override
    public String toString() {
        return name + ": " + "rating:" + rating + ", categories:" + categories.toString();
    }
}
