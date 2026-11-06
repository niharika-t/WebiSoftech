
package com.example.countryclub.locate;

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

import com.example.countryclub.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LocateUsFragment extends Fragment {

    private LocationAdapter adapter;
    private ProgressBar progressBar;
    private TextView errorText;

    public LocateUsFragment() {
    }

    public static void load(
            androidx.fragment.app.FragmentActivity activity) {

        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer,
                        new LocateUsFragment())
                .addToBackStack("locate_us")
                .commit();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_locate_us,
                container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        RecyclerView recyclerView =
                view.findViewById(R.id.rvLocations);

        progressBar =
                view.findViewById(R.id.progressLocations);

        errorText =
                view.findViewById(R.id.txtLocationError);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext()));

        adapter = new LocationAdapter(
                this::openCategory);

        recyclerView.setAdapter(adapter);

        loadCategories();
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        errorText.setVisibility(View.GONE);

        LocationApiService.getLocationCategories(
                new LocationApiService.ApiCallback<>() {

                    @Override
                    public void onSuccess(JSONArray data) {
                        if (!isAdded()) return;

                        progressBar.setVisibility(View.GONE);

                        List<LocationCategory> categories =
                                new ArrayList<>();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject object = data.optJSONObject(i);

                            if (object == null) continue;

                            if (!"1".equals(object.optString("status"))
                                    || "1".equals(
                                    object.optString("is_deleted"))) {
                                continue;
                            }

                            categories.add(new LocationCategory(
                                    object.optString("id"),
                                    object.optString("name")
                            ));
                        }

                        adapter.setCategories(categories);

                        if (categories.isEmpty()) {
                            showError("No categories available.");
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded()) return;

                        progressBar.setVisibility(View.GONE);
                        showError("Unable to load categories: " + message);
                    }
                });
    }

    private void openCategory(LocationCategory category) {
        LocationDetailsFragment fragment =
                LocationDetailsFragment.newInstance(
                        category.getId(), category.getName());

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack("location_details")
                .commit();
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}
