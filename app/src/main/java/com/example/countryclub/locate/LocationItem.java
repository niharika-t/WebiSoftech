
package com.example.countryclub.locate;

public class LocationItem {

    private final String id;
    private final String categoryId;
    private final String title;
    private final String location;
    private final String address;
    private final String breakfast;
    private final String lunch;
    private final String dinner;
    private final String image;

    public LocationItem(
            String id,
            String categoryId,
            String title,
            String location,
            String address,
            String breakfast,
            String lunch,
            String dinner,
            String image) {

        this.id = id;
        this.categoryId = categoryId;
        this.title = title;
        this.location = location;
        this.address = address;
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
        this.image = image;
    }

    public String getId() { return id; }
    public String getCategoryId() { return categoryId; }
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public String getAddress() { return address; }
    public String getBreakfast() { return breakfast; }
    public String getLunch() { return lunch; }
    public String getDinner() { return dinner; }
    public String getImage() { return image; }
}
