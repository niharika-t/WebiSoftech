
package com.example.countryclub.trip_destination;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.countryclub.R;

import java.util.ArrayList;
import java.util.List;

public class TripDestinationFragment extends Fragment {

    private static final String TAG = "TripDestinationFragment";
    private static final String ARG_CATEGORY_ID = "category_id";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;

    private TripDestinationApiService apiService;
    private TripDestinationAdapter adapter;

    private final List<TripDestination> destinations = new ArrayList<>();

    // True when displaying categories; false when displaying destinations.
    private boolean showingCategories = false;

    public TripDestinationFragment() {
        // Required empty public constructor
    }

    // Open fragment with a selected category ID from HomeFragment.
    public static TripDestinationFragment newInstance(String categoryId) {

        TripDestinationFragment fragment = new TripDestinationFragment();

        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);

        fragment.setArguments(args);

        return fragment;
    }

    // Open fragment without an ID from the navigation drawer.
    public static void load(FragmentActivity activity) {

        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new TripDestinationFragment()
                )
                .addToBackStack("trip_destination")
                .commit();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_trip_destination,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rvTripDetails);
        progressBar = view.findViewById(R.id.progressTripDetails);
        emptyText = view.findViewById(R.id.tvTripDetailsEmpty);

        apiService = new TripDestinationApiService();

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        recyclerView.setHasFixedSize(false);

        adapter = new TripDestinationAdapter(
                destinations,
                this::handleItemClick
        );

        recyclerView.setAdapter(adapter);

        String categoryId = getArguments() != null
                ? getArguments().getString(ARG_CATEGORY_ID)
                : null;

        Log.d(TAG, "Received category ID: " + categoryId);

        if (categoryId == null || categoryId.trim().isEmpty()) {

            // Drawer navigation: show all destination categories.
            loadDestinationCategories();

        } else {

            // Home category selection: show destinations of selected category.
            loadDestinationDetails(categoryId.trim());
        }
    }

    // ---------------------------------------------
    // LOAD ALL DESTINATION CATEGORIES
    // ---------------------------------------------

    private void loadDestinationCategories() {

        showingCategories = true;

        showLoading();

        Log.d(TAG, "Loading all destination categories");

        apiService.getCategories(
                new TripDestinationApiService.CategoriesCallback() {

                    @Override
                    public void onSuccess(List<TripDestination> result) {

                        if (!isAdded() || getView() == null) return;

                        progressBar.setVisibility(View.GONE);

                        destinations.clear();

                        if (result != null) {
                            destinations.addAll(result);
                        }

                        Log.d(TAG, "Categories received: "
                                + destinations.size());

                        if (destinations.isEmpty()) {

                            showEmptyMessage(
                                    "No trip destination categories available."
                            );

                            return;
                        }

                        adapter.notifyDataSetChanged();

                        recyclerView.setVisibility(View.VISIBLE);
                        emptyText.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(String message) {

                        if (!isAdded() || getView() == null) return;

                        Log.e(TAG, "Category API error: " + message);

                        showEmptyMessage(
                                "Unable to load destination categories.\n"
                                        + message
                        );
                    }
                }
        );
    }

    // ---------------------------------------------
    // LOAD DESTINATIONS BY CATEGORY ID
    // ---------------------------------------------

    private void loadDestinationDetails(String categoryId) {

        showingCategories = false;

        showLoading();

        Log.d(TAG, "Loading details for category: " + categoryId);

        apiService.getDetailsByCategory(
                categoryId,
                new TripDestinationApiService.DetailsCallback() {

                    @Override
                    public void onSuccess(List<TripDestination> result) {

                        if (!isAdded() || getView() == null) return;

                        progressBar.setVisibility(View.GONE);

                        destinations.clear();

                        if (result != null) {
                            destinations.addAll(result);
                        }

                        Log.d(TAG, "Destination details received: "
                                + destinations.size());

                        if (destinations.isEmpty()) {

                            showEmptyMessage(
                                    "No destination details available."
                            );

                            return;
                        }

                        adapter.notifyDataSetChanged();

                        recyclerView.setVisibility(View.VISIBLE);
                        emptyText.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(String message) {

                        if (!isAdded() || getView() == null) return;

                        Log.e(TAG, "Destination details error: " + message);

                        showEmptyMessage(
                                "Unable to load destination details.\n"
                                        + message
                        );

                        Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // ---------------------------------------------
    // HANDLE CATEGORY / DESTINATION ITEM CLICK
    // ---------------------------------------------

    private void handleItemClick(TripDestination item) {

        if (!isAdded() || item == null) return;

        if (showingCategories) {

            // Category selected: load its destinations.
            if (item.getId() == null || item.getId().trim().isEmpty()) {
                Toast.makeText(
                        requireContext(),
                        "Invalid category ID",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            loadDestinationDetails(item.getId().trim());

        } else {

            // Destination selected: open its details screen.
            openDetails(item);
        }
    }

    // ---------------------------------------------
    // OPEN DESTINATION DETAILS SCREEN
    // ---------------------------------------------

    private void openDetails(TripDestination destination) {

        if (!isAdded() || destination == null) return;

        Log.d(TAG, "Opening destination: " + destination.getId());

        TripDestinationDetailsFragment fragment =
                TripDestinationDetailsFragment.newInstance(
                        destination.getId()
                );

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack("trip_destination_details")
                .commit();
    }

    // ---------------------------------------------
    // LOADING
    // ---------------------------------------------

    private void showLoading() {

        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    // ---------------------------------------------
    // EMPTY / ERROR MESSAGE
    // ---------------------------------------------

    private void showEmptyMessage(String message) {

        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        emptyText.setText(message);
        emptyText.setVisibility(View.VISIBLE);
    }
}
