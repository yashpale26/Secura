package com.example.secura.modulesscreen.utilitiesandmanagement;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.secura.modulesscreen.utilitiesandmanagement.batterymonitor.BatteryMonitorActivity;
import com.google.android.material.card.MaterialCardView;
import com.example.secura.R;
import com.example.secura.modulesscreen.utilitiesandmanagement.appmanagement.AppManagementActivity;

public class UtilitiesAndManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utilities_and_management);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Utilities & Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        MaterialCardView appManagementCard = findViewById(R.id.card_app_management);
        MaterialCardView batteryMonitorCard = findViewById(R.id.card_battery_monitor);

        appManagementCard.setOnClickListener(v -> {
            Intent intent = new Intent(UtilitiesAndManagementActivity.this, AppManagementActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        batteryMonitorCard.setOnClickListener(v -> {
            Intent intent = new Intent(UtilitiesAndManagementActivity.this, BatteryMonitorActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}