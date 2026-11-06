
package com.example.countryclub.terms_and_conditions;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.countryclub.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TermsAndConditionFragment extends Fragment {

    private static final String TAG = "TermsConditions";

    private static final String API_URL =
            "https://holidayserviceclub.com/HolidayServiceClub/public/api/get-terms-conditions";

    private LinearLayout termsContent;
    private TextView txtTitle;
    private TextView txtIntroduction;

    public TermsAndConditionFragment() {
        // Required empty constructor
    }

    public static void load(FragmentActivity activity) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new TermsAndConditionFragment()
                )
                .addToBackStack("terms_conditions")
                .commit();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_terms_condition,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        termsContent = view.findViewById(R.id.termsContent);
        txtTitle = view.findViewById(R.id.txtTermsTitle);
        txtIntroduction = view.findViewById(R.id.txtTermsIntroduction);

        txtTitle.setText("Terms & Conditions");
        txtIntroduction.setText("Loading terms and conditions...");
        termsContent.removeAllViews();

        loadTermsFromApi();
    }

    private void loadTermsFromApi() {
        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(API_URL);
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                int responseCode = connection.getResponseCode();

                InputStream inputStream;

                if (responseCode >= 200 && responseCode < 300) {
                    inputStream = connection.getInputStream();
                } else {
                    inputStream = connection.getErrorStream();
                }

                if (inputStream == null) {
                    throw new Exception(
                            "Empty API response. HTTP " + responseCode
                    );
                }

                StringBuilder response = new StringBuilder();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                inputStream,
                                StandardCharsets.UTF_8
                        ))) {

                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                if (responseCode < 200 || responseCode >= 300) {
                    throw new Exception(
                            "API returned HTTP " + responseCode
                                    + ": " + response
                    );
                }

                JSONObject root = new JSONObject(response.toString());

                // Support APIs that wrap their response inside "data".
                JSONObject data = root.optJSONObject("data");

                if (data != null) {
                    root = data;
                }

                final JSONObject result = root;

                if (getActivity() == null) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || getView() == null) {
                        return;
                    }

                    displayTerms(result);
                });

            } catch (Exception e) {
                Log.e(TAG, "Error fetching terms from API", e);

                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        if (!isAdded() || getView() == null) {
                            return;
                        }

                        txtIntroduction.setText(
                                "Unable to load Terms & Conditions. "
                                        + "Please try again later."
                        );

                        termsContent.removeAllViews();

                        Toast.makeText(
                                requireContext(),
                                "Failed to load Terms & Conditions",
                                Toast.LENGTH_LONG
                        ).show();
                    });
                }

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }


    private void displayTerms(JSONObject root) {

        // Set page title
        txtTitle.setText("Terms & Conditions");

        // This API returns data as a JSONArray
        JSONArray termsArray = root.optJSONArray("data");

        termsContent.removeAllViews();

        if (termsArray == null || termsArray.length() == 0) {
            txtIntroduction.setText(
                    "No terms and conditions are available."
            );
            Log.e(TAG, "Terms data array is missing or empty");
            return;
        }

        txtIntroduction.setText(
                "Please read the following terms and conditions carefully."
        );

        for (int i = 0; i < termsArray.length(); i++) {

            JSONObject term = termsArray.optJSONObject(i);

            if (term == null) {
                continue;
            }

            // Display only active, non-deleted terms
            if (!"1".equals(term.optString("status", "1"))
                    || !"0".equals(term.optString("is_deleted", "0"))) {
                continue;
            }

            String title = term.optString("title", "").trim();
            String description = term.optString(
                    "description", ""
            ).trim();

            // Add section heading
            if (!title.isEmpty()) {
                addHeading(title);
            }

            // Add section description
            if (!description.isEmpty()) {
                addParagraph(description);
            }
        }
    }

    private void addHeading(String title) {
        if (title == null || title.trim().isEmpty()) {
            return;
        }

        TextView heading = new TextView(requireContext());
        heading.setText(title);
        heading.setTextColor(Color.rgb(32, 33, 36));
        heading.setTextSize(18);
        heading.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(12);
        params.bottomMargin = dp(6);

        termsContent.addView(heading, params);
    }

    private void addParagraph(String content) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }

        TextView description = new TextView(requireContext());
        description.setText(content);
        description.setTextColor(Color.rgb(96, 101, 109));
        description.setTextSize(15);
        description.setLineSpacing(dp(3), 1f);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dp(8);

        termsContent.addView(description, params);
    }

    private int dp(int value) {
        return (int) (value * requireContext()
                .getResources()
                .getDisplayMetrics()
                .density);
    }
}
