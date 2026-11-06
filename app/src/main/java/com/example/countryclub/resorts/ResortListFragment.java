
package com.example.countryclub.resorts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.OnBackPressedCallback;


import com.example.countryclub.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ResortListFragment extends Fragment {

    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_CATEGORY_NAME = "category_name";

    private String categoryId;
    private String categoryName;

    private RecyclerView rvResorts;
    private ProgressBar progressResortList;
    private TextView tvResortListHeading;
    private TextView tvResortListSubtitle;
    private TextView tvResortListMessage;
    private Button btnRetryResortList;

    private ResortAdapter adapter;

    private final List<Resort> resortList = new ArrayList<>();

    public ResortListFragment() {
        // Required empty constructor
    }

    public static void open(
            Fragment parent,
            ResortCategory category) {

        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, category.getId());
        args.putString(ARG_CATEGORY_NAME, category.getName());

        ResortListFragment fragment = new ResortListFragment();
        fragment.setArguments(args);

        parent.getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack("resort_list")
                .commit();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_resort_list,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        rvResorts = view.findViewById(R.id.rvResorts);
        progressResortList =
                view.findViewById(R.id.progressResortList);
        tvResortListHeading =
                view.findViewById(R.id.tvResortListHeading);
        tvResortListSubtitle =
                view.findViewById(R.id.tvResortListSubtitle);
        tvResortListMessage =
                view.findViewById(R.id.tvResortListMessage);
        btnRetryResortList =
                view.findViewById(R.id.btnRetryResortList);

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

        Bundle args = getArguments();

        if (args == null) {
            showMessage("Resort category was not selected.");
            progressResortList.setVisibility(View.GONE);
            return;
        }

        categoryId = args.getString(ARG_CATEGORY_ID);
        categoryName = args.getString(ARG_CATEGORY_NAME, "Resorts");

        tvResortListHeading.setText(categoryName);
        tvResortListSubtitle.setText(
                "Explore resorts in " + categoryName
        );

        rvResorts.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        adapter = new ResortAdapter(
                resortList,
                resort -> ResortDetailsFragment.open(this, resort)
        );

        rvResorts.setAdapter(adapter);

        btnRetryResortList.setOnClickListener(v -> loadResorts());

        loadResorts();
    }

    private void loadResorts() {
        if (!isAdded()) {
            return;
        }

        progressResortList.setVisibility(View.VISIBLE);
        rvResorts.setVisibility(View.GONE);
        tvResortListMessage.setVisibility(View.GONE);
        btnRetryResortList.setVisibility(View.GONE);

        ResortApiService.getResortDetails(
                categoryId,
                new ResortApiService.ApiCallback<JSONArray>() {

                    @Override
                    public void onSuccess(JSONArray data) {
                        if (!isAdded()) {
                            return;
                        }

                        progressResortList.setVisibility(View.GONE);
                        resortList.clear();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject object = data.optJSONObject(i);

                            if (object == null) {
                                continue;
                            }

                            String id = object.optString("id", "");
                            String itemCategoryId =
                                    object.optString("category_id", "");
                            String title =
                                    object.optString("title", "");
                            String description =
                                    object.optString("description", "");
                            String image =
                                    object.optString("image", "");

                            // Ignore inactive or deleted records.
                            if (!"1".equals(object.optString("status", "1"))) {
                                continue;
                            }

                            if ("1".equals(object.optString("is_deleted", "0"))) {
                                continue;
                            }

                            if (title.trim().isEmpty()
                                    || "null".equalsIgnoreCase(title)) {
                                continue;
                            }

                            // Avoid showing resorts from a different category.
                            if (itemCategoryId.isEmpty()
                                    || itemCategoryId.equals(categoryId)) {

                                resortList.add(
                                        new Resort(
                                                id,
                                                itemCategoryId,
                                                title,
                                                description,
                                                image
                                        )
                                );
                            }
                        }

                        if (resortList.isEmpty()) {
                            showMessage("No resorts available in this category.");
                            return;
                        }

                        adapter.notifyDataSetChanged();
                        rvResorts.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded()) {
                            return;
                        }

                        progressResortList.setVisibility(View.GONE);
                        showMessage("Unable to load resorts.");

                        Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void showMessage(String message) {
        tvResortListMessage.setText(message);
        tvResortListMessage.setVisibility(View.VISIBLE);
        rvResorts.setVisibility(View.GONE);
        btnRetryResortList.setVisibility(View.VISIBLE);
    }
}
