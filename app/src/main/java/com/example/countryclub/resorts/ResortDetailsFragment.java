
package com.example.countryclub.resorts;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.activity.OnBackPressedCallback;


import com.example.countryclub.R;

public class ResortDetailsFragment extends Fragment {

    private static final String ARG_ID = "resort_id";
    private static final String ARG_TITLE = "resort_title";
    private static final String ARG_DESCRIPTION = "resort_description";
    private static final String ARG_IMAGE = "resort_image";

    public ResortDetailsFragment() {
        // Required empty constructor
    }

    public static void open(
            Fragment parent,
            Resort resort) {

        Bundle args = new Bundle();
        args.putString(ARG_ID, resort.getId());
        args.putString(ARG_TITLE, resort.getTitle());
        args.putString(ARG_DESCRIPTION, resort.getDescription());
        args.putString(ARG_IMAGE, resort.getImage());

        ResortDetailsFragment fragment =
                new ResortDetailsFragment();

        fragment.setArguments(args);

        parent.getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_resort_details,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        ImageView ivResortDetailImage =
                view.findViewById(R.id.ivResortDetailImage);

        TextView tvResortDetailTitle =
                view.findViewById(R.id.tvResortDetailTitle);

        TextView tvResortDetailLocation =
                view.findViewById(R.id.tvResortDetailLocation);

        TextView tvResortDetailDescription =
                view.findViewById(R.id.tvResortDetailDescription);

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
            tvResortDetailTitle.setText("Resort details unavailable");
            tvResortDetailLocation.setVisibility(View.GONE);
            return;
        }

        String title = args.getString(ARG_TITLE, "Resort");
        String description = args.getString(ARG_DESCRIPTION, "");
        String image = args.getString(ARG_IMAGE, "");

        tvResortDetailTitle.setText(title);

        // The supplied JSON has no location field.
        tvResortDetailLocation.setVisibility(View.GONE);

        if (description.trim().isEmpty()
                || "null".equalsIgnoreCase(description.trim())) {
            tvResortDetailDescription.setText(
                    "No description available."
            );
        } else {
            tvResortDetailDescription.setText(
                    Html.fromHtml(
                            description,
                            Html.FROM_HTML_MODE_COMPACT
                    )
            );
        }

        ResortImageLoader.load(ivResortDetailImage, image);
    }
}
