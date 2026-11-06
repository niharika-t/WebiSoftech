
package com.example.countryclub.profile;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.countryclub.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileFragment extends Fragment {

    private EditText firstName, lastName, email, contact;
    private EditText address, aadhaar, birthDate, age;

    private AutoCompleteTextView country, state, city, gender;
    private ImageView profileImage;
    private Button saveButton;

    private ActivityResultLauncher<String> imagePicker;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private static final String API_BASE =
            "https://countriesnow.space/api/v0.1";

    private boolean countriesLoading = false;
    private boolean countriesLoaded = false;

    public ProfileFragment() {
        // Required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && profileImage != null) {
                        profileImage.setImageURI(uri);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_profile,
                container,
                false
        );

        initializeViews(view);
        setupImagePicker();
        setupGender();
        setupBirthDate();
        setupLocationDropdowns();
        setupSaveButton();

        return view;
    }

    // 1. Initialize views
    private void initializeViews(View view) {
        firstName = view.findViewById(R.id.etFirstName);
        lastName = view.findViewById(R.id.etLastName);
        email = view.findViewById(R.id.etEmail);
        contact = view.findViewById(R.id.etContact);
        address = view.findViewById(R.id.etAddress);
        aadhaar = view.findViewById(R.id.etAadhaar);
        birthDate = view.findViewById(R.id.etBirthDate);
        age = view.findViewById(R.id.etAge);

        country = view.findViewById(R.id.dropdownCountry);
        state = view.findViewById(R.id.dropdownState);
        city = view.findViewById(R.id.dropdownCity);
        gender = view.findViewById(R.id.dropdownGender);

        profileImage = view.findViewById(R.id.profileImage);
        saveButton = view.findViewById(R.id.btnSaveProfile);
    }

    // 2. Select profile photo
    private void setupImagePicker() {
        profileImage.setOnClickListener(
                v -> imagePicker.launch("image/*")
        );
    }

    // 3. Gender dropdown
    private void setupGender() {
        String[] genders = {
                "Male",
                "Female",
                "Other",
                "Prefer not to say"
        };

        setupDropdown(gender, genders);
    }

    // 4. Birth date calendar and automatic age
    private void setupBirthDate() {
        birthDate.setFocusable(false);
        birthDate.setClickable(true);

        birthDate.setOnClickListener(v -> {
            Calendar today = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
                    (picker, year, month, day) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.clear();
                        selected.set(year, month, day);

                        if (selected.after(today)) {
                            birthDate.setError(
                                    "Birth date cannot be in the future"
                            );
                            return;
                        }

                        birthDate.setError(null);

                        birthDate.setText(String.format(
                                Locale.getDefault(),
                                "%02d/%02d/%04d",
                                day, month + 1, year
                        ));

                        int calculatedAge =
                                today.get(Calendar.YEAR) - year;

                        if (today.get(Calendar.MONTH) < month ||
                                (today.get(Calendar.MONTH) == month &&
                                        today.get(Calendar.DAY_OF_MONTH) < day)) {
                            calculatedAge--;
                        }

                        age.setText(String.valueOf(calculatedAge));
                    },
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)
            );

            dialog.getDatePicker().setMaxDate(
                    System.currentTimeMillis()
            );

            dialog.show();
        });
    }

    // 5. Country -> State -> City dropdowns
    private void setupLocationDropdowns() {
        setupDropdown(country, new String[0]);
        setupDropdown(state, new String[0]);
        setupDropdown(city, new String[0]);

        state.setEnabled(false);
        city.setEnabled(false);

        country.setOnItemClickListener((parent, v, position, id) -> {
            String selectedCountry =
                    parent.getItemAtPosition(position).toString();

            state.setText("", false);
            city.setText("", false);

            state.setEnabled(false);
            city.setEnabled(false);

            setupDropdown(state, new String[0]);
            setupDropdown(city, new String[0]);

            loadStates(selectedCountry);
        });

        state.setOnItemClickListener((parent, v, position, id) -> {
            String selectedState =
                    parent.getItemAtPosition(position).toString();

            city.setText("", false);
            city.setEnabled(false);
            setupDropdown(city, new String[0]);

            loadCities(
                    country.getText().toString(),
                    selectedState
            );
        });

        loadCountries();
    }

    // Reusable dropdown setup
    private void setupDropdown(
            AutoCompleteTextView dropdown,
            String[] items) {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                items
        );

        dropdown.setAdapter(adapter);
        dropdown.setThreshold(0);
        dropdown.setKeyListener(null);
        dropdown.setFocusable(true);
        dropdown.setFocusableInTouchMode(true);

        dropdown.setOnClickListener(v -> {
            if (dropdown.isEnabled()
                    && dropdown.getAdapter() != null
                    && dropdown.getAdapter().getCount() > 0) {
                dropdown.showDropDown();
            }
        });
    }

    // 6. Load countries
    private void loadCountries() {
        if (countriesLoading || countriesLoaded) return;

        countriesLoading = true;

        requestApi("/countries/iso", null, json -> {
            countriesLoading = false;

            if (json.optBoolean("error", true)) {
                showMessage("Unable to load countries");
                return;
            }

            JSONArray data = json.optJSONArray("data");

            if (data == null) {
                showMessage("Invalid country data");
                return;
            }

            String[] items = new String[data.length()];

            for (int i = 0; i < data.length(); i++) {
                JSONObject item = data.optJSONObject(i);

                items[i] = item == null
                        ? ""
                        : item.optString("name", "");
            }

            setupDropdown(country, items);
            countriesLoaded = items.length > 0;

            if (!countriesLoaded) {
                showMessage("No countries available");
            }
        }, () -> {
            countriesLoading = false;
            showMessage("Check your internet connection");
        });
    }

    // 7. Load states for selected country
    private void loadStates(String countryName) {
        try {
            JSONObject body = new JSONObject();
            body.put("country", countryName);

            requestApi("/countries/states", body, json -> {
                if (json.optBoolean("error", true)) {
                    showMessage("Unable to load states");
                    return;
                }

                JSONObject data = json.optJSONObject("data");
                JSONArray states = data == null
                        ? null : data.optJSONArray("states");

                if (states == null) {
                    showMessage("No states available");
                    return;
                }

                String[] items = new String[states.length()];

                for (int i = 0; i < states.length(); i++) {
                    JSONObject item = states.optJSONObject(i);

                    items[i] = item == null
                            ? ""
                            : item.optString("name", "");
                }

                setupDropdown(state, items);
                state.setEnabled(items.length > 0);

                if (items.length == 0) {
                    showMessage("No states found");
                }
            }, () -> showMessage("Unable to load states"));

        } catch (Exception e) {
            showMessage("Unable to load states");
        }
    }

    // 8. Load cities for selected state
    private void loadCities(
            String countryName,
            String stateName) {

        try {
            JSONObject body = new JSONObject();
            body.put("country", countryName);
            body.put("state", stateName);

            requestApi("/countries/state/cities", body, json -> {
                if (json.optBoolean("error", true)) {
                    showMessage("Unable to load cities");
                    return;
                }

                JSONArray data = json.optJSONArray("data");

                if (data == null) {
                    showMessage("No cities available");
                    return;
                }

                String[] items = new String[data.length()];

                for (int i = 0; i < data.length(); i++) {
                    items[i] = data.optString(i, "");
                }

                setupDropdown(city, items);
                city.setEnabled(items.length > 0);

                if (items.length == 0) {
                    showMessage("No cities found");
                }
            }, () -> showMessage("Unable to load cities"));

        } catch (Exception e) {
            showMessage("Unable to load cities");
        }
    }

    // 9. Reusable API request
    private interface ApiCallback {
        void onSuccess(JSONObject json);
    }

    private interface ErrorCallback {
        void onError();
    }

    private void requestApi(
            String endpoint,
            JSONObject body,
            ApiCallback callback,
            ErrorCallback errorCallback) {

        executor.execute(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(API_BASE + endpoint);

                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty(
                        "Accept", "application/json"
                );

                if (body != null) {
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty(
                            "Content-Type", "application/json"
                    );
                    connection.setDoOutput(true);

                    try (OutputStream output =
                                 connection.getOutputStream()) {
                        output.write(body.toString().getBytes(
                                StandardCharsets.UTF_8
                        ));
                    }
                }

                int status = connection.getResponseCode();

                if (status < 200 || status >= 300) {
                    throw new Exception("HTTP " + status);
                }

                StringBuilder response = new StringBuilder();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream(),
                                StandardCharsets.UTF_8
                        ))) {

                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                JSONObject json = new JSONObject(response.toString());

                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        callback.onSuccess(json);
                    }
                });

            } catch (Exception e) {
                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        errorCallback.onError();
                    }
                });

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private void showMessage(String message) {
        if (!isAdded()) return;

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    // 10. Profile validation
    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> validateProfile());
    }

    private void validateProfile() {
        String first = firstName.getText().toString().trim();
        String last = lastName.getText().toString().trim();
        String mail = email.getText().toString().trim();
        String phone = contact.getText().toString().trim();
        String id = aadhaar.getText().toString().trim();
        String dob = birthDate.getText().toString().trim();

        if (!isValidName(first)) {
            firstName.setError("Enter a valid first name");
            firstName.requestFocus();
            return;
        }

        if (!isValidName(last)) {
            lastName.setError("Enter a valid last name");
            lastName.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(mail).matches()) {
            email.setError("Enter a valid email address");
            email.requestFocus();
            return;
        }

        if (!phone.matches("[6-9][0-9]{9}")) {
            contact.setError("Enter a valid 10-digit mobile number");
            contact.requestFocus();
            return;
        }

        if (!id.matches("[0-9]{12}")) {
            aadhaar.setError("Enter a 12-digit Aadhaar number");
            aadhaar.requestFocus();
            return;
        }

        if (dob.isEmpty()
                || age.getText().toString().trim().isEmpty()) {
            birthDate.setError("Select your birth date");
            birthDate.requestFocus();
            return;
        }

        if (gender.getText().toString().trim().isEmpty()) {
            gender.setError("Select gender");
            gender.requestFocus();
            return;
        }

        if (country.getText().toString().trim().isEmpty()) {
            country.setError("Select country");
            country.requestFocus();
            return;
        }

        if (state.getText().toString().trim().isEmpty()) {
            state.setError("Select state");
            state.requestFocus();
            return;
        }

        if (city.getText().toString().trim().isEmpty()) {
            city.setError("Select city");
            city.requestFocus();
            return;
        }

        if (address.getText().toString().trim().length() < 5) {
            address.setError("Enter a complete address");
            address.requestFocus();
            return;
        }

        showMessage("Profile validated successfully");
    }

    private boolean isValidName(String name) {
        return name.matches("\\p{L}[\\p{L} '\\u2019-]{0,49}");
    }

    // Existing MainActivity navigation method
    public static void load(FragmentActivity activity) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new ProfileFragment()
                )
                .commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop pending network work when the fragment is destroyed.
        executor.shutdownNow();
    }
}
