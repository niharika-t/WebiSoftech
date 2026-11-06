
package com.example.countryclub.blog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.countryclub.R;

import java.util.List;

public class BlogCategoryAdapter
        extends RecyclerView.Adapter<BlogCategoryAdapter.CategoryViewHolder> {

    private final List<BlogCategory> categories;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(BlogCategory category);
    }

    public BlogCategoryAdapter(
            List<BlogCategory> categories,
            OnCategoryClickListener listener) {

        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blog_category, parent, false);

        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CategoryViewHolder holder, int position) {

        BlogCategory category = categories.get(position);

        holder.tvCategory.setText(category.getName());

        holder.itemView.setOnClickListener(v -> {
            listener.onCategoryClick(category);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class CategoryViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvCategory;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvBlogCategory);
        }
    }
}
