package com.example.countryclub.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.countryclub.R;

public class HomeCarouselAdapter
        extends RecyclerView.Adapter<HomeCarouselAdapter.CarouselViewHolder> {

    private final int[] images = {
            R.drawable.club_banner,
            R.drawable.club_banner_2,
            R.drawable.club_banner_3
    };

    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_carousel, parent, false);

        return new CarouselViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CarouselViewHolder holder, int position) {

        holder.imageView.setImageResource(images[position]);
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    static class CarouselViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        CarouselViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivCarouselImage);
        }
    }
}