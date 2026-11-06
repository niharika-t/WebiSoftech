package com.example.countryclub.fitness;

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

import java.util.ArrayList;
import java.util.List;

public class FitnessFragment extends Fragment {

    private static final String ARG_CATEGORY_ID = "category_id";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView title;
    private TextView message;

    private FitnessCategoryAdapter categoryAdapter;
    private FitnessAdapter eventAdapter;

    private final List<FitnessCategory> categories = new ArrayList<>();
    private final List<Fitness> events = new ArrayList<>();

    private String selectedCategoryId = "";

    public FitnessFragment() {
        // Required empty public constructor.
    }

    // Opens the normal Events screen.
    public static void load(
            androidx.fragment.app.FragmentActivity activity) {

        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new FitnessFragment()
                )
                .addToBackStack(null)
                .commit();
    }

    // Opens Events with a particular category selected.
    public static FitnessFragment newInstance(String categoryId) {

        FitnessFragment fragment = new FitnessFragment();

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
                R.layout.fragment_events,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(
                R.id.eventsRecyclerView
        );

        progressBar = view.findViewById(
                R.id.eventsProgress
        );

        title = view.findViewById(
                R.id.eventsScreenTitle
        );

        message = view.findViewById(
                R.id.eventsMessage
        );

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        String categoryId = getArguments() != null
                ? getArguments().getString(ARG_CATEGORY_ID, "")
                : "";

        selectedCategoryId = categoryId;

        loadCategories(categoryId);
    }

    private void loadCategories(String requestedCategoryId) {

        message.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        FitnessApiService.getEventCategories(
                new FitnessApiService.CategoriesCallback() {

                    @Override
                    public void onSuccess(
                            List<FitnessCategory> result) {

                        if (!isAdded() || getView() == null) {
                            return;
                        }

                        progressBar.setVisibility(View.GONE);

                        categories.clear();

                        if (result != null) {
                            categories.addAll(result);
                        }

                        if (categories.isEmpty()) {
                            showMessage(
                                    "No event categories available."
                            );
                            return;
                        }

                        categoryAdapter = new FitnessCategoryAdapter(
                                categories,
                                FitnessFragment.this::loadEvents
                        );

                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerView.setAdapter(categoryAdapter);

                        // If opened from Home, load that category directly.
                        if (requestedCategoryId != null
                                && !requestedCategoryId.trim().isEmpty()) {

                            FitnessCategory selectedCategory = null;

                            for (FitnessCategory category : categories) {

                                if (requestedCategoryId.equals(
                                        category.getId())) {

                                    selectedCategory = category;
                                    break;
                                }
                            }

                            if (selectedCategory != null) {
                                loadEvents(selectedCategory);
                            } else {
                                showMessage(
                                        "Selected event category was not found."
                                );
                            }
                        }
                    }

                    @Override
                    public void onError(String error) {

                        if (!isAdded() || getView() == null) {
                            return;
                        }

                        progressBar.setVisibility(View.GONE);

                        showMessage(
                                "Unable to load event categories.\n"
                                        + error
                        );
                    }
                }
        );
    }

    private void loadEvents(FitnessCategory category) {

        if (category == null || !isAdded()) {
            return;
        }

        selectedCategoryId = category.getId();

        recyclerView.setVisibility(View.GONE);
        message.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        FitnessApiService.getEventsByCategory(
                selectedCategoryId,
                new FitnessApiService.EventsCallback() {

                    @Override
                    public void onSuccess(List<Fitness> result) {

                        if (!isAdded()
                                || getView() == null
                                || !selectedCategoryId.equals(
                                category.getId())) {
                            return;
                        }

                        progressBar.setVisibility(View.GONE);

                        events.clear();

                        if (result != null) {
                            events.addAll(result);
                        }

                        if (events.isEmpty()) {
                            showMessage(
                                    "No events available in this category."
                            );
                            return;
                        }

                        eventAdapter = new FitnessAdapter(
                                events,
                                FitnessFragment.this::openEventDetails
                        );

                        recyclerView.setAdapter(eventAdapter);
                        recyclerView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(String error) {

                        if (!isAdded()
                                || getView() == null
                                || !selectedCategoryId.equals(
                                category.getId())) {
                            return;
                        }

                        progressBar.setVisibility(View.GONE);

                        showMessage(
                                "Unable to load events.\n" + error
                        );
                    }
                }
        );
    }

    private void openEventDetails(Fitness event) {

        if (!isAdded() || event == null) {
            return;
        }

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        FitnessDetailFragment.newInstance(
                                event.getId(),
                                event.getCategoryId(),
                                event.getTitle(),
                                event.getDescription(),
                                event.getImage(),
                                event.getDate(),
                                event.getLocation()
                        )
                )
                .addToBackStack(null)
                .commit();
    }

    private void showMessage(String text) {

        message.setText(text);
        message.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }
}