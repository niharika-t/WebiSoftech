package com.example.countryclub.registration;

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

public class RegistrationApiService {

    private static final String API_URL =
            "https://www.holidayserviceclub.com/HolidayServiceClub/public/api/register";

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public interface RegistrationCallback {
        void onSuccess(String message, JSONObject response);
        void onError(String message);
    }

    public void registerUser(
            JSONObject requestBody,
            RegistrationCallback callback) {

        executor.execute(() -> {

            HttpURLConnection connection = null;

            try {
                URL url = new URL(API_URL);
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                // Send registration JSON
                try (OutputStream outputStream =
                             connection.getOutputStream()) {

                    outputStream.write(
                            requestBody.toString().getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );
                    outputStream.flush();
                }

                int httpCode = connection.getResponseCode();

                InputStream inputStream =
                        (httpCode >= 200 && httpCode < 300)
                                ? connection.getInputStream()
                                : connection.getErrorStream();

                StringBuilder responseBuilder = new StringBuilder();

                if (inputStream != null) {
                    try (BufferedReader reader =
                                 new BufferedReader(
                                         new InputStreamReader(
                                                 inputStream,
                                                 StandardCharsets.UTF_8
                                         ))) {

                        String line;

                        while ((line = reader.readLine()) != null) {
                            responseBuilder.append(line);
                        }
                    }
                }

                String responseText = responseBuilder.toString();

                JSONObject response =
                        new JSONObject(responseText);

                // Extract backend error message
                String errorMessage = "Registration failed.";

                JSONObject messages =
                        response.optJSONObject("messages");

                if (messages != null) {
                    errorMessage = messages.optString(
                            "error",
                            errorMessage
                    );
                }

                int apiStatus = response.optInt("status", httpCode);

                boolean failed =
                        httpCode < 200 ||
                                httpCode >= 300 ||
                                apiStatus >= 400;

                if (failed) {

                    String finalError = errorMessage;

                    mainHandler.post(() ->
                            callback.onError(finalError)
                    );

                } else {

                    String successMessage = response.optString(
                            "message",
                            "Registration successful!"
                    );

                    String finalMessage = successMessage;

                    mainHandler.post(() ->
                            callback.onSuccess(
                                    finalMessage,
                                    response
                            )
                    );
                }

            } catch (Exception e) {

                String error = e.getMessage();

                if (error == null || error.trim().isEmpty()) {
                    error = "Unable to connect to the server. Please try again.";
                }

                String finalError = error;

                mainHandler.post(() ->
                        callback.onError(finalError)
                );

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
