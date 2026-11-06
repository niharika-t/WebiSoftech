package com.example.countryclub.fitness;

public class Fitness {
    private final String id;
    private final String categoryId;
    private final String title;
    private final String description;
    private final String image;
    private final String date;
    private final String location;

    public Fitness(String id, String categoryId, String title,
                   String description, String image,
                   String date, String location) {
        this.id = id;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.image = image;
        this.date = date;
        this.location = location;
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

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }
}