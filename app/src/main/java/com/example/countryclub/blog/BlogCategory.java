package com.example.countryclub.blog;

public class BlogCategory {
    private final String id;
    private final String name;

    public BlogCategory(String id, String name) {
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