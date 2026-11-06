package com.example.countryclub.registration;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.countryclub.MainActivity;
import com.example.countryclub.R;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RegistrationFragment extends Fragment {

    private EditText etFirstName, etLastName, etEmail;
    private EditText etContact, etUsername, etBirthDate;
    private EditText etCountry, etState, etCity, etAddress;
    private EditText etPassword, etConfirmPassword;

    private RadioGroup rgGender;
    private Button btnRegister;

    private RegistrationApiService registrationApiService;

    public RegistrationFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_registration,
                container,
                false
        );

        // Personal details
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etEmail = view.findViewById(R.id.etEmail);
        etContact = view.findViewById(R.id.etContact);
        etUsername = view.findViewById(R.id.etUsername);
        etBirthDate = view.findViewById(R.id.etBirthDate);

        // Gender
        rgGender = view.findViewById(R.id.rgGender);

        // Address details
        etCountry = view.findViewById(R.id.etCountry);
        etState = view.findViewById(R.id.etState);
        etCity = view.findViewById(R.id.etCity);
        etAddress = view.findViewById(R.id.etAddress);

        // Account security
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword =
                view.findViewById(R.id.etConfirmPassword);

        btnRegister = view.findViewById(R.id.btnRegister);

        registrationApiService = new RegistrationApiService();

        etBirthDate.setOnClickListener(v -> showDatePicker());

        btnRegister.setOnClickListener(v -> validateForm());

        return view;
    }

    // --------------------------------------------
    // DATE PICKER
    // --------------------------------------------

    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        requireContext(),
                        (datePicker, year, month, day) -> {

                            Calendar selectedDate =
                                    Calendar.getInstance();

                            selectedDate.set(year, month, day);

                            SimpleDateFormat dateFormat =
                                    new SimpleDateFormat(
                                            "yyyy-MM-dd",
                                            Locale.US
                                    );

                            etBirthDate.setText(
                                    dateFormat.format(
                                            selectedDate.getTime()
                                    )
                            );
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

        datePickerDialog.getDatePicker().setMaxDate(
                System.currentTimeMillis()
        );

        datePickerDialog.show();
    }

    // --------------------------------------------
    // FORM VALIDATION
    // --------------------------------------------

    private void validateForm() {

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();

        String country = etCountry.getText().toString().trim();
        String state = etState.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        String password = etPassword.getText().toString();
        String confirmPassword =
                etConfirmPassword.getText().toString();

        // First name
        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return;
        }

        if (!firstName.matches("[\\p{L}][\\p{L} '\\-]*")) {
            etFirstName.setError("Enter a valid first name");
            etFirstName.requestFocus();
            return;
        }

        // Last name
        if (TextUtils.isEmpty(lastName)) {
            etLastName.setError("Last name is required");
            etLastName.requestFocus();
            return;
        }

        if (!lastName.matches("[\\p{L}][\\p{L} '\\-]*")) {
            etLastName.setError("Enter a valid last name");
            etLastName.requestFocus();
            return;
        }

        // Email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        // Contact
        if (TextUtils.isEmpty(contact)) {
            etContact.setError("Contact number is required");
            etContact.requestFocus();
            return;
        }

        if (!contact.matches("[6-9][0-9]{9}")) {
            etContact.setError(
                    "Enter a valid 10-digit Indian mobile number"
            );
            etContact.requestFocus();
            return;
        }

        // Username
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (!username.matches("[A-Za-z0-9_.]{3,30}")) {
            etUsername.setError(
                    "Use 3-30 letters, numbers, dots or underscores"
            );
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(birthDate)) {
            etBirthDate.setError("Please select your birth date");
            etBirthDate.requestFocus();
            return;
        }

        // Gender
        int selectedGenderId =
                rgGender.getCheckedRadioButtonId();

        if (selectedGenderId == -1) {
            Toast.makeText(
                    requireContext(),
                    "Please select your gender",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        RadioButton selectedGender =
                rgGender.findViewById(selectedGenderId);

        String gender = selectedGender.getText().toString();

        // Country
        if (TextUtils.isEmpty(country)) {
            etCountry.setError("Country is required");
            etCountry.requestFocus();
            return;
        }

        // State
        if (TextUtils.isEmpty(state)) {
            etState.setError("State is required");
            etState.requestFocus();
            return;
        }

        // City
        if (TextUtils.isEmpty(city)) {
            etCity.setError("City is required");
            etCity.requestFocus();
            return;
        }

        // Address
        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Address is required");
            etAddress.requestFocus();
            return;
        }

        // Password
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 8) {
            etPassword.setError(
                    "Password must contain at least 8 characters"
            );
            etPassword.requestFocus();
            return;
        }

        // Confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError(
                    "Please confirm your password"
            );
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // All validations passed
        sendRegistrationRequest(
                firstName,
                lastName,
                email,
                contact,
                username,
                password,
                country,
                state,
                city,
                address,
                birthDate,
                gender
        );
    }

    // --------------------------------------------
    // REGISTRATION API
    // --------------------------------------------

    private void sendRegistrationRequest(
            String firstName,
            String lastName,
            String email,
            String contact,
            String username,
            String password,
            String country,
            String state,
            String city,
            String address,
            String birthDate,
            String gender) {

        try {

            JSONObject requestBody = new JSONObject();

            requestBody.put("first_name", firstName);
            requestBody.put("last_name", lastName);
            requestBody.put("email", email);
            requestBody.put("contact", contact);
            requestBody.put("username", username);
            requestBody.put("password", password);
            requestBody.put("country", country);
            requestBody.put("state", state);
            requestBody.put("city", city);
            requestBody.put("address", address);
            requestBody.put("birth_date", birthDate);
            requestBody.put("gender", gender);

            btnRegister.setEnabled(false);
            btnRegister.setText("REGISTERING...");

            registrationApiService.registerUser(
                    requestBody,
                    new RegistrationApiService.RegistrationCallback() {

                        @Override
                        public void onSuccess(
                                String message,
                                JSONObject response) {

                            if (!isAdded()) return;

                            btnRegister.setEnabled(true);
                            btnRegister.setText("REGISTER");

                            Toast.makeText(
                                    requireContext(),
                                    "Registration successful!",
                                    Toast.LENGTH_LONG
                            ).show();

                            // Automatically save login session
                            if (requireActivity() instanceof MainActivity) {

                                ((MainActivity) requireActivity())
                                        .onLoginSuccessful();
                            }
                        }

                        @Override
                        public void onError(String message) {

                            if (!isAdded()) return;

                            btnRegister.setEnabled(true);
                            btnRegister.setText("REGISTER");

                            String errorMessage = message == null ? "" : message.trim();

                            String lowerMessage = errorMessage.toLowerCase(Locale.ROOT);

                            if (lowerMessage.contains("already registered")
                                    || lowerMessage.contains("email already exists")
                                    || lowerMessage.contains("email already taken")
                                    || lowerMessage.contains("duplicate email")
                                    || lowerMessage.contains("email has already been used")) {

                                etEmail.setError("This email is already registered");
                                etEmail.requestFocus();

                                Toast.makeText(
                                        requireContext(),
                                        "This email address is already registered. Please login or use another email.",
                                        Toast.LENGTH_LONG
                                ).show();

                            } else {

                                Toast.makeText(
                                        requireContext(),
                                        errorMessage.isEmpty()
                                                ? "Registration failed. Please try again."
                                                : errorMessage,
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                    }
            );

        } catch (Exception e) {

            btnRegister.setEnabled(true);
            btnRegister.setText("REGISTER");

            Toast.makeText(
                    requireContext(),
                    "Unable to prepare registration request",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // --------------------------------------------
    // LOAD FRAGMENT
    // --------------------------------------------

    public static void load(FragmentActivity activity) {

        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new RegistrationFragment()
                )
                .commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (registrationApiService != null) {
            registrationApiService.shutdown();
        }
    }
}
