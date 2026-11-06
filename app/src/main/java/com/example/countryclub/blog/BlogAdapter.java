
package com.example.countryclub.blog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.countryclub.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlogAdapter
        extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {

    private final List<Blog> blogList;

    private final ExecutorService executor =
            Executors.newFixedThreadPool(3);

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public BlogAdapter(List<Blog> blogList) {
        this.blogList = blogList;
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blog, parent, false);

        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull BlogViewHolder holder, int position) {

        Blog blog = blogList.get(position);

        holder.title.setText(blog.getTitle());
        holder.description.setText(blog.getDescription());
        holder.date.setText(blog.getDate());

        // Reset image when RecyclerView reuses a card
        holder.image.setImageDrawable(null);
        holder.image.setBackgroundColor(0xFFE5E5E5);

        String imageUrl = blog.getImageUrl();
        holder.image.setTag(imageUrl);

        // Load blog image
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {

            executor.execute(() -> {

                Bitmap bitmap = null;
                HttpURLConnection connection = null;

                try {
                    URL url = new URL(imageUrl);

                    connection =
                            (HttpURLConnection) url.openConnection();

                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    connection.setInstanceFollowRedirects(true);
                    connection.connect();

                    if (connection.getResponseCode() == 200) {
                        try (InputStream input =
                                     connection.getInputStream()) {

                            bitmap =
                                    BitmapFactory.decodeStream(input);
                        }
                    }

                } catch (Exception e) {
                    Log.e("BlogAdapter",
                            "Image loading failed: " + imageUrl, e);

                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }

                Bitmap result = bitmap;

                mainHandler.post(() -> {
                    Object tag = holder.image.getTag();

                    if (imageUrl.equals(tag) && result != null) {
                        holder.image.setImageBitmap(result);
                    }
                });
            });
        }

        // Open selected blog details

        holder.itemView.setOnClickListener(v -> {
            String blogId = blog.getId();

            if (blogId == null || blogId.trim().isEmpty()) {
                Log.e("BlogAdapter",
                        "Cannot open details: blog ID is missing");
                return;
            }

            FragmentActivity activity =
                    (FragmentActivity) v.getContext();

            BlogDetailFragment detailFragment =
                    BlogDetailFragment.newInstance(blogId);

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    // Non-static inner ViewHolder
    public static class BlogViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title;
        TextView description;
        TextView date;

        BlogViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.ivBlogImage);
            title = itemView.findViewById(R.id.tvBlogTitle);

            description =
                    itemView.findViewById(R.id.tvBlogDescription);

            date = itemView.findViewById(R.id.tvBlogDate);
        }
    }
}
