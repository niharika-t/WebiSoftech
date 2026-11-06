
package com.example.countryclub.holidays;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.countryclub.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BookNowFragment extends Fragment {

    private Spinner spinnerResort, spinnerDuration;
    private Spinner spinnerAdults, spinnerChildren;
    private Button btnCheckIn, btnCheckOut, btnSubmitBooking;

    private Calendar checkInDate, checkOutDate;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_book_now, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Handle device Back button
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

        spinnerResort = view.findViewById(R.id.spinnerResort);
        spinnerDuration = view.findViewById(R.id.spinnerDuration);
        spinnerAdults = view.findViewById(R.id.spinnerAdults);
        spinnerChildren = view.findViewById(R.id.spinnerChildren);

        btnCheckIn = view.findViewById(R.id.btnCheckIn);
        btnCheckOut = view.findViewById(R.id.btnCheckOut);
        btnSubmitBooking = view.findViewById(R.id.btnSubmitBooking);

        setSpinner(spinnerResort, new String[]{
                "Select Resort",
                "Goa Resort",
                "Lonavala Resort",
                "Mahabaleshwar Resort",
                "Kerala Resort"
        });

        setSpinner(spinnerDuration, new String[]{
                "Select Holiday Length",
                "2 Nights / 3 Days",
                "3 Nights / 4 Days",
                "5 Nights / 6 Days",
                "6 Nights / 7 Days",
                "7 Nights / 8 Days"
        });

        setSpinner(spinnerAdults, new String[]{
                "1", "2", "3", "4", "5", "6"
        });

        setSpinner(spinnerChildren, new String[]{
                "0", "1", "2", "3", "4", "5"
        });

        btnCheckIn.setOnClickListener(v -> showDatePicker(true));
        btnCheckOut.setOnClickListener(v -> showDatePicker(false));

        btnSubmitBooking.setOnClickListener(v -> submitBooking());
    }

    private void setSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                items);

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
    }

    private void showDatePicker(boolean isCheckIn) {

        Calendar initialDate = Calendar.getInstance();

        if (isCheckIn && checkInDate != null) {
            initialDate = (Calendar) checkInDate.clone();
        } else if (!isCheckIn && checkOutDate != null) {
            initialDate = (Calendar) checkOutDate.clone();
        } else if (!isCheckIn && checkInDate != null) {
            initialDate = (Calendar) checkInDate.clone();
            initialDate.add(Calendar.DAY_OF_MONTH, 1);
        }

        @SuppressLint("SetTextI18n") DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (picker, year, month, day) -> {

                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, day, 0, 0, 0);
                    selected.set(Calendar.MILLISECOND, 0);

                    Calendar today = Calendar.getInstance();
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MILLISECOND, 0);

                    if (isCheckIn) {

                        if (selected.before(today)) {
                            Toast.makeText(requireContext(),
                                    "Check-in cannot be in the past",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (checkOutDate != null
                                && !checkOutDate.after(selected)) {
                            checkOutDate = null;
                            btnCheckOut.setText("Select Check-out Date");
                        }

                        checkInDate = selected;
                        btnCheckIn.setText(dateFormat.format(
                                selected.getTime()));

                    } else {

                        if (checkInDate == null) {
                            Toast.makeText(requireContext(),
                                    "Please select check-in first",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!selected.after(checkInDate)) {
                            Toast.makeText(requireContext(),
                                    "Check-out must be after check-in",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        checkOutDate = selected;
                        btnCheckOut.setText(dateFormat.format(
                                selected.getTime()));
                    }
                },
                initialDate.get(Calendar.YEAR),
                initialDate.get(Calendar.MONTH),
                initialDate.get(Calendar.DAY_OF_MONTH));

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        if (isCheckIn) {
            dialog.getDatePicker().setMinDate(today.getTimeInMillis());
        } else {
            Calendar minimum = checkInDate != null
                    ? (Calendar) checkInDate.clone()
                    : today;

            minimum.add(Calendar.DAY_OF_MONTH, 1);
            dialog.getDatePicker().setMinDate(minimum.getTimeInMillis());
        }

        dialog.show();
    }

    private void submitBooking() {

        if (spinnerResort.getSelectedItemPosition() == 0) {
            showMessage("Please select a resort");
            return;
        }

        if (spinnerDuration.getSelectedItemPosition() == 0) {
            showMessage("Please select holiday length");
            return;
        }

        if (checkInDate == null || checkOutDate == null) {
            showMessage("Please select check-in and check-out dates");
            return;
        }

        String resort = spinnerResort.getSelectedItem().toString();
        String duration = spinnerDuration.getSelectedItem().toString();
        String adults = spinnerAdults.getSelectedItem().toString();
        String children = spinnerChildren.getSelectedItem().toString();

        String message = "Booking request details:\n"
                + "Resort: " + resort + "\n"
                + "Holiday: " + duration + "\n"
                + "Check-in: " + dateFormat.format(checkInDate.getTime()) + "\n"
                + "Check-out: " + dateFormat.format(checkOutDate.getTime()) + "\n"
                + "Adults: " + adults + "\n"
                + "Children: " + children;

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirm Booking Request")
                .setMessage(message)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    showMessage("Booking request submitted successfully");
                })
                .show();
    }



    private void showMessage(String message) {
        Toast.makeText(requireContext(),
                message, Toast.LENGTH_SHORT).show();
    }
}
