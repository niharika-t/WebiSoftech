
package com.example.countryclub.resorts;

public class Resort {

    private final String id;
    private final String categoryId;
    private final String title;
    private final String description;
    private final String image;

    public Resort(
            String id,
            String categoryId,
            String title,
            String description,
            String image) {

        this.id = id;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.image = image;
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
}
