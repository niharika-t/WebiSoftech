package com.example.countryclub.home;

public class HomeCategory {

    public static final String TYPE_RESORT = "resort";
    public static final String TYPE_EVENT = "event";

    public static final String TYPE_DESTINATION = "destination";

    private final String id;
    private final String name;
    private final String type;
    private final String image;

    public HomeCategory(
            String id,
            String name,
            String type,
            String image) {

        this.id = id;
        this.name = name;
        this.type = type;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getImage() {
        return image;
    }
}