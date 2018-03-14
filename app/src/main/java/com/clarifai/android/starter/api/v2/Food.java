package com.clarifai.android.starter.api.v2;

import java.util.Comparator;

public class Food {
    //class that holds variables received from requests to Nutritionix and Yelp

    //pull from Nutritionix Search/Instant
    public String name;
    public double calories;

    public Restaurant place;

    @Override
    public String toString() {
        return name + ": " + calories;
    }

}