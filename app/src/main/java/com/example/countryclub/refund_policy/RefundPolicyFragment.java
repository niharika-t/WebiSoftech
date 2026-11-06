package com.example.countryclub.refund_policy;

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
import java.nio.charset.StandardCharsets;

public class RefundPolicyFragment extends Fragment {

    private LinearLayout termsContent;
    private TextView txtTitle;
    private TextView txtIntroduction;

    protected RefundPolicyFragment() {
        // Required empty constructor
    }

    public static void load(FragmentActivity activity) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new RefundPolicyFragment()
                )
                .addToBackStack("refund_policy")
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

        loadTermsFromJson();
    }

    private void loadTermsFromJson() {
        try {
            StringBuilder jsonText = new StringBuilder();

            InputStream inputStream = requireContext()
                    .getAssets()
                    .open("refund_policy.json");

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            inputStream,
                            StandardCharsets.UTF_8
                    ))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    jsonText.append(line);
                }
            }

            JSONObject root = new JSONObject(jsonText.toString());

            // Display page title
            txtTitle.setText(
                    root.optString(
                            "pageTitle",
                            root.optString("title", "Terms & Conditions")
                    )
            );

            // Display introduction if available
            txtIntroduction.setText(
                    root.optString("introduction", "")
            );

            JSONArray sections = root.optJSONArray("sections");

            if (sections == null) {
                Log.e("TermsConditions", "Sections array not found");
                return;
            }

            // Clear previous content to prevent duplicates
            termsContent.removeAllViews();

            for (int i = 0; i < sections.length(); i++) {
                JSONObject section = sections.optJSONObject(i);

                if (section == null) {
                    continue;
                }

                String title = section.optString("title", "");

                // Handle content as either an array or a string
                JSONArray paragraphs = section.optJSONArray("content");

                if (paragraphs != null) {
                    addSection(title, paragraphs);
                } else {
                    String content = section.optString("content", "");
                    addSection(title, content);
                }
            }

        } catch (Exception e) {
            Log.e("TermsConditions", "Error loading JSON", e);

            if (isAdded()) {
                Toast.makeText(
                        requireContext(),
                        "Unable to load Terms & Conditions",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    // Handles content stored as a JSON array
    private void addSection(String title, JSONArray paragraphs) {
        addHeading(title);

        for (int i = 0; i < paragraphs.length(); i++) {
            String paragraph = paragraphs.optString(i, "");

            if (!paragraph.trim().isEmpty()) {
                addParagraph(paragraph);
            }
        }
    }

    // Handles content stored as a single string
    private void addSection(String title, String content) {
        addHeading(title);

        if (content != null && !content.trim().isEmpty()) {
            addParagraph(content);
        }
    }

    private void addHeading(String title) {
        if (title == null || title.trim().isEmpty()) {
            return;
        }

        TextView heading = new TextView(requireContext());
        heading.setText(title);
        heading.setTextColor(android.graphics.Color.rgb(32, 33, 36));
        heading.setTextSize(18);
        heading.setTypeface(null, android.graphics.Typeface.BOLD);

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
        TextView description = new TextView(requireContext());

        // Display plain text, not JSON formatting
        description.setText(content);
        description.setTextColor(android.graphics.Color.rgb(96, 101, 109));
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