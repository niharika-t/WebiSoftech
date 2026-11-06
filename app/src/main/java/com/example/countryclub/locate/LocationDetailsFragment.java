
package com.example.countryclub.locate;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.OnBackPressedCallback;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.example.countryclub.R;

public class LocationDetailsFragment extends Fragment {

    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_CATEGORY_NAME = "category_name";

    private String categoryId;

    private ProgressBar progressBar;
    private TextView txtError;
    private RecyclerView recyclerView;

    private LocationDetailsAdapter adapter;
    private final List<LocationItem> locationList = new ArrayList<>();

    public static LocationDetailsFragment newInstance(
            String categoryId, String categoryName) {

        LocationDetailsFragment fragment = new LocationDetailsFragment();

        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY_NAME, categoryName);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_location_details,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressLocationDetails);
        TextView txtCategoryTitle = view.findViewById(R.id.txtDetailsCategory);
        txtError = view.findViewById(R.id.txtLocationDetailsError);
        recyclerView = view.findViewById(R.id.rvLocationDetails);

        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(
                        getViewLifecycleOwner(),
                        new OnBackPressedCallback(true) {
                            @Override
                            public void handleOnBackPressed() {
                                if (getParentFragmentManager()
                                        .getBackStackEntryCount() > 0) {

                                    getParentFragmentManager()
                                            .popBackStack();
                                } else {
                                    setEnabled(false);
                                    requireActivity()
                                            .getOnBackPressedDispatcher()
                                            .onBackPressed();
                                }
                            }
                        }
                );

        Bundle args = getArguments();

        if (args == null) {
            showError("Unable to load location category.");
            return;
        }

        categoryId = args.getString(ARG_CATEGORY_ID);
        String categoryName = args.getString(ARG_CATEGORY_NAME, "Locations");

        txtCategoryTitle.setText(categoryName);

        adapter = new LocationDetailsAdapter(requireContext(), locationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        loadLocationDetails();
    }

    private void loadLocationDetails() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        txtError.setVisibility(View.GONE);

        LocationApiService.getLocationDetails(
                categoryId,
                new LocationApiService.ApiCallback<>() {

                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(JSONArray data) {
                        if (!isAdded()) return;

                        locationList.clear();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject object = data.optJSONObject(i);

                            if (object == null) continue;

                            String status = object.optString("status", "1");
                            String deleted = object.optString("is_deleted", "0");

                            if (!"1".equals(status) || "1".equals(deleted)) {
                                continue;
                            }

                            locationList.add(new LocationItem(
                                    object.optString("id", ""),
                                    object.optString("category_id", ""),
                                    object.optString("title", "Unnamed Resort"),
                                    object.optString("location", ""),
                                    object.optString("address", ""),
                                    object.optString("breakfast", ""),
                                    object.optString("lunch", ""),
                                    object.optString("dinner", ""),
                                    object.optString("image", "")
                            ));
                        }

                        progressBar.setVisibility(View.GONE);

                        if (locationList.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            showError("No locations available in this category yet.");
                        } else {
                            txtError.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded()) return;

                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        showError("Unable to load locations.\n" + message);
                    }
                }
        );
    }

    private void showError(String message) {
        if (txtError != null) {
            txtError.setText(message);
            txtError.setVisibility(View.VISIBLE);
        }
    }
}
