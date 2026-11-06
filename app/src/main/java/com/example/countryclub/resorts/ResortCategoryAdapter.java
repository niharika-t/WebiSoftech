
package com.example.countryclub.resorts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.countryclub.R;

import java.util.List;

public class ResortCategoryAdapter
        extends RecyclerView.Adapter<ResortCategoryAdapter.ViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(ResortCategory category);
    }

    private final List<ResortCategory> categoryList;
    private final OnCategoryClickListener listener;

    public ResortCategoryAdapter(
            List<ResortCategory> categoryList,
            OnCategoryClickListener listener) {

        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_resort_category,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        ResortCategory category = categoryList.get(position);

        holder.tvCategoryName.setText(category.getName());


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvCategoryName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);


            tvCategoryName =
                    itemView.findViewById(R.id.tvCategoryName);
        }
    }
}
