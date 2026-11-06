
package com.example.countryclub.subscriptions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.countryclub.R;

import java.util.List;

public class SubscriptionDetailsFragment extends Fragment {

    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_CATEGORY_NAME = "category_name";

    private TextView heading;
    private TextView subtitle;
    private TextView message;
    private ProgressBar progress;
    private RecyclerView recyclerView;

    private SubscriptionApiService apiService;

    public SubscriptionDetailsFragment() {
        // Required empty public constructor.
    }

    public static SubscriptionDetailsFragment newInstance(
            String categoryId,
            String categoryName) {

        SubscriptionDetailsFragment fragment =
                new SubscriptionDetailsFragment();

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
                R.layout.fragment_subscription,
                container,
                false
        );
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

        String categoryId = args == null
                ? null
                : args.getString(ARG_CATEGORY_ID);

        String categoryName = args == null
                ? "Subscription Details"
                : args.getString(
                ARG_CATEGORY_NAME,
                "Subscription Details"
        );

        heading.setText(
                categoryName == null || categoryName.trim().isEmpty()
                        ? "Subscription Details"
                        : categoryName
        );

        subtitle.setText("Membership benefits and information");

        if (categoryId == null || categoryId.trim().isEmpty()) {
            progress.setVisibility(View.GONE);
            showMessage("Subscription category ID is missing.");
            return;
        }

        loadDetails(categoryId);
    }

    private void loadDetails(String categoryId) {

        progress.setVisibility(View.VISIBLE);
        message.setVisibility(View.VISIBLE);
        message.setText("Loading subscription details...");
        recyclerView.setAdapter(null);

        apiService.getDetailsByCategory(
                categoryId,
                new SubscriptionApiService.DetailsCallback() {

                    @Override
                    public void onSuccess(
                            List<SubscriptionDetail> details) {

                        if (!isAdded()) {
                            return;
                        }

                        progress.setVisibility(View.GONE);

                        if (details == null || details.isEmpty()) {
                            showMessage(
                                    "No details available for this membership."
                            );
                            return;
                        }

                        message.setVisibility(View.GONE);

                        recyclerView.setAdapter(
                                new SubscriptionAdapter(details)
                        );
                    }

                    @Override
                    public void onError(String error) {

                        if (!isAdded()) {
                            return;
                        }

                        progress.setVisibility(View.GONE);

                        showMessage(
                                "Unable to load subscription details.\n"
                                        + error
                        );
                    }
                }
        );
    }

    private void showMessage(String text) {
        message.setVisibility(View.VISIBLE);
        message.setText(text);
    }
}
