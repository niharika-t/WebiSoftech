
package com.example.countryclub.payment_details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.countryclub.R;

public class PaymentDetailsFragment extends Fragment {

    public PaymentDetailsFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_payment,
                container,
                false
        );
    }

    // Call this function directly from MainActivity
    public static void load(FragmentActivity activity) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new PaymentDetailsFragment()
                )
                .commit();
    }
}
