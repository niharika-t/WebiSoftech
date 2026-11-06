
package com.example.countryclub.subscriptions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.countryclub.R;
import com.example.countryclub.resorts.ResortsFragment;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionFragment extends Fragment {

    private TextView heading;
    private TextView subtitle;
    private TextView message;
    private ProgressBar progress;
    private RecyclerView recyclerView;

    private SubscriptionApiService apiService;

    private List<SubscriptionCategory> categories =
            new ArrayList<>();

    public SubscriptionFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_subscription,
                container,
                false
        );
    }

    public static void load(FragmentActivity activity) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new SubscriptionFragment()
                )
                .commit();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        heading = view.findViewById(R.id.tvSubscriptionHeading);
        subtitle = view.findViewById(R.id.tvSubscriptionSubtitle);
        progress = view.findViewById(R.id.progressSubscription);
        message = view.findViewById(R.id.tvSubscriptionMessage);
        recyclerView = view.findViewById(R.id.rvSubscriptions);

        apiService = new SubscriptionApiService();

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        recyclerView.setHasFixedSize(false);

        loadCategories();
    }

    private void loadCategories() {

        subtitle.setText("Explore our membership experiences");

        showLoading("Loading subscription categories...");

        apiService.getCategories(
                new SubscriptionApiService.CategoriesCallback() {

                    @Override
                    public void onSuccess(
                            List<SubscriptionCategory> result) {

                        if (!isAdded()) {
                            return;
                        }

                        categories = result == null
                                ? new ArrayList<>()
                                : new ArrayList<>(result);

                        progress.setVisibility(View.GONE);

                        if (categories.isEmpty()) {
                            recyclerView.setAdapter(null);
                            showMessage(
                                    "No subscription categories available."
                            );
                            return;
                        }

                        message.setVisibility(View.GONE);

                        SubscriptionAdapter adapter =
                                new SubscriptionAdapter(
                                        categories,
                                        SubscriptionFragment.this
                                                ::openSubscriptionDetails
                                );

                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onError(String error) {

                        if (!isAdded()) {
                            return;
                        }

                        progress.setVisibility(View.GONE);
                        recyclerView.setAdapter(null);

                        showMessage(
                                "Unable to load subscription categories.\n"
                                        + error
                        );
                    }
                }
        );
    }

    private void openSubscriptionDetails(
            SubscriptionCategory category) {

        if (category == null || !isAdded()) {
            return;
        }

        SubscriptionDetailsFragment fragment =
                SubscriptionDetailsFragment.newInstance(
                        category.getId(),
                        category.getTitle()
                );

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack("subscription_details")
                .commit();
    }

    private void showLoading(String text) {

        progress.setVisibility(View.VISIBLE);
        message.setVisibility(View.VISIBLE);
        message.setText(text);

        recyclerView.setAdapter(null);
    }

    private void showMessage(String text) {

        message.setVisibility(View.VISIBLE);
        message.setText(text);
    }
}
