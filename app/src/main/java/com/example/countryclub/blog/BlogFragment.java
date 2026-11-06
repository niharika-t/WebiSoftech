
package com.example.countryclub.blog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.countryclub.MainActivity;
import com.example.countryclub.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BlogFragment extends Fragment {

    private static final String TAG = "BlogFragment";

    private RecyclerView recyclerBlogs;
    private ProgressBar progressBar;
    private TextView tvBlogError;
    private TextView tvSelectedCategory;

    private ImageView imgBlogNotAvailable;

    private BlogAdapter blogAdapter;
    private BlogCategoryAdapter categoryAdapter;

    private final List<Blog> blogList = new ArrayList<>();
    private final List<BlogCategory> categoryList = new ArrayList<>();

    private int requestNumber = 0;

    public static void load(MainActivity activity) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new BlogFragment())
                .commit();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_blog, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        recyclerBlogs = view.findViewById(R.id.recyclerBlogs);
        RecyclerView recyclerBlogCategories = view.findViewById(R.id.recyclerBlogCategories);
        progressBar = view.findViewById(R.id.progressBar);
        tvBlogError = view.findViewById(R.id.tvBlogError);

        imgBlogNotAvailable = view.findViewById(R.id.imgBlogNotAvailable);

        // Optional TextView: add this ID to fragment_blog.xml.
        tvSelectedCategory = view.findViewById(R.id.tvSelectedCategory);

        recyclerBlogs.setLayoutManager(
                new LinearLayoutManager(requireContext()));

        blogAdapter = new BlogAdapter(blogList);
        recyclerBlogs.setAdapter(blogAdapter);

        recyclerBlogCategories.setLayoutManager(
                new LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false));

        categoryAdapter = new BlogCategoryAdapter(
                categoryList,
                category -> fetchSelectedCategory(category.getId())
        );

        recyclerBlogCategories.setAdapter(categoryAdapter);

        progressBar.setVisibility(View.GONE);
        recyclerBlogs.setVisibility(View.GONE);
        tvBlogError.setVisibility(View.GONE);

        loadBlogCategories();
    }

    private void loadBlogCategories() {

        BlogApiService.getBlogCategories(
                new BlogApiService.ApiCallback<>() {

                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(JSONArray data) {

                        if (!isAdded() || getView() == null) return;

                        categoryList.clear();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.optJSONObject(i);
                            if (item == null) continue;

                            String id = item.optString("id", "");
                            String name = item.optString("name", "");

                            if (!id.isEmpty()
                                    && !"null".equals(id)
                                    && !name.isEmpty()
                                    && !"null".equals(name)) {

                                categoryList.add(
                                        new BlogCategory(id, name));
                            }
                        }

                        categoryAdapter.notifyDataSetChanged();

                        Log.d(TAG, "Loaded categories: "
                                + categoryList.size());

                        if (categoryList.isEmpty()) {
                            showError("No categories found.");
                            return;
                        }

                        // Initially load the first category.
                        fetchSelectedCategory(
                                categoryList.get(0).getId());
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded() || getView() == null) return;

                        showError("Unable to load categories: " + message);
                    }
                });
    }

    // Called when the user taps a category card.
    private void fetchSelectedCategory(String categoryId) {

        if (categoryId == null || categoryId.trim().isEmpty()
                || "null".equalsIgnoreCase(categoryId.trim())) {
            showError("Invalid category ID.");
            return;
        }

        final int currentRequest = ++requestNumber;

        progressBar.setVisibility(View.VISIBLE);
        recyclerBlogs.setVisibility(View.GONE);
        tvBlogError.setVisibility(View.GONE);

        blogList.clear();
        blogAdapter.notifyDataSetChanged();

        Log.d(TAG, "Selected category ID: " + categoryId);

        imgBlogNotAvailable.setVisibility(View.GONE);
        recyclerBlogs.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        // Step 1: Get the category object.
        BlogApiService.getBlogCategoryById(
                categoryId,
                new BlogApiService.ApiCallback<>() {

                    @Override
                    public void onSuccess(JSONObject category) {

                        if (!isAdded() || getView() == null
                                || currentRequest != requestNumber) {
                            return;
                        }

                        String categoryName =
                                category.optString("name", "Blogs");

                        String returnedId =
                                category.optString("id", "");

                        if (!categoryId.equals(returnedId)) {
                            progressBar.setVisibility(View.GONE);
                            showError("The returned category ID does not match.");
                            return;
                        }

                        if (tvSelectedCategory != null) {
                            tvSelectedCategory.setText(categoryName);
                            tvSelectedCategory.setVisibility(View.VISIBLE);
                        }

                        Log.d(TAG, "Category loaded: " + categoryName);

                        // Step 2: Fetch the blog array for this ID.
                        fetchBlogsForSelectedCategory(
                                categoryId, currentRequest);
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded() || getView() == null
                                || currentRequest != requestNumber) {
                            return;
                        }

                        progressBar.setVisibility(View.GONE);
                        showError("Unable to load category: " + message);
                    }
                });
    }

    private void fetchBlogsForSelectedCategory(
            String categoryId, int currentRequest) {

        BlogApiService.getBlogsForCategory(
                categoryId,
                new BlogApiService.ApiCallback<>() {

                    @Override
                    public void onSuccess(JSONArray blogs) {
                        if (!isAdded() || getView() == null
                                || currentRequest != requestNumber) {
                            return;
                        }

                        displayBlogs(blogs, categoryId);
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded() || getView() == null
                                || currentRequest != requestNumber) {
                            return;
                        }

                        progressBar.setVisibility(View.GONE);
                        recyclerBlogs.setVisibility(View.GONE);

                        ImageView img = getView().findViewById(
                                R.id.imgBlogNotAvailable
                        );
                        img.setVisibility(View.VISIBLE);

                        Log.e(TAG, "Blog loading failed: " + message);
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void displayBlogs(JSONArray data, String categoryId) {

        blogList.clear();

        for (int i = 0; i < data.length(); i++) {

            JSONObject item = data.optJSONObject(i);
            if (item == null) continue;

            String id = item.optString("id", "");
            String itemCategoryId =
                    item.optString("category_id", "");
            String title = item.optString("title", "");
            String description =
                    item.optString("description", "");
            String image = item.optString("image", "");
            String date = item.optString("created_at", "");

            if (title.trim().isEmpty()
                    || "null".equalsIgnoreCase(title)
                    || !categoryId.equals(itemCategoryId)) {
                continue;
            }

            blogList.add(new Blog(
                    id,
                    itemCategoryId,
                    title,
                    description,
                    image,
                    date
            ));
        }

        blogAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);

        if (blogList.isEmpty()) {
            recyclerBlogs.setVisibility(View.GONE);
            showError("No blogs found for this category.");
        } else {
            tvBlogError.setVisibility(View.GONE);
            recyclerBlogs.setVisibility(View.VISIBLE);

            Log.d(TAG, "Displayed " + blogList.size()
                    + " blogs for category " + categoryId);
        }
    }

    private void showError(String message) {
        if (tvBlogError != null) {
            tvBlogError.setText(message);
            tvBlogError.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        requestNumber++;
        super.onDestroyView();
    }
}
