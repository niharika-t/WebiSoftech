
package com.example.countryclub.locate;

public class Location {

    private final String name;
    private final String city;
    private final String address;
    private final String category;
    private final String facilities;
    private final String breakfast;
    private final String lunch;
    private final String dinner;
    private final int imageResId;

    public Location(String name, String city, String address,
                    String category, String facilities,
                    String breakfast, String lunch, String dinner,
                    int imageResId) {
        this.name = name;
        this.city = city;
        this.address = address;
        this.category = category;
        this.facilities = facilities;
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
        this.imageResId = imageResId;
    }

    public String getName() { return name; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public String getCategory() { return category; }
    public String getFacilities() { return facilities; }
    public String getBreakfast() { return breakfast; }
    public String getLunch() { return lunch; }
    public String getDinner() { return dinner; }
    public int getImageResId() { return imageResId; }
}
