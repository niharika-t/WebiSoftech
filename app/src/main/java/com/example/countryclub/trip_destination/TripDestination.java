
package com.example.countryclub.trip_destination;

public class TripDestination {

    private final String id;
    private final String categoryId;
    private final String title;
    private final String description;
    private final String image;
    private final String subtitle;

    public TripDestination(
            String id,
            String categoryId,
            String title,
            String description,
            String image,
            String subtitle) {

        this.id = id;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.image = image;
        this.subtitle = subtitle;
    }

    public String getId() {
        return id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public String getSubtitle() {
        return subtitle;
    }
}
