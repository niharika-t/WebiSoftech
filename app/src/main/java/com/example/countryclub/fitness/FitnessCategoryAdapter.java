
package com.example.countryclub.fitness;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.countryclub.R;

import java.util.List;

public class FitnessCategoryAdapter
        extends RecyclerView.Adapter<FitnessCategoryAdapter.ViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(FitnessCategory category);
    }

    private final List<FitnessCategory> categories;
    private final OnCategoryClickListener listener;

    public FitnessCategoryAdapter(
            List<FitnessCategory> categories,
            OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_category, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        FitnessCategory category = categories.get(position);

        holder.name.setText(category.getName());

        // Open category details when the category name is clicked
        holder.name.setOnClickListener(
                v -> listener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.eventCategoryName);
        }
    }
}
