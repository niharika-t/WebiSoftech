
package com.example.countryclub.subscriptions;

public class SubscriptionDetail {

    private final String id;
    private final String categoryId;
    private final String title;
    private final String description;
    private final String image;

    public SubscriptionDetail(
            String id,
            String categoryId,
            String title,
            String description,
            String image) {

        this.id = clean(id);
        this.categoryId = clean(categoryId);
        this.title = clean(title);
        this.description = clean(description);
        this.image = clean(image);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
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
