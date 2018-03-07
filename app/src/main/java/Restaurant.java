import java.util.ArrayList;

public class Restaurant {
    //class that holds variables received from requests to Nutritionix and Yelp

    //pull from Nutritionix Location
    public String name;
    public double brand_id;
    public double distanceFromUser;

    //pull from Yelp
    public String address;
    public String price;
    public double rating;
    public String phoneNumber;

    //pull from Nutritionix Search/Instant
    class MenuItem {
        String name;
        double calories;

        MenuItem(String n, double c) {
            name = n;
            calories = c;
        }
    }
    public ArrayList<MenuItem> menu = new ArrayList<MenuItem>();


    public void addMenuItem(String itemName, double itemCalories) {
        MenuItem item = new MenuItem(itemName, itemCalories);
        menu.add(item);
    }
}
