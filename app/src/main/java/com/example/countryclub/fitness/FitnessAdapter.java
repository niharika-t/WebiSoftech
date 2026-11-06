
package com.example.countryclub.fitness;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.countryclub.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FitnessAdapter extends RecyclerView.Adapter<FitnessAdapter.EventViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Fitness event);
    }

    private final List<Fitness> eventList;
    private final OnEventClickListener listener;

    private static final ExecutorService imageExecutor =
            Executors.newFixedThreadPool(3);

    public FitnessAdapter(
            List<Fitness> eventList,
            OnEventClickListener listener
    ) {
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);

        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull EventViewHolder holder,
            int position
    ) {
        Fitness event = eventList.get(position);

        holder.title.setText(
                getTextOrFallback(event.getTitle(), "Untitled Event")
        );

        holder.description.setText(
                getTextOrFallback(
                        event.getDescription(),
                        "Event details will be announced soon."
                )
        );

        holder.date.setText(
                getTextOrFallback(
                        event.getDate(),
                        "Date to be announced"
                )
        );

        holder.location.setText(
                getTextOrFallback(
                        event.getLocation(),
                        "Location to be announced"
                )
        );

        // Reset image while a new image loads.
        holder.image.setImageResource(
                android.R.drawable.ic_menu_gallery
        );

        String imageUrl = event.getImage();

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            loadImage(imageUrl, holder.image);
        }

        holder.detailsButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });

        // Also allow tapping the card to open event details.
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });
    }

    private String getTextOrFallback(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value.trim();
    }

    private void loadImage(String imageUrl, ImageView imageView) {
        imageExecutor.execute(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(imageUrl);

                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestMethod("GET");
                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (InputStream inputStream =
                                 connection.getInputStream()) {

                        Bitmap bitmap =
                                BitmapFactory.decodeStream(inputStream);

                        if (bitmap != null) {
                            imageView.post(() ->
                                    imageView.setImageBitmap(bitmap));
                        }
                    }
                }

            } catch (Exception e) {
                android.util.Log.e(
                        "EventAdapter",
                        "Unable to load event image: " + imageUrl,
                        e
                );
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList == null ? 0 : eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title;
        TextView date;
        TextView location;
        TextView description;
        Button detailsButton;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.eventImage);
            title = itemView.findViewById(R.id.eventTitle);
            date = itemView.findViewById(R.id.eventDate);
            location = itemView.findViewById(R.id.eventLocation);
            description = itemView.findViewById(R.id.eventDescription);
            detailsButton = itemView.findViewById(R.id.eventDetailsButton);
        }
    }
}
