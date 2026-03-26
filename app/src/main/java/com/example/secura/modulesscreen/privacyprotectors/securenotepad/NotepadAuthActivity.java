package com.example.secura.modulesscreen.privacyprotectors.securenotepad;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.secura.R;

public class NotepadAuthActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SecureNotepadPrefs";
    private static final String KEY_PASSWORD = "notepad_password";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepad_auth);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Check if a password is already set
        if (!sharedPreferences.contains(KEY_PASSWORD) || sharedPreferences.getString(KEY_PASSWORD, "").isEmpty()) {
            // No password set, prompt to set one
            showSetPasswordDialog();
        } else {
            // Password already set, prompt for authentication
            showConfirmPasswordDialog();
        }
    }

    /**
     * Displays a custom dialog to set a new password for the Secure Notepad.
     */
    @SuppressLint("MissingInflatedId")
    private void showSetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_AlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_password, null);
        builder.setView(dialogView);
        builder.setCancelable(false); // Don't allow dismissing without setting password

        final EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        final EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        Button btnSetPassword = dialogView.findViewById(R.id.btnSetPassword);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();

        btnSetPassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showToast("Please enter both passwords.");
            } else if (!newPassword.equals(confirmPassword)) {
                showToast("Passwords do not match.");
            } else {
                // Save the new password
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_PASSWORD, newPassword); // Consider hashing the password in a real app
                editor.apply();
                showToast("Password set successfully!");
                dialog.dismiss();
                // After setting password, proceed to confirm it or directly open if first time
                openNotepad();
            }
        });
    }

    /**
     * Displays a custom dialog to confirm the existing password for the Secure Notepad.
     */
    @SuppressLint({"MissingInflatedId", "GestureBackNavigation"})
    private void showConfirmPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_AlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_confirm_password, null);
        builder.setView(dialogView);
        builder.setCancelable(false); // Don't allow dismissing without authentication

        final EditText etPassword = dialogView.findViewById(R.id.etPassword);
        Button btnAuthenticate = dialogView.findViewById(R.id.btnAuthenticate);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();

        btnAuthenticate.setOnClickListener(v -> {
            String enteredPassword = etPassword.getText().toString().trim();
            String storedPassword = sharedPreferences.getString(KEY_PASSWORD, "");

            if (enteredPassword.equals(storedPassword)) {
                showToast("Authentication successful!");
                dialog.dismiss();
                openNotepad();
            } else {
                showToast("Incorrect password. Try again.");
                etPassword.setText(""); // Clear password field
            }
        });

        // Handle back button press for this dialog
        dialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                // Prevent dialog dismissal on back press
                finish(); // Close this authentication activity
                return true;
            }
            return false;
        });
    }

    /**
     * Opens the SecureNotepadActivity.
     */
    private void openNotepad() {
        Intent intent = new Intent(NotepadAuthActivity.this, SecureNotepadActivity.class);
        startActivity(intent);
        finish(); // Finish this authentication activity once notepad is opened
    }

    /**
     * Displays a short Toast message.
     * @param message The message to display.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}