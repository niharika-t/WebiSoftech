
package com.example.countryclub.blog;

public class Blog {

    private final String id;
    private final String categoryId;
    private final String title;
    private final String description;
    private final String imageUrl;
    private final String date;

    // Full constructor for API data
    public Blog(String id, String categoryId, String title,
                String description, String imageUrl, String date) {
        this.id = id;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.date = date;
    }

    // Keep this constructor for existing code
    public Blog(String title, String description,
                String imageUrl, String date) {
        this("", "", title, description, imageUrl, date);
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

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDate() {
        return date;
    }
}
