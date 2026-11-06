
package com.example.countryclub.blog;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlogApiService {

    private static final String TAG = "BlogApiService";

    private static final String BASE_URL =
            "https://holidayserviceclub.com/HolidayServiceClub/public/api/";

    private static final String CATEGORY_URL =
            BASE_URL + "get-blog-category";

    private static final String CATEGORY_BY_ID_URL =
            BASE_URL + "get-blog-category/";

    private static final String BLOG_DETAILS_URL =
            BASE_URL + "get-blog-details/";

    private static final ExecutorService executor =
            Executors.newFixedThreadPool(2);

    private static final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    // Perform GET request
    private static String getResponse(String urlString) throws Exception {

        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("Accept", "application/json");

            int statusCode = connection.getResponseCode();

            InputStream stream =
                    (statusCode >= 200 && statusCode < 300)
                            ? connection.getInputStream()
                            : connection.getErrorStream();

            if (stream == null) {
                throw new Exception("Empty response. HTTP " + statusCode);
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

    private static JSONObject getJsonObject(String url) throws Exception {
        return new JSONObject(getResponse(url));
    }

    // Load all blog categories
    public static void getBlogCategories(
            ApiCallback<JSONArray> callback) {

        executor.execute(() -> {
            try {
                JSONObject response = getJsonObject(CATEGORY_URL);

                if (!"success".equalsIgnoreCase(
                        response.optString("status"))) {
                    throw new Exception(
                            "Category API failed: " + response);
                }

                JSONArray data = response.optJSONArray("data");

                if (data == null) {
                    throw new Exception(
                            "Category list is not an array: " + response);
                }

                mainHandler.post(() -> callback.onSuccess(data));

            } catch (Exception e) {
                Log.e(TAG, "Failed to load categories", e);
                postError(callback, e);
            }
        });
    }

    // Fetch one category
    public static void getBlogCategoryById(
            String categoryId,
            ApiCallback<JSONObject> callback) {

        executor.execute(() -> {
            try {
                validateId(categoryId);

                String url = CATEGORY_BY_ID_URL + categoryId.trim();

                Log.d(TAG, "Category request: " + url);

                JSONObject response = getJsonObject(url);

                if (!"success".equalsIgnoreCase(
                        response.optString("status"))) {
                    throw new Exception(
                            "Category API failed: " + response);
                }

                JSONObject category = response.optJSONObject("data");

                if (category == null) {
                    throw new Exception(
                            "Category data is not an object: " + response);
                }

                mainHandler.post(() -> callback.onSuccess(category));

            } catch (Exception e) {
                Log.e(TAG, "Failed to load category " + categoryId, e);
                postError(callback, e);
            }
        });
    }

    // Fetch blogs for a selected category.
    // Formats created_at before returning the array.
    public static void getBlogsForCategory(
            String categoryId,
            ApiCallback<JSONArray> callback) {

        executor.execute(() -> {
            try {
                validateId(categoryId);

                String url = BLOG_DETAILS_URL + categoryId.trim();

                Log.d(TAG, "Blog list request: " + url);

                JSONObject response = getJsonObject(url);

                if (!"success".equalsIgnoreCase(
                        response.optString("status"))) {
                    throw new Exception(
                            "Blog API failed: " + response);
                }

                JSONArray blogs = response.optJSONArray("data");

                if (blogs == null) {
                    throw new Exception(
                            "Expected blog array, received: " + response);
                }

                // Remove the time from created_at for each blog.
                for (int i = 0; i < blogs.length(); i++) {
                    JSONObject blog = blogs.optJSONObject(i);

                    if (blog != null) {
                        String createdAt =
                                blog.optString("created_at", "");

                        if (!createdAt.isEmpty()
                                && !"null".equalsIgnoreCase(createdAt)) {
                            blog.put(
                                    "created_at",
                                    formatDate(createdAt)
                            );
                        }
                    }
                }

                Log.d(TAG, "Blog array size: " + blogs.length());

                mainHandler.post(() -> callback.onSuccess(blogs));

            } catch (Exception e) {
                Log.e(TAG,
                        "Failed to load blogs for ID " + categoryId, e);
                postError(callback, e);
            }
        });
    }

    // Fetch a single blog.
    // The API returns data as an array, so use its first object.
    public static void getBlogDetails(
            String blogId,
            ApiCallback<Blog> callback) {

        executor.execute(() -> {
            try {
                validateId(blogId);

                String url = BLOG_DETAILS_URL + blogId.trim();

                Log.d(TAG, "Fetching blog details from: " + url);

                JSONObject response = getJsonObject(url);

                if (!"success".equalsIgnoreCase(
                        response.optString("status"))) {
                    throw new Exception(
                            "Blog API failed: " + response);
                }

                JSONArray data = response.optJSONArray("data");

                if (data == null || data.length() == 0) {
                    throw new Exception(
                            "No blog details found: " + response);
                }

                JSONObject blogObject = data.optJSONObject(0);

                if (blogObject == null) {
                    throw new Exception("Invalid blog data");
                }

                String createdAt =
                        formatDate(blogObject.optString("created_at", ""));

                Blog blog = new Blog(
                        blogObject.optString("id", ""),
                        blogObject.optString("category_id", ""),
                        blogObject.optString("title", ""),
                        blogObject.optString("description", ""),
                        blogObject.optString("image", ""),
                        createdAt
                );

                mainHandler.post(() -> callback.onSuccess(blog));

            } catch (Exception e) {
                Log.e(TAG, "Single blog details error", e);

                String message = e.getMessage() == null
                        ? "Unable to load blog details"
                        : e.getMessage();

                mainHandler.post(() -> callback.onError(message));
            }
        });
    }

    // Format "2026-10-16 13:16:22" as "16 Oct 2026"
    private static String formatDate(String dateTime) {

        if (dateTime == null || dateTime.trim().isEmpty()
                || "null".equalsIgnoreCase(dateTime.trim())) {
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
                // Try the next supported date format.
            }
        }

        return dateTime;
    }

    private static void validateId(String id) throws Exception {
        if (id == null
                || id.trim().isEmpty()
                || "null".equalsIgnoreCase(id.trim())) {
            throw new Exception("Invalid ID");
        }
    }

    private static <T> void postError(
            ApiCallback<T> callback,
            Exception e) {

        String message = e.getMessage() == null
                ? "Something went wrong while loading data"
                : e.getMessage();

        mainHandler.post(() -> callback.onError(message));
    }
}
