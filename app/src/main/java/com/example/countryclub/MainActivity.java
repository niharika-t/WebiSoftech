package com.example.countryclub;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.countryclub.blog.BlogFragment;
import com.example.countryclub.fitness.FitnessFragment;
import com.example.countryclub.holidays.HolidaysFragment;
import com.example.countryclub.home.HomeFragment;
import com.example.countryclub.locate.LocateUsFragment;
import com.example.countryclub.login.LoginFragment;
import com.example.countryclub.payment_details.PaymentDetailsFragment;
import com.example.countryclub.privacy_policy.PrivacyPolicyFragment;
import com.example.countryclub.profile.ProfileFragment;
import com.example.countryclub.refund_policy.RefundPolicyFragment;
import com.example.countryclub.resorts.ResortsFragment;
import com.example.countryclub.subscriptions.SubscriptionFragment;
import com.example.countryclub.terms_and_conditions.TermsAndConditionFragment;
import com.example.countryclub.trip_destination.TripDestinationFragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private SharedPreferences sessionPreferences;

    private static final String PREF_NAME = "CountryClubSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    // Country Club contact details
    private static final String CLUB_PHONE = "+918882871871";
    private static final String CLUB_EMAIL = "booking@theholidaysclub.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen.installSplashScreen(this);

        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);

        // Enable edge-to-edge compatibility
        WindowCompat.setDecorFitsSystemWindows(
                getWindow(),
                false
        );

        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        // Apply system bar styling
        setupSystemBars();

        // Initialize session preferences
        sessionPreferences = getSharedPreferences(
                PREF_NAME,
                MODE_PRIVATE
        );

        // Set drawer menu according to login status
        refreshDrawerMenu();

        // Open navigation drawer
        toolbar.setNavigationOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        // Handle toolbar Call and Email icons
        toolbar.setOnMenuItemClickListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.action_phone) {

                openPhoneDialer();
                return true;

            } else if (itemId == R.id.action_mail) {

                openEmailApp();
                return true;
            }

            return false;
        });

        // Navigation drawer item selection
        navigationView.setNavigationItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {

                loadHomeFragment();

            } else if (itemId == R.id.nav_login) {

                LoginFragment.load(this);

            } else if (itemId == R.id.nav_logout) {

                logoutUser();

            } else if (itemId == R.id.profile) {

                ProfileFragment.load(this);

            } else if (itemId == R.id.nav_events) {

                FitnessFragment.load(this);

            } else if (itemId == R.id.nav_locate_us) {

                LocateUsFragment.load(this);

            } else if (itemId == R.id.holidays) {

                HolidaysFragment.load(this);

            } else if (itemId == R.id.blog) {

                BlogFragment.load(this);

            } else if (itemId == R.id.payment_details) {

                PaymentDetailsFragment.load(this);

            } else if (itemId == R.id.privacy_policy) {

                PrivacyPolicyFragment.load(this);

            } else if (itemId == R.id.nav_resorts) {

                ResortsFragment.load(this);

            } else if (itemId == R.id.refund_policy) {

                RefundPolicyFragment.load(this);

            } else if (itemId == R.id.subscription) {

                SubscriptionFragment.load(this);

            } else if (itemId == R.id.terms_and_conditions) {

                TermsAndConditionFragment.load(this);

            } else if (itemId == R.id.nav_trip) {

                TripDestinationFragment.load(this);
            }

            if (itemId != R.id.nav_logout) {
                navigationView.setCheckedItem(itemId);
            }

            drawerLayout.closeDrawer(GravityCompat.START);

            return true;
        });

        // Always open Home when the app launches for the first time
        if (savedInstanceState == null) {

            loadHomeFragment();

            navigationView.setCheckedItem(R.id.nav_home);
        }

        // Handle device back button
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (drawerLayout.isDrawerOpen(
                                GravityCompat.START)) {

                            drawerLayout.closeDrawer(
                                    GravityCompat.START
                            );

                            return;
                        }

                        if (!(getSupportFragmentManager()
                                .findFragmentById(R.id.fragmentContainer)
                                instanceof HomeFragment)) {

                            loadHomeFragment();

                            navigationView.setCheckedItem(
                                    R.id.nav_home
                            );

                        } else {

                            finish();
                        }
                    }
                }
        );
    }

    // ==========================================
    // TOOLBAR CONTACT FUNCTIONALITY
    // ==========================================

    // Open phone dialer with Country Club number
    private void openPhoneDialer() {

        try {

            Intent intent = new Intent(Intent.ACTION_DIAL);

            intent.setData(
                    Uri.parse("tel:" + CLUB_PHONE)
            );

            startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to open phone dialer",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Open email application with Country Club email
    private void openEmailApp() {

        try {

            Intent intent = new Intent(Intent.ACTION_SENDTO);

            intent.setData(
                    Uri.parse("mailto:" + CLUB_EMAIL)
            );

            intent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Inquiry - Holiday Service Club"
            );

            startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "No email application found on this device",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // ==========================================
    // SYSTEM BAR STYLING
    // ==========================================

    // Platform-independent system bar styling
    private void setupSystemBars() {

        int primaryColor = ContextCompat.getColor(
                this,
                R.color.colorPrimary
        );

        // Root background appears behind transparent status bar
        drawerLayout.setBackgroundColor(primaryColor);

        View mainLayout = findViewById(R.id.main);

        // Preserve original padding
        final int initialLeft = mainLayout.getPaddingLeft();
        final int initialTop = mainLayout.getPaddingTop();
        final int initialRight = mainLayout.getPaddingRight();
        final int initialBottom = mainLayout.getPaddingBottom();

        // Apply system bar insets to main content only
        ViewCompat.setOnApplyWindowInsetsListener(
                mainLayout,
                (view, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            initialLeft + systemBars.left,
                            initialTop + systemBars.top,
                            initialRight + systemBars.right,
                            initialBottom + systemBars.bottom
                    );

                    return insets;
                }
        );

        // Configure system bar icons
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(
                        getWindow(),
                        getWindow().getDecorView()
                );

        // Keep light icons on the primary-colored status bar
        controller.setAppearanceLightStatusBars(false);
    }

    // ==========================================
    // LOGIN SESSION MANAGEMENT
    // ==========================================

    // Check login status
    private boolean isLoggedIn() {

        return sessionPreferences.getBoolean(
                KEY_IS_LOGGED_IN,
                false
        );
    }

    // Update drawer items according to login status
    private void refreshDrawerMenu() {

        boolean loggedIn = isLoggedIn();

        // Always visible for guests and logged-in users
        navigationView.getMenu()
                .findItem(R.id.nav_resorts).setVisible(true);

        navigationView.getMenu()
                .findItem(R.id.nav_events).setVisible(true);

        navigationView.getMenu()
                .findItem(R.id.nav_trip).setVisible(true);

        navigationView.getMenu()
                .findItem(R.id.nav_home).setVisible(true);

        // Visible only after successful login
        navigationView.getMenu()
                .findItem(R.id.profile).setVisible(loggedIn);

        navigationView.getMenu()
                .findItem(R.id.nav_locate_us).setVisible(loggedIn);

        navigationView.getMenu()
                .findItem(R.id.holidays).setVisible(loggedIn);

        navigationView.getMenu()
                .findItem(R.id.subscription).setVisible(loggedIn);

        navigationView.getMenu()
                .findItem(R.id.payment_details).setVisible(loggedIn);

        navigationView.getMenu()
                .findItem(R.id.blog).setVisible(loggedIn);

        navigationView.getMenu()
                .findItem(R.id.privacy_policy).setVisible(loggedIn);

        navigationView.getMenu()
                .findItem(R.id.terms_and_conditions).setVisible(loggedIn);

        navigationView.getMenu()
                .findItem(R.id.refund_policy).setVisible(loggedIn);

        // Switch Login and Logout
        navigationView.getMenu()
                .findItem(R.id.nav_login).setVisible(!loggedIn);

        navigationView.getMenu()
                .findItem(R.id.nav_logout).setVisible(loggedIn);
    }

    // Called by LoginFragment after successful API login
    public void onLoginSuccessful() {

        sessionPreferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();

        refreshDrawerMenu();

        loadHomeFragment();

        navigationView.setCheckedItem(R.id.nav_home);
    }

    // Logout user
    private void logoutUser() {

        sessionPreferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .apply();

        refreshDrawerMenu();

        loadHomeFragment();

        navigationView.setCheckedItem(R.id.nav_home);

        Toast.makeText(
                this,
                "Logged out successfully",
                Toast.LENGTH_SHORT
        ).show();
    }

    // ==========================================
    // HOME FRAGMENT
    // ==========================================

    private void loadHomeFragment() {

        HomeFragment homeFragment = new HomeFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        homeFragment
                )
                .commit();
    }
}
