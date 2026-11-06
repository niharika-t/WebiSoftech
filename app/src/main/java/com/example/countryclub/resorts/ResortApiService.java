
package com.example.countryclub.resorts;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResortApiService {

    private static final String TAG = "ResortApiService";

    private static final String BASE_URL =
            "https://holidayserviceclub.com/HolidayServiceClub/public/api/";

    private static final String CATEGORY_URL =
            BASE_URL + "get-resort-category";

    private static final String CATEGORY_BY_ID_URL =
            BASE_URL + "get-resort-category/";

    private static final String RESORT_DETAILS_URL =
            BASE_URL + "get-resort-details/";

    private static final ExecutorService executor =
            Executors.newFixedThreadPool(2);

    private static final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public interface ApiCallback<T> {
        void onSuccess(T result);

        void onError(String message);
    }

    private static String getResponse(String urlString)
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

            int statusCode = connection.getResponseCode();

            InputStream stream =
                    statusCode >= 200 && statusCode < 300
                            ? connection.getInputStream()
                            : connection.getErrorStream();

            if (stream == null) {
                throw new Exception(
                        "Empty response. HTTP " + statusCode);
            }

            StringBuilder result = new StringBuilder();

            try (InputStream input = stream;
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(
                                 input, StandardCharsets.UTF_8))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            }

            if (statusCode < 200 || statusCode >= 300) {
                throw new Exception(
                        "HTTP " + statusCode + ": " + result);
            }

            return result.toString();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static JSONObject getJsonObject(String url)
            throws Exception {

        return new JSONObject(getResponse(url));
    }

    // Fetch all resort categories
    public static void getResortCategories(
            ApiCallback<JSONArray> callback) {

        executor.execute(() -> {
            try {
                JSONObject response =
                        getJsonObject(CATEGORY_URL);

                if (!"success".equalsIgnoreCase(
                        response.optString("status"))) {
                    throw new Exception(
                            "Category API failed: " + response);
                }

                JSONArray data =
                        response.optJSONArray("data");

                if (data == null) {
                    throw new Exception(
                            "Category data is not an array: "
                                    + response);
                }

                mainHandler.post(
                        () -> callback.onSuccess(data));

            } catch (Exception e) {
                Log.e(TAG, "Failed to load categories", e);
                postError(callback, e);
            }
        });
    }

    // Fetch one resort category by ID
    public static void getResortCategoryById(
            String categoryId,
            ApiCallback<JSONObject> callback) {

        executor.execute(() -> {
            try {
                validateId(categoryId);

                String url =
                        CATEGORY_BY_ID_URL + categoryId.trim();

                JSONObject response = getJsonObject(url);

                if (!"success".equalsIgnoreCase(
                        response.optString("status"))) {
                    throw new Exception(
                            "Category API failed: " + response);
                }

                JSONObject category =
                        response.optJSONObject("data");

                if (category == null) {
                    throw new Exception(
                            "Category data is not an object: "
                                    + response);
                }

                mainHandler.post(
                        () -> callback.onSuccess(category));

            } catch (Exception e) {
                Log.e(TAG, "Failed to load category", e);
                postError(callback, e);
            }
        });
    }

    // Fetch resorts for the selected category
    public static void getResortDetails(
            String categoryId,
            ApiCallback<JSONArray> callback) {

        executor.execute(() -> {
            try {
                validateId(categoryId);

                String url =
                        RESORT_DETAILS_URL + categoryId.trim();

                Log.d(TAG, "Resort details API: " + url);

                JSONObject response = getJsonObject(url);

                if (!"success".equalsIgnoreCase(
                        response.optString("status"))) {
                    throw new Exception(
                            "Resort details API failed: "
                                    + response);
                }

                JSONArray data =
                        response.optJSONArray("data");

                if (data == null) {
                    throw new Exception(
                            "Resort details data is not an array: "
                                    + response);
                }

                mainHandler.post(
                        () -> callback.onSuccess(data));

            } catch (Exception e) {
                Log.e(TAG,
                        "Failed to load resorts for category "
                                + categoryId, e);
                postError(callback, e);
            }
        });
    }

    private static void validateId(String id)
            throws Exception {

        if (id == null
                || id.trim().isEmpty()
                || "null".equalsIgnoreCase(id.trim())) {

            throw new Exception("Invalid category ID");
        }
    }

    private static <T> void postError(
            ApiCallback<T> callback,
            Exception e) {

        String message = e.getMessage() == null
                ? "Unable to load data"
                : e.getMessage();

        mainHandler.post(
                () -> callback.onError(message));
    }
}
