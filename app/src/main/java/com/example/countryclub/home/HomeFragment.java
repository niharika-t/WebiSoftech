package com.example.countryclub.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.countryclub.R;
import com.example.countryclub.fitness.FitnessApiService;
import com.example.countryclub.fitness.FitnessCategory;
import com.example.countryclub.fitness.FitnessFragment;
import com.example.countryclub.resorts.ResortCategory;
import com.example.countryclub.resorts.ResortApiService;
import com.example.countryclub.resorts.ResortsFragment;
import com.example.countryclub.trip_destination.TripDestination;
import com.example.countryclub.trip_destination.TripDestinationApiService;
import com.example.countryclub.trip_destination.TripDestinationFragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private static final String IMAGE_BASE_URL =
            "https://holidayserviceclub.com/HolidayServiceClub/public/";

    private RecyclerView rvHomeResorts;
    private RecyclerView rvHomeEvents;
    private RecyclerView rvHomeDestinations;

    private ProgressBar progressHomeResorts;
    private ProgressBar progressHomeEvents;
    private ProgressBar progressHomeDestinations;

    private TextView tvHomeResortMessage;
    private TextView tvHomeEventMessage;
    private TextView tvHomeDestinationMessage;

    private HomeCategoryAdapter resortAdapter;
    private HomeCategoryAdapter eventAdapter;
    private HomeCategoryAdapter destinationAdapter;

    private final List<HomeCategory> resortCategories = new ArrayList<>();
    private final List<HomeCategory> eventCategories = new ArrayList<>();
    private final List<HomeCategory> destinationCategories = new ArrayList<>();

    private TripDestinationApiService tripDestinationApiService;

    private ViewPager2 homeImageCarousel;

    private HomeCarouselAdapter carouselAdapter;

    private final Handler carouselHandler =
            new Handler(Looper.getMainLooper());

    private final Runnable carouselRunnable = new Runnable() {
        @Override
        public void run() {

            if (homeImageCarousel == null ||
                    homeImageCarousel.getAdapter() == null) {
                return;
            }

            int currentItem = homeImageCarousel.getCurrentItem();
            int totalItems = homeImageCarousel.getAdapter().getItemCount();

            int nextItem = (currentItem + 1) % totalItems;

            homeImageCarousel.setCurrentItem(nextItem, true);

            carouselHandler.postDelayed(this, 3500);
        }
    };

    public HomeFragment() {
        // Required empty public constructor.
    }

    public static void load(FragmentActivity activity) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new HomeFragment())
                .commit();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_home,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        homeImageCarousel = view.findViewById(R.id.homeImageCarousel);

        carouselAdapter = new HomeCarouselAdapter();

        homeImageCarousel.setAdapter(carouselAdapter);

        rvHomeResorts = view.findViewById(R.id.rvHomeResorts);
        rvHomeEvents = view.findViewById(R.id.rvHomeEvents);
        rvHomeDestinations = view.findViewById(R.id.rvHomeDestinations);

        progressHomeResorts = view.findViewById(R.id.progressHomeResorts);
        progressHomeEvents = view.findViewById(R.id.progressHomeEvents);
        progressHomeDestinations = view.findViewById(R.id.progressHomeDestinations);

        tvHomeResortMessage = view.findViewById(R.id.tvHomeResortMessage);
        tvHomeEventMessage = view.findViewById(R.id.tvHomeEventMessage);
        tvHomeDestinationMessage = view.findViewById(R.id.tvHomeDestinationMessage);

        tripDestinationApiService = new TripDestinationApiService();

        setupRecyclerViews();

        loadResortCategories();
        loadEventCategories();
        loadTripDestinationCategories();
    }

    // ---------------- RECYCLER VIEWS ----------------

    private void setupRecyclerViews() {

        rvHomeResorts.setLayoutManager(
                new LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        rvHomeEvents.setLayoutManager(
                new LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        rvHomeDestinations.setLayoutManager(
                new LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        rvHomeResorts.setNestedScrollingEnabled(false);
        rvHomeEvents.setNestedScrollingEnabled(false);
        rvHomeDestinations.setNestedScrollingEnabled(false);

        resortAdapter = new HomeCategoryAdapter(
                resortCategories,
                this::openResortCategory
        );

        eventAdapter = new HomeCategoryAdapter(
                eventCategories,
                this::openEventCategory
        );

        destinationAdapter = new HomeCategoryAdapter(
                destinationCategories,
                this::openTripDestinationCategory
        );

        rvHomeResorts.setAdapter(resortAdapter);
        rvHomeEvents.setAdapter(eventAdapter);
        rvHomeDestinations.setAdapter(destinationAdapter);

        rvHomeResorts.setVisibility(View.GONE);
        rvHomeEvents.setVisibility(View.GONE);
        rvHomeDestinations.setVisibility(View.GONE);

        tvHomeResortMessage.setVisibility(View.GONE);
        tvHomeEventMessage.setVisibility(View.GONE);
        tvHomeDestinationMessage.setVisibility(View.GONE);
    }

    // ---------------- IMAGE URL HELPER ----------------

    private String getFullImageUrl(String image) {

        if (image == null || image.trim().isEmpty()
                || "null".equalsIgnoreCase(image.trim())) {
            return "";
        }

        image = image.trim();

        if (image.startsWith("http://")
                || image.startsWith("https://")) {
            return image;
        }

        image = image.replaceFirst("^/+", "");

        if (image.startsWith("public/")) {
            image = image.substring("public/".length());
        }

        return IMAGE_BASE_URL + image;
    }

    // ---------------- RESORT CATEGORIES ----------------

    private void loadResortCategories() {

        progressHomeResorts.setVisibility(View.VISIBLE);
        tvHomeResortMessage.setVisibility(View.GONE);
        rvHomeResorts.setVisibility(View.GONE);

        ResortApiService.getResortCategories(
                new ResortApiService.ApiCallback<>() {

                    @Override
                    public void onSuccess(JSONArray data) {

                        if (!isAdded() || getView() == null) return;

                        progressHomeResorts.setVisibility(View.GONE);
                        resortCategories.clear();

                        for (int i = 0; i < data.length(); i++) {

                            JSONObject object = data.optJSONObject(i);

                            if (object == null) continue;

                            String id = getValue(object, "id", "category_id");
                            String name = getValue(
                                    object, "name", "category_name", "title");
                            String image = getValue(
                                    object, "image", "image_url", "category_image");

                            image = getFullImageUrl(image);

                            if (!id.isEmpty() && !name.isEmpty()) {
                                resortCategories.add(
                                        new HomeCategory(
                                                id,
                                                name,
                                                HomeCategory.TYPE_RESORT,
                                                image
                                        )
                                );
                            }
                        }

                        resortAdapter.notifyDataSetChanged();

                        if (resortCategories.isEmpty()) {
                            showResortMessage("No resort categories available.");
                        } else {
                            rvHomeResorts.setVisibility(View.VISIBLE);
                        }

                        Log.d(TAG, "Loaded resort categories: "
                                + resortCategories.size());
                    }

                    @Override
                    public void onError(String message) {

                        if (!isAdded() || getView() == null) return;

                        progressHomeResorts.setVisibility(View.GONE);
                        showResortMessage("Unable to load resort categories.");

                        Log.e(TAG, "Resort API error: " + message);
                    }
                }
        );
    }

    // ---------------- EVENT CATEGORIES ----------------

    private void loadEventCategories() {

        progressHomeEvents.setVisibility(View.VISIBLE);
        tvHomeEventMessage.setVisibility(View.GONE);
        rvHomeEvents.setVisibility(View.GONE);

        FitnessApiService.getEventCategories(
                new FitnessApiService.CategoriesCallback() {

                    @Override
                    public void onSuccess(List<FitnessCategory> result) {

                        if (!isAdded() || getView() == null) return;

                        progressHomeEvents.setVisibility(View.GONE);
                        eventCategories.clear();

                        if (result != null) {

                            for (FitnessCategory category : result) {

                                if (category == null) continue;

                                String id = category.getId();
                                String name = category.getName();
                                String image = getFullImageUrl(category.getImage());

                                if (id != null && !id.trim().isEmpty()
                                        && name != null && !name.trim().isEmpty()) {

                                    eventCategories.add(
                                            new HomeCategory(
                                                    id,
                                                    name,
                                                    HomeCategory.TYPE_EVENT,
                                                    image
                                            )
                                    );
                                }
                            }
                        }

                        eventAdapter.notifyDataSetChanged();

                        if (eventCategories.isEmpty()) {
                            showEventMessage("No event categories available.");
                        } else {
                            rvHomeEvents.setVisibility(View.VISIBLE);
                        }

                        Log.d(TAG, "Loaded event categories: "
                                + eventCategories.size());
                    }

                    @Override
                    public void onError(String error) {

                        if (!isAdded() || getView() == null) return;

                        progressHomeEvents.setVisibility(View.GONE);
                        showEventMessage("Unable to load event categories.");

                        Log.e(TAG, "Event API error: " + error);
                    }
                }
        );
    }

    // ---------------- TRIP DESTINATION CATEGORIES ----------------

    private void loadTripDestinationCategories() {

        progressHomeDestinations.setVisibility(View.VISIBLE);
        tvHomeDestinationMessage.setVisibility(View.GONE);
        rvHomeDestinations.setVisibility(View.GONE);

        tripDestinationApiService.getCategories(
                new TripDestinationApiService.CategoriesCallback() {

                    @Override
                    public void onSuccess(List<TripDestination> result) {

                        if (!isAdded() || getView() == null) return;

                        progressHomeDestinations.setVisibility(View.GONE);
                        destinationCategories.clear();

                        if (result != null) {

                            for (TripDestination destination : result) {

                                if (destination == null) continue;

                                String id = destination.getId();
                                String name = destination.getTitle();
                                String image = getFullImageUrl(
                                        destination.getImage());

                                if (id != null && !id.trim().isEmpty()
                                        && name != null && !name.trim().isEmpty()) {

                                    destinationCategories.add(
                                            new HomeCategory(
                                                    id,
                                                    name,
                                                    HomeCategory.TYPE_DESTINATION,
                                                    image
                                            )
                                    );
                                }
                            }
                        }

                        destinationAdapter.notifyDataSetChanged();

                        if (destinationCategories.isEmpty()) {
                            showDestinationMessage(
                                    "No trip destinations available.");
                        } else {
                            rvHomeDestinations.setVisibility(View.VISIBLE);
                        }

                        Log.d(TAG, "Loaded trip destination categories: "
                                + destinationCategories.size());
                    }

                    @Override
                    public void onError(String message) {

                        if (!isAdded() || getView() == null) return;

                        progressHomeDestinations.setVisibility(View.GONE);

                        showDestinationMessage(
                                "Unable to load trip destinations.");

                        Log.e(TAG, "Trip Destination API error: " + message);
                    }
                }
        );
    }

    // ---------------- NAVIGATION ----------------

    private void openResortCategory(HomeCategory category) {

        if (!isAdded() || category == null) return;

        ResortCategory resortCategory = new ResortCategory(
                category.getId(),
                category.getName(),
                category.getImage()
        );

        ResortsFragment.openCategory(this, resortCategory);
    }

    private void openEventCategory(HomeCategory category) {

        if (!isAdded() || category == null) return;

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        FitnessFragment.newInstance(category.getId())
                )
                .addToBackStack(null)
                .commit();
    }

    private void openTripDestinationCategory(HomeCategory category) {

        if (!isAdded() || category == null) return;

        TripDestinationFragment fragment =
                TripDestinationFragment.newInstance(category.getId());

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack("trip_destination")
                .commit();
    }

    // ---------------- JSON HELPER ----------------

    private String getValue(JSONObject object, String... keys) {

        for (String key : keys) {

            String value = object.optString(key, "").trim();

            if (!value.isEmpty() && !"null".equalsIgnoreCase(value)) {
                return value;
            }
        }

        return "";
    }

    // ---------------- MESSAGE HELPERS ----------------

    private void showResortMessage(String message) {
        tvHomeResortMessage.setText(message);
        tvHomeResortMessage.setVisibility(View.VISIBLE);
        rvHomeResorts.setVisibility(View.GONE);
    }

    private void showEventMessage(String message) {
        tvHomeEventMessage.setText(message);
        tvHomeEventMessage.setVisibility(View.VISIBLE);
        rvHomeEvents.setVisibility(View.GONE);
    }

    private void showDestinationMessage(String message) {
        tvHomeDestinationMessage.setText(message);
        tvHomeDestinationMessage.setVisibility(View.VISIBLE);
        rvHomeDestinations.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (homeImageCarousel != null) {
            carouselHandler.removeCallbacks(carouselRunnable);
            carouselHandler.postDelayed(carouselRunnable, 3500);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        carouselHandler.removeCallbacks(carouselRunnable);
    }

    @Override
    public void onDestroyView() {
        carouselHandler.removeCallbacks(carouselRunnable);

        homeImageCarousel = null;
        carouselAdapter = null;

        super.onDestroyView();
    }
}
