package com.example.secura.splashloginregister;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.secura.R;

import java.util.Random;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etName, etUsername, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;
    private ImageButton ibRandomUsername;
    private DatabaseHelper databaseHelper;

    private final String[] CYBER_USERNAMES = {
            "CyberKnight", "NetGuardian", "DataSentinel", "SecureCoder", "AnonHacker",
            "EthicalByte", "InfoProtector", "KeyMaster", "PacketWhisperer", "RootDefender"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        etName = findViewById(R.id.et_name);
        etUsername = findViewById(R.id.et_username_reg);
        etPassword = findViewById(R.id.et_password_reg);
        etConfirmPassword = findViewById(R.id.et_confirm_password_reg);
        btnRegister = findViewById(R.id.btn_register);
        tvLoginLink = findViewById(R.id.tv_login_link);
        ibRandomUsername = findViewById(R.id.ib_random_username);

        databaseHelper = new DatabaseHelper(this);

        Animation slideInAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        etName.startAnimation(slideInAnimation);
        etUsername.startAnimation(slideInAnimation);
        etPassword.startAnimation(slideInAnimation);
        etConfirmPassword.startAnimation(slideInAnimation);
        btnRegister.startAnimation(slideInAnimation);

        // Load button click animation
        final Animation clickAnimation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation);

        ibRandomUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(clickAnimation); // Apply click animation to random username button too
                String randomUsername = generateRandomCyberUsername();
                etUsername.setText(randomUsername);
                Toast.makeText(RegistrationActivity.this, "Generated: " + randomUsername, Toast.LENGTH_SHORT).show();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(clickAnimation); // Apply click animation
                String name = etName.getText().toString().trim();
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                if (name.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(RegistrationActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegistrationActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    if (databaseHelper.checkUser(username, password)) {
                        Toast.makeText(RegistrationActivity.this, "Username already exists. Please choose a different one.", Toast.LENGTH_LONG).show();
                    } else if (databaseHelper.insertUser(name, username, password)) {
                        Toast.makeText(RegistrationActivity.this, "Registration Successful!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        finish();
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Registration Failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(RegistrationActivity.this, "Error during registration: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });

        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
    }

    private String generateRandomCyberUsername() {
        Random random = new Random();
        int index = random.nextInt(CYBER_USERNAMES.length);
        return CYBER_USERNAMES[index];
    }
}