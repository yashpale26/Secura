package com.example.secura.settingsscreen; // Adjust package as needed

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.secura.R;
import com.example.secura.splashloginregister.DatabaseHelper;

public class EditSingleCredentialActivity extends AppCompatActivity {

    private EditText etName, etUsername, etPassword;
    private Button btnSaveChanges;
    private DatabaseHelper databaseHelper;
    private String originalUsername; // The username passed from the previous activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_single_credential);

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
                onBackPressed(); // Go back to EditDeleteCredentialsActivity
            }
        });

        etName = findViewById(R.id.et_edit_name);
        etUsername = findViewById(R.id.et_edit_username);
        etPassword = findViewById(R.id.et_edit_password);
        btnSaveChanges = findViewById(R.id.btn_save_changes_single);

        databaseHelper = new DatabaseHelper(this);

        // Get the username to edit from the intent
        if (getIntent().hasExtra("username_to_edit")) {
            originalUsername = getIntent().getStringExtra("username_to_edit");
            loadCredentialData(originalUsername);
        } else {
            Toast.makeText(this, "No user selected for editing.", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if no username is provided
        }

        btnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEditedCredentials();
            }
        });
    }

    private void loadCredentialData(String username) {
        Cursor cursor = null;
        try {
            // Use existing getUserDetails or create a new one to get all fields including password
            cursor = databaseHelper.getAllUsers(); // getAllUsers returns name, username, password_hash
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int nameColumnIndex = cursor.getColumnIndex(DatabaseHelper.COL_NAME);
                    int usernameColumnIndex = cursor.getColumnIndex(DatabaseHelper.COL_USERNAME);
                    int passwordHashColumnIndex = cursor.getColumnIndex(DatabaseHelper.COL_PASSWORD_HASH);

                    String currentUsername = (usernameColumnIndex != -1) ? cursor.getString(usernameColumnIndex) : "";

                    if (currentUsername.equals(username)) {
                        String name = (nameColumnIndex != -1) ? cursor.getString(nameColumnIndex) : "";
                        String password = (passwordHashColumnIndex != -1) ? cursor.getString(passwordHashColumnIndex) : "";

                        etName.setText(name);
                        etUsername.setText(currentUsername);
                        etPassword.setText(password);
                        return; // Found the user, exit
                    }
                }
                Toast.makeText(this, "User data not found for " + username, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error loading user data.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading credential data: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void saveEditedCredentials() {
        String newName = etName.getText().toString().trim();
        String newUsername = etUsername.getText().toString().trim();
        String newPassword = etPassword.getText().toString().trim();

        if (newName.isEmpty() || newUsername.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if username already exists for another user (if username is changed)
        if (!newUsername.equals(originalUsername)) {
            Cursor checkCursor = null;
            try {
                // This check needs to be robust. A direct checkUser(newUsername, anyPassword)
                // might return true if the username exists with *any* password.
                // A better approach is to have a specific method like checkUsernameExists(username)
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                checkCursor = db.rawQuery("SELECT " + DatabaseHelper.COL_USERNAME + " FROM " + DatabaseHelper.TABLE_USERS + " WHERE " + DatabaseHelper.COL_USERNAME + " = ?", new String[]{newUsername});
                if (checkCursor != null && checkCursor.getCount() > 0) {
                    Toast.makeText(this, "Username already exists. Please choose a different one.", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error checking username existence: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
                return;
            } finally {
                if (checkCursor != null) {
                    checkCursor.close();
                }
            }
        }


        // Call the new method in DatabaseHelper to update credentials
        boolean isUpdated = databaseHelper.updateUserCredentials(originalUsername, newName, newUsername, newPassword);

        if (isUpdated) {
            Toast.makeText(this, "Account updated successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Go back to EditDeleteCredentialsActivity
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        } else {
            Toast.makeText(this, "Failed to update account. An error occurred or username might already exist.", Toast.LENGTH_LONG).show();
        }
    }
}
