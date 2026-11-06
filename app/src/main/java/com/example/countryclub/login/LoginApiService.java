package com.example.countryclub.login;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginApiService {

    private static final String LOGIN_URL =
            "https://www.holidayserviceclub.com/HolidayServiceClub/public/api/login";

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public interface LoginCallback {
        void onSuccess(String message, JSONObject response);

        void onError(String message);
    }

    public void login(String email, String password,
                      LoginCallback callback) {

        executor.execute(() -> {

            HttpURLConnection connection = null;

            try {
                URL url = new URL(LOGIN_URL);

                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "Content-Type", "application/json");

                connection.setRequestProperty(
                        "Accept", "application/json");

                JSONObject json = new JSONObject();

                json.put("login", email);
                json.put("password", password);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                int responseCode = connection.getResponseCode();

                InputStream inputStream;

                if (responseCode >= 200 && responseCode < 300) {
                    inputStream = connection.getInputStream();
                } else {
                    inputStream = connection.getErrorStream();
                }

                StringBuilder responseBuilder = new StringBuilder();

                if (inputStream != null) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream))) {

                        String line;

                        while ((line = reader.readLine()) != null) {
                            responseBuilder.append(line);
                        }
                    }
                }

                String responseText = responseBuilder.toString();

                JSONObject response = new JSONObject(responseText);

                // Check actual HTTP success first.
                if (responseCode >= 200 && responseCode < 300) {

                    boolean success = response.optBoolean("status", false);

                    // If the API uses a different success field,
                    // we will adjust this after checking Postman response.

                    if (success) {
                        String message = response.optString(
                                "message", "Login Successfully");

                        mainHandler.post(() ->
                                callback.onSuccess(message, response));

                    } else {
                        String message = response.optString(
                                "message", "Login failed");

                        mainHandler.post(() ->
                                callback.onError(message));
                    }

                } else {

                    String message = response.optString(
                            "message", "Login failed. Please try again.");

                    mainHandler.post(() ->
                            callback.onError(message));
                }

            } catch (Exception e) {

                String error = e.getMessage() != null
                        ? e.getMessage()
                        : "Something went wrong";

                mainHandler.post(() ->
                        callback.onError(error));

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
    public void shutdown() {
        executor.shutdownNow();
    }
}
