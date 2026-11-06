
package com.example.countryclub.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.countryclub.R;

import java.util.List;

public class HomeCategoryAdapter
        extends RecyclerView.Adapter<HomeCategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(HomeCategory category);
    }

    private final List<HomeCategory> categories;
    private final OnCategoryClickListener listener;

    public HomeCategoryAdapter(
            List<HomeCategory> categories,
            OnCategoryClickListener listener) {

        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_home_category,
                        parent,
                        false
                );

        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CategoryViewHolder holder,
            int position) {

        HomeCategory category = categories.get(position);

        // Set category name
        holder.tvCategoryName.setText(category.getName());

        // Load category image
        Glide.with(holder.itemView.getContext())
                .load(category.getImage())
                .placeholder(R.drawable.category_image_bg)
                .error(R.drawable.category_image_bg)
                .centerCrop()
                .into(holder.imgCategory);

        // Existing category click functionality
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {

        final TextView tvCategoryName;
        final ImageView imgCategory;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCategoryName = itemView.findViewById(
                    R.id.tvHomeCategory
            );

            imgCategory = itemView.findViewById(
                    R.id.imgHomeCategory
            );
        }
    }
}
