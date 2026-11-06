
package com.example.countryclub.trip_destination;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.OnBackPressedCallback;

import com.example.countryclub.R;

import java.util.ArrayList;
import java.util.List;

public class TripDestinationDetailsFragment extends Fragment {

    private static final String ARG_CATEGORY_ID = "category_id";

    private String categoryId;

    private TextView titleText;
    private TextView descriptionText;
    private TextView emptyText;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    private TripDestinationApiService apiService;

    public TripDestinationDetailsFragment() {
        // Required empty constructor.
    }

    public static TripDestinationDetailsFragment newInstance(
            String categoryId) {

        TripDestinationDetailsFragment fragment =
                new TripDestinationDetailsFragment();

        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);
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
                R.layout.fragment_trip_destination_details,
                container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        categoryId = getArguments() != null
                ? getArguments().getString(ARG_CATEGORY_ID, "")
                : "";

        titleText = view.findViewById(R.id.tvTripDetailTitle);
        descriptionText = view.findViewById(R.id.tvTripCategoryDescription);
        emptyText = view.findViewById(R.id.tvTripDetailsEmpty);
        progressBar = view.findViewById(R.id.progressTripDetails);
        recyclerView = view.findViewById(R.id.rvTripDetails);

        apiService = new TripDestinationApiService();

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext()));

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

        loadCategory();
        loadDestinations();
    }

    private void loadCategory() {
        apiService.getCategoryById(categoryId,
                new TripDestinationApiService.CategoryCallback() {
                    @Override
                    public void onSuccess(TripDestination category) {
                        if (!isAdded()) return;

                        titleText.setText("📍 " + category.getTitle());

                        if (category.getDescription().isEmpty()) {
                            descriptionText.setVisibility(View.GONE);
                        } else {
                            descriptionText.setText(category.getDescription());
                            descriptionText.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded()) return;

                        titleText.setText("🌍 Trip Destinations");
                        Toast.makeText(requireContext(),
                                "Category info: " + message,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadDestinations() {
        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        apiService.getDetailsByCategory(categoryId,
                new TripDestinationApiService.DetailsCallback() {
                    @Override
                    public void onSuccess(List<TripDestination> details) {
                        if (!isAdded()) return;

                        progressBar.setVisibility(View.GONE);

                        if (details.isEmpty()) {
                            emptyText.setText(
                                    "🌴 No destinations available in this category yet.");
                            emptyText.setVisibility(View.VISIBLE);
                            return;
                        }

                        recyclerView.setAdapter(new TripDestinationAdapter(
                                new ArrayList<>(details),
                                item -> showDestination(item)));

                        recyclerView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded()) return;

                        progressBar.setVisibility(View.GONE);
                        emptyText.setText("Unable to load destinations.\n" + message);
                        emptyText.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void showDestination(TripDestination item) {
        String message = item.getTitle();

        if (!item.getDescription().isEmpty()) {
            message += "\n\n" + item.getDescription();
        }

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("🌴 " + item.getTitle())
                .setMessage(message)
                .setPositiveButton("Close", null)
                .show();
    }
}
