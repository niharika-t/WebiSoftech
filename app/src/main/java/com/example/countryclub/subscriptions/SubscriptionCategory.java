
package com.example.countryclub.subscriptions;

public class SubscriptionCategory {

    private final String id;
    private final String title;
    private final String amount;
    private final String duration;
    private final String image;

    public SubscriptionCategory(
            String id,
            String title,
            String amount,
            String duration,
            String image) {

        this.id = clean(id);
        this.title = clean(title);
        this.amount = clean(amount);
        this.duration = clean(duration);
        this.image = clean(image);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAmount() {
        return amount;
    }

    public String getDuration() {
        return duration;
    }

    public String getImage() {
        return image;
    }

    public String getDisplayInfo() {
        StringBuilder info = new StringBuilder();

        if (!amount.isEmpty()) {
            info.append("₹").append(amount);
        }

        if (!duration.isEmpty()) {
            if (info.length() > 0) {
                info.append("  •  ");
            }
            info.append(duration);
        }

        return info.toString();
    }
}
