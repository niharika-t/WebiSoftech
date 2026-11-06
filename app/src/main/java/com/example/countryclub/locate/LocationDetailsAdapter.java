
package com.example.countryclub.locate;

import android.content.Context;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationDetailsAdapter
        extends RecyclerView.Adapter<LocationDetailsAdapter.LocationViewHolder> {

    private final Context context;
    private final List<LocationItem> locations;

    private static final ExecutorService imageExecutor =
            Executors.newFixedThreadPool(3);

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public LocationDetailsAdapter(
            Context context, List<LocationItem> locations) {
        this.context = context;
        this.locations = locations;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(
                R.layout.item_location_detail,
                parent,
                false
        );

        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull LocationViewHolder holder, int position) {

        LocationItem item = locations.get(position);

        holder.title.setText(item.getTitle());
        holder.location.setText(
                item.getLocation().isEmpty()
                        ? "Location not available"
                        : item.getLocation()
        );

        holder.address.setText(
                item.getAddress().isEmpty()
                        ? "Address not available"
                        : item.getAddress()
        );

        holder.breakfast.setText(
                "Breakfast  •  " + displayValue(item.getBreakfast())
        );
        holder.lunch.setText(
                "Lunch  •  " + displayValue(item.getLunch())
        );
        holder.dinner.setText(
                "Dinner  •  " + displayValue(item.getDinner())
        );

        holder.image.setImageResource(
                android.R.drawable.ic_menu_gallery
        );

        String imageUrl = item.getImage();
        holder.image.setTag(imageUrl);

        if (imageUrl != null && imageUrl.startsWith("https://")) {
            imageExecutor.execute(() -> {
                Bitmap bitmap = downloadImage(imageUrl);

                if (bitmap != null) {
                    mainHandler.post(() -> {
                        if (imageUrl.equals(holder.image.getTag())) {
                            holder.image.setImageBitmap(bitmap);
                        }
                    });
                }
            });
        }
    }

    private String displayValue(String value) {
        return value == null || value.trim().isEmpty()
                ? "Not specified"
                : value;
    }

    private Bitmap downloadImage(String imageUrl) {
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection)
                    new URL(imageUrl).openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            try (InputStream input = connection.getInputStream()) {
                return BitmapFactory.decodeStream(input);
            }

        } catch (Exception e) {
            android.util.Log.e(
                    "LocationDetailsAdapter",
                    "Image loading failed",
                    e
            );
            return null;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title, location, address;
        TextView breakfast, lunch, dinner;

        LocationViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.imgDetailLocation);
            title = itemView.findViewById(R.id.txtDetailTitle);
            location = itemView.findViewById(R.id.txtDetailLocation);
            address = itemView.findViewById(R.id.txtDetailAddress);
            breakfast = itemView.findViewById(R.id.txtDetailBreakfast);
            lunch = itemView.findViewById(R.id.txtDetailLunch);
            dinner = itemView.findViewById(R.id.txtDetailDinner);
        }
    }
}
