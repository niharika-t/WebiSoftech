
package com.example.countryclub.fitness;

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
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FitnessApiService {

    private static final String TAG = "EventApiService";

    private static final String BASE_URL =
            "https://holidayserviceclub.com/HolidayServiceClub/public/api/";

    private static final String CATEGORY_ENDPOINT = "get-event-category";
    private static final String EVENT_DETAILS_ENDPOINT = "get-event-details/";

    private static final ExecutorService executor =
            Executors.newFixedThreadPool(2);

    private static final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public interface CategoriesCallback {
        void onSuccess(List<FitnessCategory> categories);
        void onError(String message);
    }

    public interface EventsCallback {
        void onSuccess(List<Fitness> events);
        void onError(String message);
    }

    // Load event categories
    public static void getEventCategories(CategoriesCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject response = request(BASE_URL + CATEGORY_ENDPOINT);
                JSONArray data = getArrayOrSingleObject(response, "data");

                List<FitnessCategory> categories = new ArrayList<>();

                for (int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.getJSONObject(i);

                    if (!isActive(obj)) {
                        continue;
                    }

                    categories.add(new FitnessCategory(
                            getString(obj, "id"),
                            getString(obj, "name"),
                            resolveImage(getString(obj, "image"))
                    ));
                }

                mainHandler.post(() -> callback.onSuccess(categories));

            } catch (Exception e) {
                Log.e(TAG, "Failed to fetch event categories", e);

                String message = e.getMessage() != null
                        ? e.getMessage()
                        : "Unable to load event categories";

                mainHandler.post(() -> callback.onError(message));
            }
        });
    }

    // Load events for the selected category
    public static void getEventsByCategory(
            String categoryId,
            EventsCallback callback
    ) {
        executor.execute(() -> {
            try {
                String endpoint =
                        BASE_URL + EVENT_DETAILS_ENDPOINT + categoryId;

                JSONObject response = request(endpoint);
                JSONArray data = getArrayOrSingleObject(response, "data");

                List<Fitness> events = new ArrayList<>();

                for (int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.getJSONObject(i);

                    if (!isActive(obj)) {
                        continue;
                    }

                    String title = getString(obj, "title");

                    // The API uses "details", not necessarily "description".
                    String description = getString(obj, "details");

                    if (description.isEmpty()) {
                        description = getString(obj, "description");
                    }

                    // Format API start/end dates without the time.
                    String startDate =
                            formatDate(getString(obj, "start_date"));

                    String endDate =
                            formatDate(getString(obj, "end_date"));

                    String eventDate;

                    if (!startDate.isEmpty() && !endDate.isEmpty()) {
                        eventDate = "Start: " + startDate
                                + " | End: " + endDate;
                    } else if (!startDate.isEmpty()) {
                        eventDate = "Start: " + startDate;
                    } else if (!endDate.isEmpty()) {
                        eventDate = "End: " + endDate;
                    } else {
                        // Fallback if an API response uses a single date.
                        eventDate = formatDate(getString(obj, "date"));
                    }

                    String imageUrl =
                            resolveImage(getString(obj, "image"));

                    String location =
                            getString(obj, "location");

                    Fitness event = new Fitness(
                            getString(obj, "id"),
                            getString(obj, "category_id"),
                            title,
                            description,
                            imageUrl,
                            eventDate,
                            location
                    );

                    events.add(event);

                    Log.d(TAG, "Event loaded: " + title);
                    Log.d(TAG, "Description: " + description);
                    Log.d(TAG, "Date: " + eventDate);
                    Log.d(TAG, "Location: " + location);
                }

                mainHandler.post(() -> callback.onSuccess(events));

            } catch (Exception e) {
                Log.e(TAG,
                        "Failed to fetch events for category " + categoryId,
                        e);

                String message = e.getMessage() != null
                        ? e.getMessage()
                        : "Unable to load events";

                mainHandler.post(() -> callback.onError(message));
            }
        });
    }

    // Perform the GET request
    private static JSONObject request(String endpoint) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = URI.create(endpoint).toURL();

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("Accept", "application/json");

            int status = connection.getResponseCode();

            InputStream stream;

            if (status >= 200 && status < 300) {
                stream = connection.getInputStream();
            } else {
                stream = connection.getErrorStream();
            }

            if (stream == null) {
                throw new Exception("HTTP error: " + status);
            }

            StringBuilder result = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            }

            if (status < 200 || status >= 300) {
                throw new Exception(
                        "HTTP " + status + ": " + result);
            }

            JSONObject response =
                    new JSONObject(result.toString());

            if (!"success".equalsIgnoreCase(
                    response.optString("status"))) {

                throw new Exception(
                        response.optString(
                                "message",
                                "API returned an unsuccessful status"
                        )
                );
            }

            return response;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // Supports either an array or a single object in "data"
    private static JSONArray getArrayOrSingleObject(
            JSONObject response,
            String key
    ) throws JSONException {

        Object data = response.opt(key);

        if (data instanceof JSONArray) {
            return (JSONArray) data;
        }

        if (data instanceof JSONObject) {
            JSONArray array = new JSONArray();
            array.put(data);
            return array;
        }

        throw new JSONException(
                "Expected an array or object in response field: " + key);
    }

    // Safely read a JSON value as a String
    private static String getString(JSONObject obj, String key) {
        Object value = obj.opt(key);

        if (value == null || value == JSONObject.NULL) {
            return "";
        }

        return String.valueOf(value).trim();
    }

    // Keep active, non-deleted records only
    private static boolean isActive(JSONObject obj) {
        String status = obj.optString("status", "1");
        String deleted = obj.optString("is_deleted", "0");

        return "1".equals(status) && !"1".equals(deleted);
    }

    // Convert "2026-07-18 08:00:00" to "18 Jul 2026"
    private static String formatDate(String dateTime) {
        if (dateTime == null || dateTime.trim().isEmpty()) {
            return "";
        }

        dateTime = dateTime.trim();

        String[] inputPatterns = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        };

        for (String pattern : inputPatterns) {
            try {
                SimpleDateFormat inputFormat =
                        new SimpleDateFormat(pattern, Locale.ENGLISH);

                inputFormat.setLenient(false);

                Date date = inputFormat.parse(dateTime);

                if (date != null) {
                    SimpleDateFormat outputFormat =
                            new SimpleDateFormat(
                                    "dd MMM yyyy",
                                    Locale.ENGLISH
                            );

                    return outputFormat.format(date);
                }

            } catch (ParseException ignored) {
                // Try the next supported input format.
            }
        }

        // Preserve the original value if it has an unexpected format.
        return dateTime;
    }

    // Convert relative image paths to full URLs
    // Convert relative image paths to full URLs
    private static String resolveImage(String path) {

        if (path == null || path.trim().isEmpty()
                || "null".equalsIgnoreCase(path.trim())) {
            return "";
        }

        path = path.trim();

        if (path.startsWith("https://")
                || path.startsWith("http://")) {
            return path;
        }

        path = path.replaceFirst("^/+", "");

        if (path.startsWith("public/")) {
            path = path.substring("public/".length());
        }

        return "https://holidayserviceclub.com/HolidayServiceClub/public/"
                + path;
    }
}
