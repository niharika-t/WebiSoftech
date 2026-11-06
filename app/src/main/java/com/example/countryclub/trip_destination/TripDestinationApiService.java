
package com.example.countryclub.trip_destination;

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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TripDestinationApiService {

    private static final String TAG = "TripDestinationAPI";

    private static final String BASE_URL =
            "https://holidayserviceclub.com/HolidayServiceClub/public/api/";

    private static final String CATEGORY_ENDPOINT =
            "get-destination-category";

    private static final String SINGLE_CATEGORY_ENDPOINT =
            "get-destination-category/";

    private static final String DETAILS_ENDPOINT =
            "get-destination-details/";

    private static final ExecutorService executor =
            Executors.newFixedThreadPool(2);

    private static final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public interface CategoriesCallback {
        void onSuccess(List<TripDestination> categories);
        void onError(String message);
    }

    public interface CategoryCallback {
        void onSuccess(TripDestination category);
        void onError(String message);
    }

    public interface DetailsCallback {
        void onSuccess(List<TripDestination> details);
        void onError(String message);
    }

    // ------------------------------------------------
    // GET ALL DESTINATION CATEGORIES
    // ------------------------------------------------

    public void getCategories(CategoriesCallback callback) {

        executor.execute(() -> {

            try {
                JSONObject response = request(BASE_URL + CATEGORY_ENDPOINT);

                Log.d(TAG, "Category response: " + response);

                JSONArray data = extractArray(response);

                List<TripDestination> categories = new ArrayList<>();

                for (int i = 0; i < data.length(); i++) {

                    JSONObject obj = data.optJSONObject(i);

                    if (obj == null || !isActive(obj)) continue;

                    String id = getString(obj, "id", "category_id");

                    String name = getString(
                            obj, "name", "title", "category_name"
                    );

                    if (id.isEmpty()) continue;

                    categories.add(new TripDestination(
                            id,
                            id,
                            name.isEmpty() ? "Trip Category" : name,
                            getString(obj, "description", "details"),
                            resolveImage(getString(
                                    obj, "image", "image_url", "category_image"
                            )),
                            getString(obj, "subtitle", "location")
                    ));
                }

                if (categories.isEmpty()) {
                    postError(callback,
                            "No active trip destination categories found.");
                } else {
                    mainHandler.post(() -> callback.onSuccess(categories));
                }

            } catch (Exception e) {

                Log.e(TAG, "Category API failed", e);
                postError(callback, errorMessage(e));
            }
        });
    }

    // ------------------------------------------------
    // GET SINGLE CATEGORY
    // ------------------------------------------------

    public void getCategoryById(
            String categoryId,
            CategoryCallback callback) {

        executor.execute(() -> {

            try {
                if (categoryId == null || categoryId.trim().isEmpty()) {
                    postError(callback, "Category ID is missing.");
                    return;
                }

                String url = BASE_URL
                        + SINGLE_CATEGORY_ENDPOINT
                        + encode(categoryId.trim());

                JSONObject response = request(url);

                Log.d(TAG, "Single category response: " + response);

                checkStatus(response);

                JSONObject obj = extractObject(response);

                if (!isActive(obj)) {
                    postError(callback, "This category is inactive.");
                    return;
                }

                String id = getString(obj, "id", "category_id");

                TripDestination category = new TripDestination(
                        id.isEmpty() ? categoryId : id,
                        id.isEmpty() ? categoryId : id,
                        getString(obj, "name", "title", "category_name"),
                        getString(obj, "description", "details"),
                        resolveImage(getString(
                                obj, "image", "image_url", "category_image"
                        )),
                        getString(obj, "subtitle", "location")
                );

                mainHandler.post(() -> callback.onSuccess(category));

            } catch (Exception e) {

                Log.e(TAG, "Single category API failed", e);
                postError(callback, errorMessage(e));
            }
        });
    }

    // ------------------------------------------------
    // GET DETAILS BY CATEGORY ID
    // ------------------------------------------------

    public void getDetailsByCategory(
            String categoryId,
            DetailsCallback callback) {

        executor.execute(() -> {

            try {
                if (categoryId == null || categoryId.trim().isEmpty()) {
                    postError(callback, "Category ID is missing.");
                    return;
                }

                String url = BASE_URL
                        + DETAILS_ENDPOINT
                        + encode(categoryId.trim());

                Log.d(TAG, "Details API URL: " + url);

                JSONObject response = request(url);

                Log.d(TAG, "Details API response: " + response);

                checkStatus(response);

                JSONArray data = extractArray(response);

                List<TripDestination> details = new ArrayList<>();

                for (int i = 0; i < data.length(); i++) {

                    JSONObject obj = data.optJSONObject(i);

                    if (obj == null || !isActive(obj)) continue;

                    String id = getString(obj, "id", "destination_id");

                    if (id.isEmpty()) continue;

                    String itemCategoryId = getString(
                            obj, "category_id", "categoryId"
                    );

                    if (itemCategoryId.isEmpty()) {
                        itemCategoryId = categoryId.trim();
                    }

                    String title = getString(
                            obj, "title", "name", "destination_name"
                    );

                    String description = getString(
                            obj, "description", "details"
                    );

                    String image = resolveImage(getString(
                            obj, "image", "image_url", "destination_image"
                    ));

                    String subtitle = getString(
                            obj, "location", "subtitle"
                    );

                    details.add(new TripDestination(
                            id,
                            itemCategoryId,
                            title.isEmpty() ? "Destination" : title,
                            description,
                            image,
                            subtitle
                    ));
                }

                Log.d(TAG, "Parsed destination details: " + details.size());

                // Empty result is handled by the Fragment.
                mainHandler.post(() -> callback.onSuccess(details));

            } catch (Exception e) {

                Log.e(TAG, "Destination details API failed", e);
                postError(callback, errorMessage(e));
            }
        });
    }

    // ------------------------------------------------
    // HTTP REQUEST
    // ------------------------------------------------

    private static JSONObject request(String urlString) throws Exception {

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection)
                    new URL(urlString).openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("Accept", "application/json");

            int code = connection.getResponseCode();

            InputStream stream = (code >= 200 && code < 300)
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

            String responseText = result.toString();

            Log.d(TAG, "HTTP code: " + code);

            if (code < 200 || code >= 300) {
                throw new Exception(
                        "HTTP " + code + ": " + responseText
                );
            }

            return new JSONObject(responseText);

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // ------------------------------------------------
    // JSON RESPONSE HELPERS
    // ------------------------------------------------

    private static JSONArray extractArray(JSONObject response)
            throws JSONException {

        checkStatus(response);

        Object value = response.opt("data");

        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }

        // Also supports a direct array inside a nested result object.
        if (value instanceof JSONObject) {

            JSONObject dataObject = (JSONObject) value;

            Object nested = dataObject.opt("data");

            if (nested instanceof JSONArray) {
                return (JSONArray) nested;
            }

            throw new JSONException(
                    "Expected an array in data, but received an object."
            );
        }

        throw new JSONException(
                "Expected a JSON array in the API data field."
        );
    }

    private static JSONObject extractObject(JSONObject response)
            throws JSONException {

        checkStatus(response);

        Object value = response.opt("data");

        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }

        if (value instanceof JSONArray) {

            JSONArray array = (JSONArray) value;

            if (array.length() > 0 && array.optJSONObject(0) != null) {
                return array.getJSONObject(0);
            }
        }

        throw new JSONException(
                "Expected a category object in the API data field."
        );
    }

    private static void checkStatus(JSONObject response)
            throws JSONException {

        String status = response.optString("status", "");

        if ("error".equalsIgnoreCase(status)
                || "failed".equalsIgnoreCase(status)
                || "failure".equalsIgnoreCase(status)) {

            throw new JSONException(
                    response.optString(
                            "message", "API returned an error."
                    )
            );
        }
    }

    // ------------------------------------------------
    // JSON VALUE HELPER
    // ------------------------------------------------

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

    // ------------------------------------------------
    // ACTIVE STATUS
    // ------------------------------------------------

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

    // ------------------------------------------------
    // IMAGE URL HELPER
    // ------------------------------------------------

    private static String resolveImage(String image) {

        if (image == null || image.trim().isEmpty()
                || "null".equalsIgnoreCase(image.trim())) {
            return "";
        }

        image = image.trim();

        if (image.startsWith("https://")
                || image.startsWith("http://")) {
            return image;
        }

        image = image.replaceFirst("^/+", "");

        if (image.startsWith("public/")) {
            image = image.substring("public/".length());
        }

        return "https://holidayserviceclub.com/HolidayServiceClub/public/"
                + image;
    }

    // ------------------------------------------------
    // URL ENCODING
    // ------------------------------------------------

    private static String encode(String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8");
    }

    // ------------------------------------------------
    // ERROR HELPERS
    // ------------------------------------------------

    private static String errorMessage(Exception e) {

        String message = e.getMessage();

        return message == null || message.trim().isEmpty()
                ? "Unable to load trip destination data."
                : message;
    }

    private static void postError(
            CategoriesCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }

    private static void postError(
            CategoryCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }

    private static void postError(
            DetailsCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
