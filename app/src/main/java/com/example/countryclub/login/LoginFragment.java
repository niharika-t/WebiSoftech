package com.example.countryclub.login;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.countryclub.MainActivity;
import com.example.countryclub.R;
import com.example.countryclub.registration.RegistrationFragment;

public class LoginFragment extends Fragment {

    private EditText contact, password, resetContact;
    private EditText  newPassword, confirmPassword;

    private LinearLayout loginSection, resetSection;

    private Button btnLogin;

    private boolean isResetScreen = false;

    private LoginApiService loginApiService;

    public LoginFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_login, container, false);

        initializeViews(view);
        setupListeners(view);

        loginApiService = new LoginApiService();

        return view;
    }

    private void initializeViews(View view) {

        contact = view.findViewById(R.id.etLoginContact);
        password = view.findViewById(R.id.etLoginPassword);

        resetContact = view.findViewById(R.id.etResetContact);
        newPassword = view.findViewById(R.id.etNewPassword);
        confirmPassword = view.findViewById(R.id.etConfirmNewPassword);

        loginSection = view.findViewById(R.id.loginSection);
        resetSection = view.findViewById(R.id.resetSection);

        btnLogin = view.findViewById(R.id.btnLogin);
    }

    private void setupListeners(View view) {

        Button btnReset = view.findViewById(R.id.btnResetPassword);

        TextView forgot = view.findViewById(R.id.tvForgotPassword);
        TextView back = view.findViewById(R.id.tvBackToLogin);
        TextView register = view.findViewById(R.id.tvCreateAccount);

        btnLogin.setOnClickListener(v -> login());

        btnReset.setOnClickListener(v -> resetPassword());

        forgot.setOnClickListener(v -> showResetScreen());

        back.setOnClickListener(v -> showLoginScreen());

        register.setOnClickListener(v -> openRegistration());
    }

    private void showResetScreen() {

        resetContact.setText(contact.getText().toString().trim());

        isResetScreen = true;

        loginSection.setVisibility(View.GONE);
        resetSection.setVisibility(View.VISIBLE);
    }

    private void showLoginScreen() {

        isResetScreen = false;

        resetSection.setVisibility(View.GONE);
        loginSection.setVisibility(View.VISIBLE);
    }

    private void openRegistration() {

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new RegistrationFragment()
                )
                .addToBackStack("registration")
                .commit();
    }

    private boolean isValidEmail(String email) {

        return !Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @SuppressLint("SetTextI18n")
    private void login() {

        String email = contact.getText().toString().trim();

        String pass = password.getText().toString();

        if (email.isEmpty()) {

            contact.setError("Email address is required");
            contact.requestFocus();
            return;
        }

        if (isValidEmail(email)) {

            contact.setError("Enter a valid email address");
            contact.requestFocus();
            return;
        }

        if (pass.trim().isEmpty()) {

            password.setError("Password is required");
            password.requestFocus();
            return;
        }

        // Disable button while API request is running.
        btnLogin.setEnabled(false);
        btnLogin.setText("PLEASE WAIT...");

        loginApiService.login(email, pass,
                new LoginApiService.LoginCallback() {

                    @Override
                    public void onSuccess(String message,
                                          org.json.JSONObject response) {

                        if (!isAdded()) return;

                        btnLogin.setEnabled(true);
                        btnLogin.setText("LOGIN");

                        Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_LONG
                        ).show();

                        if (requireActivity() instanceof MainActivity) {
                            ((MainActivity) requireActivity()).onLoginSuccessful();
                        }
                        // Successful authentication.
                        // We can add navigation to Home/Profile after
                        // confirming the required destination.
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onError(String message) {

                        if (!isAdded()) return;

                        btnLogin.setEnabled(true);
                        btnLogin.setText("LOGIN");

                        Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void resetPassword() {

        String email = resetContact.getText().toString().trim();

        String pass = newPassword.getText().toString();

        String confirm = confirmPassword.getText().toString();

        if (isValidEmail(email)) {

            resetContact.setError("Enter a valid email address");
            resetContact.requestFocus();
            return;
        }

        if (pass.length() < 8) {

            newPassword.setError(
                    "Password must contain at least 8 characters");

            newPassword.requestFocus();
            return;
        }

        if (!pass.equals(confirm)) {

            confirmPassword.setError("Passwords do not match");
            confirmPassword.requestFocus();
            return;
        }

        Toast.makeText(
                requireContext(),
                "Connect password reset API",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(),
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {

                                if (isResetScreen) {

                                    showLoginScreen();

                                } else {

                                    setEnabled(false);

                                    requireActivity()
                                            .getOnBackPressedDispatcher()
                                            .onBackPressed();
                                }
                            }
                        });
    }

    public static void load(FragmentActivity activity) {

        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new LoginFragment()
                )
                .addToBackStack("login")
                .commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (loginApiService != null) {
            loginApiService.shutdown();
        }
    }
}
