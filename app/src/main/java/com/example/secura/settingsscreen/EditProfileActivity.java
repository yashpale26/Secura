package com.example.secura.settingsscreen; // Create a new package 'profile'

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.CompoundButton; // Import for Switch
import com.google.android.material.switchmaterial.SwitchMaterial; // Import for Material Switch

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.secura.R; // Ensure this is correct for your R file
import com.example.secura.splashloginregister.DatabaseHelper; // Import your DatabaseHelper
import com.example.secura.splashloginregister.LoginActivity; // Import LoginActivity for shared preferences key

import de.hdodenhof.circleimageview.CircleImageView; // For circular image view

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private CircleImageView profilePicImageView;
    private ImageView editProfilePicIcon;
    private EditText etName, etUsername;
    private Button btnSaveChanges;
    private SwitchMaterial permissionSwitch; // Declare the Switch

    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private Uri selectedImageUri; // To store the URI of the selected image
    private String originalUsername; // To store the username before editing

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

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
                onBackPressed(); // Go back to ProfileActivity
            }
        });

        // Initialize views
        profilePicImageView = findViewById(R.id.profile_pic_image_view);
        editProfilePicIcon = findViewById(R.id.edit_profile_pic_icon);
        etName = findViewById(R.id.et_name);
        etUsername = findViewById(R.id.et_username);
        btnSaveChanges = findViewById(R.id.btn_save_changes);
        permissionSwitch = findViewById(R.id.permission_switch); // Initialize the Switch

        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        // Load current user data
        loadCurrentUserData();

        // Set initial state of the switch based on current permission status
        updatePermissionSwitchState();

        // Set click listener for profile picture edit icon
        editProfilePicIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Only open image chooser if the permission switch is enabled
                if (permissionSwitch.isChecked()) {
                    openImageChooser();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Please enable photo access permission first.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set listener for the permission switch
        permissionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // If switch is turned ON, request permission
                    checkAndRequestPermissions();
                } else {
                    // If switch is turned OFF, no action needed other than disabling image picking
                    Toast.makeText(EditProfileActivity.this, "Photo access permission disabled.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set click listener for Save Changes button
        btnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
            }
        });
    }

    private void loadCurrentUserData() {
        originalUsername = sharedPreferences.getString(LoginActivity.KEY_LOGGED_IN_USERNAME, null);

        if (originalUsername != null) {
            Cursor cursor = null;
            try {
                cursor = databaseHelper.getUserDetails(originalUsername);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameColumnIndex = cursor.getColumnIndex(DatabaseHelper.COL_NAME);
                    int usernameColumnIndex = cursor.getColumnIndex(DatabaseHelper.COL_USERNAME);
                    int profilePicUriColumnIndex = cursor.getColumnIndex(DatabaseHelper.COL_PROFILE_PIC_URI);

                    String name = (nameColumnIndex != -1) ? cursor.getString(nameColumnIndex) : "";
                    String username = (usernameColumnIndex != -1) ? cursor.getString(usernameColumnIndex) : "";
                    String profilePicUriString = (profilePicUriColumnIndex != -1) ? cursor.getString(profilePicUriColumnIndex) : "";

                    etName.setText(name);
                    etUsername.setText(username);

                    if (profilePicUriString != null && !profilePicUriString.isEmpty()) {
                        selectedImageUri = Uri.parse(profilePicUriString);
                        profilePicImageView.setImageURI(selectedImageUri);
                    } else {
                        profilePicImageView.setImageResource(R.drawable.ic_default_profile_pic);
                    }
                } else {
                    Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
                    finish(); // Go back if data not found
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error loading current profile data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show();
            finish(); // Go back if no user logged in
        }
    }

    private void updatePermissionSwitchState() {
        boolean hasPermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else { // Android 12 and below
            hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        permissionSwitch.setChecked(hasPermission);
    }

    private void checkAndRequestPermissions() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else { // Android 12 and below
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, ensure switch is checked
            permissionSwitch.setChecked(true);
            Toast.makeText(this, "Photo access permission already granted.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Photo access permission granted!", Toast.LENGTH_SHORT).show();
                permissionSwitch.setChecked(true); // Update switch state
                // Optionally, open image chooser immediately after permission is granted
                // openImageChooser();
            } else {
                Toast.makeText(this, "Permission denied to access photos.", Toast.LENGTH_SHORT).show();
                permissionSwitch.setChecked(false); // Update switch state
            }
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            profilePicImageView.setImageURI(selectedImageUri);
            Toast.makeText(this, "Image selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveChanges() {
        String newName = etName.getText().toString().trim();
        String newUsername = etUsername.getText().toString().trim();
        String profilePicUriString = (selectedImageUri != null) ? selectedImageUri.toString() : "";

        if (newName.isEmpty() || newUsername.isEmpty()) {
            Toast.makeText(this, "Name and Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the old password hash to keep it the same
        String passwordHash = databaseHelper.getPasswordHash(originalUsername);
        if (passwordHash == null) {
            Toast.makeText(this, "Error: Could not retrieve old password. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the user details in the database
        boolean isUpdated = databaseHelper.updateUserDetails(originalUsername, newName, newUsername, profilePicUriString);

        if (isUpdated) {
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

            // Update SharedPreferences if username changed
            if (!newUsername.equals(originalUsername)) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(LoginActivity.KEY_LOGGED_IN_USERNAME, newUsername); // Update logged-in username
                // If you store password in shared preferences (for remember me), you might need to update it too
                // editor.putString("password", passwordHash); // This is the hashed password from DB
                editor.apply();
            }
            finish(); // Go back to ProfileActivity
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        } else {
            Toast.makeText(this, "Failed to update profile. Username might already exist or an error occurred.", Toast.LENGTH_LONG).show();
        }
    }
}
