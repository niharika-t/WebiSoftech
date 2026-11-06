
package com.example.countryclub.subscriptions;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
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
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubscriptionAdapter
        extends RecyclerView.Adapter<SubscriptionAdapter.ViewHolder> {

    public static final int TYPE_CATEGORY = 0;
    public static final int TYPE_DETAIL = 1;

    private final int type;
    private final List<SubscriptionCategory> categories;
    private final List<SubscriptionDetail> details;
    private final OnCategoryClickListener listener;

    private static final ExecutorService imageExecutor =
            Executors.newFixedThreadPool(3);

    public interface OnCategoryClickListener {
        void onCategoryClick(SubscriptionCategory category);
    }

    public SubscriptionAdapter(
            List<SubscriptionCategory> categories,
            OnCategoryClickListener listener) {

        this.type = TYPE_CATEGORY;
        this.categories = categories;
        this.details = null;
        this.listener = listener;
    }

    public SubscriptionAdapter(List<SubscriptionDetail> details) {
        this.type = TYPE_DETAIL;
        this.details = details;
        this.categories = null;
        this.listener = null;
    }

    @Override
    public int getItemViewType(int position) {
        return type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        int layout = viewType == TYPE_CATEGORY
                ? R.layout.item_subscription_category
                : R.layout.item_subscription_detail;

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);

        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        if (type == TYPE_CATEGORY) {
            SubscriptionCategory category = categories.get(position);

            holder.title.setText(
                    nonEmpty(category.getTitle(), "Membership"));

            String amount = formatAmount(category.getAmount());
            String duration = category.getDuration();

            String displayInfo = amount;

            if (!duration.isEmpty()) {
                displayInfo = displayInfo.isEmpty()
                        ? duration
                        : displayInfo + "  •  " + duration;
            }

            holder.description.setText(
                    nonEmpty(displayInfo, "Membership information"));

            loadImage(category.getImage(), holder.image);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            });

        } else {
            SubscriptionDetail detail = details.get(position);

            holder.title.setText(
                    nonEmpty(detail.getTitle(), "Subscription Details"));

            holder.description.setText(
                    nonEmpty(detail.getDescription(), ""));

            loadImage(detail.getImage(), holder.image);

            holder.itemView.setOnClickListener(null);
        }
    }

    private static String nonEmpty(String value, String fallback) {
        return value == null || value.trim().isEmpty()
                ? fallback
                : value.trim();
    }

    private static String formatAmount(String amount) {
        if (amount == null || amount.trim().isEmpty()) {
            return "";
        }

        try {
            double value = Double.parseDouble(amount.trim());

            NumberFormat formatter = NumberFormat.getNumberInstance(
                    new Locale("en", "IN"));

            formatter.setMinimumFractionDigits(2);
            formatter.setMaximumFractionDigits(2);

            return "₹" + formatter.format(value);

        } catch (NumberFormatException e) {
            Log.w("SubscriptionAdapter",
                    "Invalid subscription amount: " + amount);
            return "₹" + amount;
        }
    }

    @Override
    public int getItemCount() {
        if (type == TYPE_CATEGORY) {
            return categories == null ? 0 : categories.size();
        }

        return details == null ? 0 : details.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView image;
        final TextView title;
        final TextView description;

        ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);

            if (viewType == TYPE_CATEGORY) {
                image = itemView.findViewById(
                        R.id.imgSubscriptionCategory);

                title = itemView.findViewById(
                        R.id.tvSubscriptionCategoryTitle);

                description = itemView.findViewById(
                        R.id.tvSubscriptionCategoryDescription);

            } else {
                image = itemView.findViewById(
                        R.id.imgSubscriptionDetail);

                title = itemView.findViewById(
                        R.id.tvSubscriptionDetailTitle);

                description = itemView.findViewById(
                        R.id.tvSubscriptionDetailDescription);
            }
        }
    }

    private static void loadImage(
            String imageUrl,
            ImageView imageView) {

        if (imageView == null) {
            return;
        }

        String urlToLoad = imageUrl == null ? "" : imageUrl.trim();

        imageView.setTag(urlToLoad);
        imageView.setImageDrawable(null);
        imageView.setBackgroundColor(Color.rgb(240, 236, 246));

        if (urlToLoad.isEmpty()) {
            return;
        }

        imageExecutor.execute(() -> {
            Bitmap bitmap = null;
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection)
                        new URL(urlToLoad).openConnection();

                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                try (InputStream stream = connection.getInputStream()) {
                    bitmap = BitmapFactory.decodeStream(stream);
                }

            } catch (Exception e) {
                Log.e("SubscriptionAdapter",
                        "Image loading failed: " + urlToLoad, e);

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            Bitmap result = bitmap;

            imageView.post(() -> {
                if (urlToLoad.equals(imageView.getTag())
                        && result != null) {
                    imageView.setImageBitmap(result);
                }
            });
        });
    }
}
