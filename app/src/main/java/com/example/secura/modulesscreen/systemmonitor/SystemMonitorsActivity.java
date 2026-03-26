package com.example.secura.modulesscreen.systemmonitor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.card.MaterialCardView;
import com.example.secura.R;
import com.example.secura.modulesscreen.systemmonitor.SecurityMonitorActivity;

public class SystemMonitorsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_monitors);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("System Monitors");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        MaterialCardView securityMonitorCard = findViewById(R.id.card_security_monitor);

        securityMonitorCard.setOnClickListener(v -> {
            Intent intent = new Intent(SystemMonitorsActivity.this, SecurityMonitorActivity.class);
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