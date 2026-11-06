
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
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.countryclub.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ResortsFragment extends Fragment {

    private RecyclerView rvResortCategories;
    private ProgressBar progressResorts;
    private TextView tvResortMessage;
    private Button btnRetryResorts;
    private View resortEmptyState;

    private ResortCategoryAdapter adapter;

    private final List<ResortCategory> categoryList =
            new ArrayList<>();

    public ResortsFragment() {
        // Required empty public constructor.
    }

    public static void load(FragmentActivity activity) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new ResortsFragment()
                )
                .addToBackStack(null)
                .commit();
    }

    // Opens a specific resort category from Home or another fragment.
    public static void openCategory(
            Fragment source,
            ResortCategory category) {

        if (source == null
                || category == null
                || !source.isAdded()) {
            return;
        }

        ResortListFragment.open(source, category);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_resorts,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        rvResortCategories =
                view.findViewById(R.id.rvResortCategories);

        progressResorts =
                view.findViewById(R.id.progressResorts);

        tvResortMessage =
                view.findViewById(R.id.tvResortMessage);

        btnRetryResorts =
                view.findViewById(R.id.btnRetryResorts);

        resortEmptyState =
                view.findViewById(R.id.resortEmptyState);

        rvResortCategories.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        adapter = new ResortCategoryAdapter(
                categoryList,
                category -> ResortListFragment.open(this, category)
        );

        rvResortCategories.setAdapter(adapter);

        btnRetryResorts.setOnClickListener(
                v -> loadCategories()
        );

        loadCategories();
    }

    private void loadCategories() {

        if (!isAdded()) {
            return;
        }

        resortEmptyState.setVisibility(View.GONE);
        progressResorts.setVisibility(View.VISIBLE);
        rvResortCategories.setVisibility(View.GONE);
        tvResortMessage.setVisibility(View.GONE);
        btnRetryResorts.setVisibility(View.GONE);

        ResortApiService.getResortCategories(
                new ResortApiService.ApiCallback<JSONArray>() {

                    @Override
                    public void onSuccess(JSONArray data) {

                        if (!isAdded() || getView() == null) {
                            return;
                        }

                        progressResorts.setVisibility(View.GONE);
                        categoryList.clear();

                        for (int i = 0; i < data.length(); i++) {

                            JSONObject object =
                                    data.optJSONObject(i);

                            if (object == null) {
                                continue;
                            }

                            String id = getValue(
                                    object,
                                    "id",
                                    "category_id"
                            );

                            String name = getValue(
                                    object,
                                    "name",
                                    "category_name",
                                    "title"
                            );

                            String image = getValue(
                                    object,
                                    "image",
                                    "image_url",
                                    "category_image"
                            );

                            if (!id.isEmpty() && !name.isEmpty()) {

                                categoryList.add(
                                        new ResortCategory(
                                                id,
                                                name,
                                                image
                                        )
                                );
                            }
                        }

                        adapter.notifyDataSetChanged();

                        if (categoryList.isEmpty()) {
                            showMessage(
                                    "No resort categories available."
                            );
                            return;
                        }

                        resortEmptyState.setVisibility(View.GONE);
                        rvResortCategories.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(String message) {

                        if (!isAdded() || getView() == null) {
                            return;
                        }

                        progressResorts.setVisibility(View.GONE);

                        showMessage(
                                "Unable to load resort categories."
                        );

                        Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private String getValue(
            JSONObject object,
            String... keys) {

        for (String key : keys) {

            String value = object.optString(key, "").trim();

            if (!value.isEmpty()
                    && !"null".equalsIgnoreCase(value)) {
                return value;
            }
        }

        return "";
    }

    private void showMessage(String message) {

        tvResortMessage.setText(message);
        progressResorts.setVisibility(View.GONE);
        rvResortCategories.setVisibility(View.GONE);
        resortEmptyState.setVisibility(View.VISIBLE);
        btnRetryResorts.setVisibility(View.VISIBLE);
    }
}
