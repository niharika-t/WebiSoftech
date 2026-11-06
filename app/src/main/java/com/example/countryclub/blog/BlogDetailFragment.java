
package com.example.countryclub.blog;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlogDetailFragment extends Fragment {

    private ImageView ivBlogImage;
    private TextView tvBlogTitle;
    private TextView tvBlogDescription;
    private TextView tvBlogDate;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public BlogDetailFragment() {
        // Required empty constructor
    }

    public static BlogDetailFragment newInstance(String blogId) {
        BlogDetailFragment fragment = new BlogDetailFragment();

        Bundle args = new Bundle();
        args.putString("blog_id", blogId);

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
                R.layout.fragment_blog_detail,
                container,
                false
        );
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        ivBlogImage = view.findViewById(R.id.ivDetailBlogImage);
        tvBlogTitle = view.findViewById(R.id.tvDetailBlogTitle);
        tvBlogDescription =
                view.findViewById(R.id.tvDetailBlogDescription);
        tvBlogDate = view.findViewById(R.id.tvDetailBlogDate);

        tvBlogTitle.setText("Loading blog...");
        tvBlogDescription.setText("");
        tvBlogDate.setText("");

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
            tvBlogTitle.setText("Blog ID is missing");
            return;
        }

        String blogId = args.getString("blog_id", "");

        if (blogId.trim().isEmpty()) {
            tvBlogTitle.setText("Blog ID is missing");
            return;
        }

        loadBlogDetails(blogId);
    }

    private void loadBlogDetails(String blogId) {

        BlogApiService.getBlogDetails(
                blogId,
                new BlogApiService.ApiCallback<>() {

                    @Override
                    public void onSuccess(Blog blog) {
                        if (!isAdded() || getView() == null) {
                            return;
                        }

                        tvBlogTitle.setText(blog.getTitle());
                        tvBlogDescription.setText(
                                blog.getDescription());
                        tvBlogDate.setText(blog.getDate());

                        String imagePath = blog.getImageUrl();

                        if (imagePath != null &&
                                !imagePath.trim().isEmpty()) {

                            // The API may return a relative image path.
                            String imageUrl = imagePath;

                            if (!imageUrl.startsWith("http://") &&
                                    !imageUrl.startsWith("https://")) {

                                imageUrl =
                                        "https://holidayserviceclub.com/HolidayServiceClub/public/"
                                                + imageUrl.replaceFirst("^/+", "");
                            }

                            loadImage(imageUrl);
                        }
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onError(String message) {
                        if (!isAdded() || getView() == null) {
                            return;
                        }

                        Log.e("BlogDetailFragment",
                                "Blog details API error: " + message);

                        tvBlogTitle.setText("Unable to load blog");
                        tvBlogDescription.setText(message);
                    }
                }
        );
    }

    private void loadImage(String imageUrl) {

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return;
        }

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

                int responseCode = connection.getResponseCode();

                if (responseCode >= 200 && responseCode < 300) {
                    try (InputStream input =
                                 connection.getInputStream()) {
                        bitmap = BitmapFactory.decodeStream(input);
                    }
                }

            } catch (Exception e) {
                Log.e("BlogDetailFragment",
                        "Image loading failed: " + imageUrl, e);

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            Bitmap result = bitmap;

            mainHandler.post(() -> {
                if (!isAdded() || getView() == null) {
                    return;
                }

                if (result != null) {
                    ivBlogImage.setImageBitmap(result);
                }
            });
        });
    }

    @Override
    public void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
