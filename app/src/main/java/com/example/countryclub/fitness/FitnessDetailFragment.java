package com.example.countryclub.fitness;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FitnessDetailFragment extends Fragment {

    private static final ExecutorService imageExecutor =
            Executors.newSingleThreadExecutor();

    public FitnessDetailFragment() {
    }

    public static FitnessDetailFragment newInstance(
            String id,
            String categoryId,
            String title,
            String description,
            String image,
            String date,
            String location) {

        FitnessDetailFragment fragment = new FitnessDetailFragment();

        Bundle args = new Bundle();
        args.putString("event_id", id);
        args.putString("category_id", categoryId);
        args.putString("event_title", title);
        args.putString("event_description", description);
        args.putString("event_image", image);
        args.putString("event_date", date);
        args.putString("event_location", location);

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
                R.layout.fragment_event_detail, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) return;

        ImageView image = view.findViewById(R.id.eventDetailImage);
        TextView title = view.findViewById(R.id.eventDetailTitle);
        TextView date = view.findViewById(R.id.eventDetailDate);
        TextView location = view.findViewById(R.id.eventDetailLocation);
        TextView description =
                view.findViewById(R.id.eventDetailDescription);

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

        title.setText(args.getString("event_title", "Event"));
        date.setText(args.getString("event_date", "").isEmpty()
                ? "Date to be announced"
                : args.getString("event_date"));
        location.setText(args.getString("event_location", "").isEmpty()
                ? "Location to be announced"
                : args.getString("event_location"));
        description.setText(
                args.getString("event_description", "Details coming soon."));

        String imageUrl = args.getString("event_image", "");
        image.setTag(imageUrl);
        image.setImageResource(android.R.drawable.ic_menu_gallery);

        if (!imageUrl.isEmpty()) {
            imageExecutor.execute(() -> {
                try (InputStream stream = new URL(imageUrl).openStream()) {
                    Bitmap bitmap = BitmapFactory.decodeStream(stream);

                    if (isAdded()) {
                        image.post(() -> {
                            if (imageUrl.equals(image.getTag())
                                    && bitmap != null) {
                                image.setImageBitmap(bitmap);
                            }
                        });
                    }
                } catch (Exception ignored) {
                }
            });
        }

    }
}