package com.example.secura.splashloginregister;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.secura.R;
import com.example.secura.homescreen.HomeActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private CheckBox cbRememberMe;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper databaseHelper;

    // Key for storing the currently logged-in username
    public static final String KEY_LOGGED_IN_USERNAME = "loggedInUsername";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register_link);
        cbRememberMe = findViewById(R.id.cb_remember_me);

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(this);

        // Check if "remember me" was previously checked and pre-fill credentials
        if (sharedPreferences.getBoolean("rememberMe", false)) {
            etUsername.setText(sharedPreferences.getString("username", ""));
            etPassword.setText(sharedPreferences.getString("password", ""));
            cbRememberMe.setChecked(true);
        }

        // Apply animations to input fields and buttons
        Animation slideInAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        etUsername.startAnimation(slideInAnimation);
        etPassword.startAnimation(slideInAnimation);
        cbRememberMe.startAnimation(slideInAnimation);
        btnLogin.startAnimation(slideInAnimation);

        // Load button click animation for visual feedback
        final Animation clickAnimation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(clickAnimation); // Apply click animation
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // Validate input fields
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check user credentials against the database
                if (databaseHelper.checkUser(username, password)) {
                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (cbRememberMe.isChecked()) {
                        editor.putBoolean("rememberMe", true);
                        editor.putString("username", username);
                        editor.putString("password", password); // Storing password for "remember me"
                    } else {
                        editor.clear(); // Clear all stored login preferences if "remember me" is not checked
                    }
                    // IMPORTANT: Store the username of the currently logged-in user
                    editor.putString(KEY_LOGGED_IN_USERNAME, username);
                    editor.apply(); // Apply changes to SharedPreferences

                    // Navigate to HomeActivity
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    finish(); // Finish LoginActivity so user can't go back to it
                } else {
                    // Show error message for invalid credentials
                    Toast.makeText(LoginActivity.this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to RegistrationActivity
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
    }
}