
package com.example.countryclub.subscriptions;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubscriptionApiService {

    private static final String TAG = "SubscriptionAPI";

    private static final String BASE_URL =
            "https://holidayserviceclub.com/HolidayServiceClub/public/api/";

    private static final ExecutorService executor =
            Executors.newFixedThreadPool(2);

    private static final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public interface CategoriesCallback {
        void onSuccess(List<SubscriptionCategory> categories);
        void onError(String message);
    }

    public interface DetailsCallback {
        void onSuccess(List<SubscriptionDetail> details);
        void onError(String message);
    }

    public void getCategories(CategoriesCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject response = request(
                        BASE_URL + "get-subscription-category");

                Log.d(TAG, "Category response: " + response);

                JSONArray data = getDataArray(response);

                List<SubscriptionCategory> categories =
                        new ArrayList<>();

                for (int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.optJSONObject(i);

                    if (obj == null || !isActive(obj)) {
                        continue;
                    }

                    String id = getString(obj, "id");

                    // This API returns "name", not "title".
                    String name = getString(
                            obj, "name", "title", "category_name");

                    String amount = getString(obj, "amount");
                    String duration = getString(obj, "duration");

                    String image = resolveImage(
                            getString(obj, "image", "image_url"));

                    Log.d(TAG,
                            "Category id=" + id
                                    + ", name=" + name
                                    + ", amount=" + amount
                                    + ", duration=" + duration);

                    if (!id.isEmpty()) {
                        categories.add(new SubscriptionCategory(
                                id,
                                name,
                                amount,
                                duration,
                                image
                        ));
                    }
                }

                if (categories.isEmpty()) {
                    postError(callback,
                            "No active subscription categories found.");
                } else {
                    mainHandler.post(
                            () -> callback.onSuccess(categories));
                }

            } catch (Exception e) {
                Log.e(TAG, "Category API failed", e);
                postError(callback, errorMessage(e));
            }
        });
    }

    public void getDetailsByCategory(
            String categoryId,
            DetailsCallback callback) {

        executor.execute(() -> {
            try {
                if (categoryId == null
                        || categoryId.trim().isEmpty()) {
                    postError(callback, "Category ID is missing.");
                    return;
                }

                JSONObject response = request(
                        BASE_URL + "get-subscription-details/"
                                + categoryId.trim());

                Log.d(TAG, "Details response: " + response);

                JSONArray data = getDataArray(response);

                List<SubscriptionDetail> details =
                        new ArrayList<>();

                for (int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.optJSONObject(i);

                    if (obj == null || !isActive(obj)) {
                        continue;
                    }

                    String id = getString(obj, "id");

                    String itemCategoryId = getString(
                            obj, "category_id", "categoryId");

                    String title = getString(
                            obj, "title", "name");

                    String description = getString(
                            obj, "description", "details");

                    String image = resolveImage(
                            getString(obj, "image", "image_url"));

                    if (itemCategoryId.isEmpty()) {
                        itemCategoryId = categoryId.trim();
                    }

                    details.add(new SubscriptionDetail(
                            id,
                            itemCategoryId,
                            title,
                            description,
                            image
                    ));
                }

                mainHandler.post(() -> callback.onSuccess(details));

            } catch (Exception e) {
                Log.e(TAG, "Details API failed", e);
                postError(callback, errorMessage(e));
            }
        });
    }

    private static JSONObject request(String urlString)
            throws Exception {

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection)
                    new URL(urlString).openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty(
                    "Accept", "application/json");

            int code = connection.getResponseCode();

            InputStream stream = code >= 200 && code < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            if (stream == null) {
                throw new Exception("HTTP error: " + code);
            }

            StringBuilder result = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            stream, StandardCharsets.UTF_8))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            }

            if (code < 200 || code >= 300) {
                throw new Exception(
                        "HTTP " + code + ": " + result);
            }

            return new JSONObject(result.toString());

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static JSONArray getDataArray(JSONObject response)
            throws JSONException {

        String status = response.optString("status", "");

        if ("error".equalsIgnoreCase(status)
                || "failed".equalsIgnoreCase(status)
                || "failure".equalsIgnoreCase(status)) {
            throw new JSONException(
                    response.optString(
                            "message", "API returned an error."));
        }

        Object value = response.opt("data");

        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }

        if (value instanceof JSONObject) {
            JSONObject object = (JSONObject) value;

            String[] keys = {
                    "items", "categories", "details",
                    "subscriptions", "results", "list"
            };

            for (String key : keys) {
                Object nested = object.opt(key);

                if (nested instanceof JSONArray) {
                    return (JSONArray) nested;
                }
            }

            JSONArray singleItem = new JSONArray();
            singleItem.put(object);
            return singleItem;
        }

        throw new JSONException(
                "Response does not contain valid subscription data.");
    }

    private static String getString(
            JSONObject obj, String... keys) {

        for (String key : keys) {
            Object value = obj.opt(key);

            if (value != null && value != JSONObject.NULL) {
                String text = String.valueOf(value).trim();

                if (!text.isEmpty()
                        && !"null".equalsIgnoreCase(text)) {
                    return text;
                }
            }
        }

        return "";
    }

    private static boolean isActive(JSONObject obj) {
        String status = getString(obj, "status", "is_active");

        if ("0".equals(status)
                || "false".equalsIgnoreCase(status)
                || "inactive".equalsIgnoreCase(status)) {
            return false;
        }

        String deleted = getString(obj, "is_deleted", "deleted");

        return !"1".equals(deleted)
                && !"true".equalsIgnoreCase(deleted);
    }

    private static String resolveImage(String image) {
        if (image == null || image.trim().isEmpty()) {
            return "";
        }

        image = image.trim();

        if (image.startsWith("https://")
                || image.startsWith("http://")) {
            return image;
        }

        if (image.startsWith("/")) {
            return "https://holidayserviceclub.com" + image;
        }

        return "https://holidayserviceclub.com/" + image;
    }

    private static String errorMessage(Exception e) {
        String message = e.getMessage();

        return message == null || message.trim().isEmpty()
                ? "Unable to load subscription data."
                : message;
    }

    private static void postError(
            CategoriesCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }

    private static void postError(
            DetailsCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
