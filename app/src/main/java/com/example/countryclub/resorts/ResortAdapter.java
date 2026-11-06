
package com.example.countryclub.resorts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.countryclub.R;

import java.util.List;

public class ResortAdapter
        extends RecyclerView.Adapter<ResortAdapter.ViewHolder> {

    public interface OnResortClickListener {
        void onResortClick(Resort resort);
    }

    private final List<Resort> resortList;
    private final OnResortClickListener listener;

    public ResortAdapter(
            List<Resort> resortList,
            OnResortClickListener listener) {

        this.resortList = resortList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_resort,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        Resort resort = resortList.get(position);

        holder.tvResortTitle.setText(resort.getTitle());

        // This JSON does not contain a location field.
        holder.tvResortLocation.setVisibility(View.GONE);

        ResortImageLoader.load(
                holder.ivResortImage,
                resort.getImage()
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onResortClick(resort);
            }
        });


    }

    @Override
    public int getItemCount() {
        return resortList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivResortImage;
        TextView tvResortTitle;
        TextView tvResortLocation;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivResortImage =
                    itemView.findViewById(R.id.ivResortImage);

            tvResortTitle =
                    itemView.findViewById(R.id.tvResortTitle);

            tvResortLocation =
                    itemView.findViewById(R.id.tvResortLocation);


        }
    }
}
