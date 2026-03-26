package com.example.secura.settingsscreen; // Create a new package 'profile'

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.secura.R; // Ensure this is correct for your R file
import com.example.secura.splashloginregister.DatabaseHelper; // Import your DatabaseHelper
import com.example.secura.splashloginregister.LoginActivity; // Import LoginActivity for logout

import de.hdodenhof.circleimageview.CircleImageView; // For circular image view

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView profilePicImageView;
    private TextView tvName, tvUsername;
    private Button btnEditProfile, btnLogout;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title
        }

        // Set up back button click listener
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Go back to the previous activity (SettingsFragment)
            }
        });

        // Initialize views
        profilePicImageView = findViewById(R.id.profile_pic_image_view);
        tvName = findViewById(R.id.tv_name);
        tvUsername = findViewById(R.id.tv_username);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnLogout = findViewById(R.id.btn_logout);

        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        // Load user data
        loadUserProfile();

        // Set click listener for Edit Profile button
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        // Set click listener for Logout button
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear login session
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear(); // Clear all stored login preferences
                editor.apply();

                // Redirect to LoginActivity
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear back stack
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                finish(); // Finish current activity
                Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile data when returning to this activity (e.g., after editing)
        loadUserProfile();
    }

    private void loadUserProfile() {
        // Use the KEY_LOGGED_IN_USERNAME from LoginActivity
        String loggedInUsername = sharedPreferences.getString(LoginActivity.KEY_LOGGED_IN_USERNAME, null);

        if (loggedInUsername != null) {
            Cursor cursor = null;
            try {
                cursor = databaseHelper.getUserDetails(loggedInUsername);
                if (cursor != null && cursor.moveToFirst()) {
                    // It's safer to get column index by name to avoid issues if column order changes
                    int nameColumnIndex = cursor.getColumnIndex(DatabaseHelper.COL_NAME);
                    int usernameColumnIndex = cursor.getColumnIndex(DatabaseHelper.COL_USERNAME);
                    int profilePicUriColumnIndex = cursor.getColumnIndex(DatabaseHelper.COL_PROFILE_PIC_URI);

                    String name = (nameColumnIndex != -1) ? cursor.getString(nameColumnIndex) : "N/A";
                    String username = (usernameColumnIndex != -1) ? cursor.getString(usernameColumnIndex) : "N/A";
                    String profilePicUriString = (profilePicUriColumnIndex != -1) ? cursor.getString(profilePicUriColumnIndex) : "";

                    tvName.setText("Name: " + name);
                    tvUsername.setText("Username: " + username);

                    // Load profile picture if URI exists
                    if (profilePicUriString != null && !profilePicUriString.isEmpty()) {
                        Uri profilePicUri = Uri.parse(profilePicUriString);
                        profilePicImageView.setImageURI(profilePicUri);
                    } else {
                        // Set a default placeholder if no image is set
                        profilePicImageView.setImageResource(R.drawable.ic_default_profile_pic);
                    }
                } else {
                    Toast.makeText(this, "User data not found for logged-in user.", Toast.LENGTH_SHORT).show();
                    // Handle case where user data is not found (e.g., redirect to login)
                    logoutUser();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            Toast.makeText(this, "No user logged in. Please log in.", Toast.LENGTH_SHORT).show();
            // Redirect to login if no user is logged in
            logoutUser();
        }
    }

    private void logoutUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
