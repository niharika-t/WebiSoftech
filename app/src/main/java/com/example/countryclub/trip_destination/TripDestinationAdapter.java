
package com.example.countryclub.trip_destination;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class TripDestinationAdapter
        extends RecyclerView.Adapter<TripDestinationAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(TripDestination item);
    }

    private final List<TripDestination> items;
    private final OnItemClickListener listener;

    private static final ExecutorService imageExecutor =
            Executors.newFixedThreadPool(3);

    public TripDestinationAdapter(
            List<TripDestination> items,
            OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip_destination, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        TripDestination item = items.get(position);

        // Set title
        String title = item.getTitle();
        holder.title.setText(
                title == null || title.trim().isEmpty()
                        ? "Destination"
                        : title
        );

        // Set subtitle
        String subtitle = item.getSubtitle();
        holder.subtitle.setText(
                subtitle == null || subtitle.trim().isEmpty()
                        ? "Discover more"
                        : subtitle
        );

        // Set description
        String description = item.getDescription();
        holder.description.setText(
                description == null ? "" : description
        );

        holder.description.setVisibility(
                description == null || description.trim().isEmpty()
                        ? View.GONE
                        : View.VISIBLE
        );

        // Load image without applying styling in Java
        String imageUrl = item.getImage();

        holder.image.setTag(imageUrl);
        holder.image.setImageDrawable(null);

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            imageExecutor.execute(() -> {
                Bitmap bitmap = downloadImage(imageUrl);

                if (bitmap != null) {
                    holder.image.post(() -> {
                        if (imageUrl.equals(holder.image.getTag())) {
                            holder.image.setImageBitmap(bitmap);
                        }
                    });
                }
            });
        }

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    private static Bitmap downloadImage(String urlString) {
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection)
                    new URL(urlString).openConnection();

            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);

            try (InputStream stream = connection.getInputStream()) {
                return BitmapFactory.decodeStream(stream);
            }

        } catch (Exception ignored) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title;
        TextView subtitle;
        TextView description;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.ivTripImage);
            title = itemView.findViewById(R.id.tvTripTitle);
            subtitle = itemView.findViewById(R.id.tvTripSubtitle);
            description = itemView.findViewById(R.id.tvTripDescription);
        }
    }
}
