
package com.example.countryclub.locate;

public class LocationCategory {

    private final String id;
    private final String name;

    public LocationCategory(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
