
package com.example.countryclub.holidays;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.countryclub.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HolidaysFragment extends Fragment {

    private final List<Holiday> holidayList = new ArrayList<>();

    public static void load(androidx.fragment.app.FragmentActivity activity) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new HolidaysFragment())
                .commit();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_holidays, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerHolidays = view.findViewById(R.id.recyclerHolidays);
        ExtendedFloatingActionButton btnBookNow = view.findViewById(R.id.btnBookNow);

        recyclerHolidays.setLayoutManager(
                new LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.VERTICAL,
                        false));

        loadDummyHolidays();

        recyclerHolidays.setAdapter(new RecyclerView.Adapter<HolidayViewHolder>() {

            @NonNull
            @Override
            public HolidayViewHolder onCreateViewHolder(
                    @NonNull ViewGroup parent, int viewType) {

                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_holiday, parent, false);

                return new HolidayViewHolder(itemView);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(
                    @NonNull HolidayViewHolder holder, int position) {

                Holiday holiday = holidayList.get(position);

                holder.duration.setText(holiday.duration);
                holder.title.setText(holiday.title);
                holder.status.setText("EXPIRED");
                holder.expiry.setText("Holiday expired on " + holiday.expiry);
            }

            @Override
            public int getItemCount() {
                return holidayList.size();
            }
        });

        btnBookNow.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new BookNowFragment())
                        .addToBackStack("holidays")
                        .commit());
    }

    private void loadDummyHolidays() {
        holidayList.clear();

        holidayList.add(new Holiday(
                "6 Nights / 7 Days",
                "Holiday Package 01",
                "30 Sep 2026"));

        holidayList.add(new Holiday(
                "3 Nights / 4 Days",
                "Holiday Package 02",
                "15 Aug 2026"));

        holidayList.add(new Holiday(
                "5 Nights / 6 Days",
                "Holiday Package 03",
                "01 Jun 2026"));
    }

    private static class Holiday {
        String duration;
        String title;
        String expiry;

        Holiday(String duration, String title, String expiry) {
            this.duration = duration;
            this.title = title;
            this.expiry = expiry;
        }
    }

    private static class HolidayViewHolder
            extends RecyclerView.ViewHolder {

        TextView duration, title, status, expiry;

        HolidayViewHolder(@NonNull View itemView) {
            super(itemView);

            duration = itemView.findViewById(R.id.txtHolidayDuration);
            title = itemView.findViewById(R.id.txtHolidayTitle);
            status = itemView.findViewById(R.id.txtHolidayStatus);
            expiry = itemView.findViewById(R.id.txtExpiryDate);
        }
    }
}
