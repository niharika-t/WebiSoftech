
package com.example.countryclub.locate;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.countryclub.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationAdapter
        extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    public interface OnCategoryClick {
        void onClick(LocationCategory category);
    }

    public interface OnDetailClick {
        void onClick(LocationItem item);
    }

    private final boolean categoryMode;
    private final OnCategoryClick categoryClick;
    private final OnDetailClick detailClick;

    private final List<LocationCategory> categories = new ArrayList<>();
    private final List<LocationItem> items = new ArrayList<>();

    private static final ExecutorService imageExecutor =
            Executors.newFixedThreadPool(2);

    private static final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public LocationAdapter(OnCategoryClick callback) {
        categoryMode = true;
        categoryClick = callback;
        detailClick = null;
    }

    public LocationAdapter(OnDetailClick callback) {
        categoryMode = false;
        categoryClick = null;
        detailClick = callback;
    }

    public void setCategories(List<LocationCategory> data) {
        categories.clear();
        categories.addAll(data);
        notifyDataSetChanged();
    }

    public void setItems(List<LocationItem> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        // Reset recycled views before binding new data.
        holder.image.setVisibility(View.GONE);
        holder.subtitle.setVisibility(View.GONE);
        holder.place.setVisibility(View.GONE);
        holder.arrow.setVisibility(View.VISIBLE);

        holder.image.setImageDrawable(null);
        holder.image.setTag(null);
        holder.subtitle.setText("");
        holder.place.setText("");

        if (categoryMode) {
            LocationCategory category = categories.get(position);

            holder.title.setText(category.getName());

            holder.itemView.setOnClickListener(v -> {
                if (categoryClick != null) {
                    categoryClick.onClick(category);
                }
            });

        } else {
            LocationItem item = items.get(position);

            holder.title.setText(item.getTitle());

            // Display location and address in the existing card.
            String locationText = valueOrFallback(
                    item.getLocation(), "Location not available");

            String addressText = valueOrFallback(
                    item.getAddress(), "Address not available");

            holder.subtitle.setText(locationText);
            holder.subtitle.setVisibility(View.VISIBLE);

            holder.place.setText(addressText);
            holder.place.setVisibility(View.VISIBLE);

            if (item.getImage() != null
                    && !item.getImage().trim().isEmpty()) {

                holder.image.setVisibility(View.VISIBLE);
                holder.image.setTag(item.getImage());

                loadImage(item.getImage(), holder.image);
            }

            holder.itemView.setOnClickListener(v -> {
                if (detailClick != null) {
                    detailClick.onClick(item);
                }
            });
        }
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.trim().isEmpty()
                ? fallback
                : value.trim();
    }

    private void loadImage(String imageUrl, ImageView imageView) {
        imageExecutor.execute(() -> {
            Bitmap bitmap = null;
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection)
                        new URL(imageUrl).openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                if (connection.getResponseCode()
                        == HttpURLConnection.HTTP_OK) {

                    try (InputStream stream =
                                 connection.getInputStream()) {
                        bitmap = BitmapFactory.decodeStream(stream);
                    }
                }

            } catch (Exception e) {
                android.util.Log.e(
                        "LocationAdapter",
                        "Image loading failed: " + imageUrl,
                        e
                );
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            Bitmap result = bitmap;

            mainHandler.post(() -> {
                Object tag = imageView.getTag();

                if (tag != null
                        && imageUrl.equals(tag)
                        && result != null) {
                    imageView.setImageBitmap(result);
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return categoryMode ? categories.size() : items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title;
        TextView subtitle;
        TextView place;
        TextView arrow;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.imgLocation);
            title = itemView.findViewById(R.id.txtLocationTitle);
            subtitle = itemView.findViewById(R.id.txtLocationSubtitle);
            place = itemView.findViewById(R.id.txtLocationPlace);
            arrow = itemView.findViewById(R.id.txtLocationArrow);
        }
    }
}
