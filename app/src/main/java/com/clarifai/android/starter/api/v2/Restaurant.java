package com.clarifai.android.starter.api.v2;

import java.util.ArrayList;

public class Restaurant {
    //compare
    public String name;
    public String phoneNumber;
    public double distanceFromUser;

    //pull from Nutritionix Location
    public String brand_id;

    //pull from Yelp
    public boolean closed;
    public String address;
    public String price;
    public double rating;
    public ArrayList<String> categories = new ArrayList<String>();
    public String phoneDisplay;

    @Override
    public String toString() {
        return name + ": " + "rating:" + rating + ", categories:" + categories.toString();
    }

    public boolean equals(Restaurant ret)
    {
        if (!isEmpty() && !ret.isEmpty()) {
            String reg = "(.*)" + ret.name + "(.*)";
            if (name.matches(reg) || name.isEmpty() || ret.name.isEmpty()) {
//                if (phoneNumber.equals(ret.phoneNumber) || phoneNumber.isEmpty() || ret.phoneNumber.isEmpty())
//                {
                if (distanceFromUser > 0 && ret.distanceFromUser > 0) {
                    double d = distanceFromUser - ret.distanceFromUser;
                    double offset = R.string.distance_offset;
                    if (d > offset || d < (0 - offset))
                        return false;
                }
                return true;
//                }
            }
        }
        return false;
    }
    private boolean isEmpty()
    {
        return (name.isEmpty() && phoneNumber.isEmpty());
    }

}
