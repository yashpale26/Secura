package com.example.secura.modulesscreen.privacyprotectors.applock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.secura.R;

public class AppLockActivity extends AppCompatActivity {

    private String packageToUnlock;
    private EditText pinInput;
    private TextView lockedAppText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock);

        pinInput = findViewById(R.id.pinInput);
        Button unlockBtn = findViewById(R.id.unlockBtn);
        GridLayout numpad = findViewById(R.id.numpad);
//        ImageButton backspaceButton = findViewById(R.id.backspaceButton);
        ImageButton clearButton = findViewById(R.id.clearButton);
        lockedAppText = findViewById(R.id.lockedAppText);

        packageToUnlock = getIntent().getStringExtra("locked_package");
        if (packageToUnlock != null) {
            String appName = null;
            try {
                appName = getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(packageToUnlock, 0)).toString();
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
            lockedAppText.setText("Unlock " + appName);
        }

        // Handle numpad button clicks
        for (int i = 0; i < numpad.getChildCount(); i++) {
            View child = numpad.getChildAt(i);
            if (child instanceof Button) {
                child.setOnClickListener(v -> {
                    Button button = (Button) v;
                    String number = button.getText().toString();
                    if (pinInput.getText().length() < 4) {
                        pinInput.append(number);
                    }
                });
            }
        }

//        backspaceButton.setOnClickListener(v -> {
//            String currentPin = pinInput.getText().toString();
//            if (!TextUtils.isEmpty(currentPin)) {
//                pinInput.setText(currentPin.substring(0, currentPin.length() - 1));
//            }
//        });

        clearButton.setOnClickListener(v -> pinInput.setText(""));

        unlockBtn.setOnClickListener(v -> {
            String enteredPin = pinInput.getText().toString().trim();
            SharedPreferences prefs = getSharedPreferences("app_lock_prefs", MODE_PRIVATE);
            String correctPin = prefs.getString("custom_pin", "1234"); // Default to 1234 if not set

            if (enteredPin.equals(correctPin)) {
                if (!TextUtils.isEmpty(packageToUnlock)) {
                    prefs.edit().putString("temp_unlocked_app", packageToUnlock).apply();
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageToUnlock);
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                    }
                }
                Toast.makeText(this, "App Unlocked", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                pinInput.setText("");
            }
        });
    }

    @SuppressLint({"GestureBackNavigation", "MissingSuperCall"})
    @Override
    public void onBackPressed() {
        // Prevent back navigation to bypass lock screen
    }
}