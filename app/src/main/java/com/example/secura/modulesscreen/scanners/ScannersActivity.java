package com.example.secura.modulesscreen.scanners;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.secura.modulesscreen.scanners.callandsmsscan.CallSmsScannerActivity;
import com.example.secura.modulesscreen.scanners.networkscan.NetworkScannerActivity;
import com.google.android.material.card.MaterialCardView;
import com.example.secura.R;
import com.example.secura.modulesscreen.scanners.malwarescan.MalwareScannerActivity;

public class ScannersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanners);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Scanners");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        MaterialCardView malwareScannerCard = findViewById(R.id.card_malware_scanner);
        MaterialCardView networkScannerCard = findViewById(R.id.card_network_scanner);
        MaterialCardView callSmsScannerCard = findViewById(R.id.card_call_sms_scanner);

        malwareScannerCard.setOnClickListener(v -> {
            Intent intent = new Intent(ScannersActivity.this, MalwareScannerActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        networkScannerCard.setOnClickListener(v -> {
            Intent intent = new Intent(ScannersActivity.this, NetworkScannerActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        callSmsScannerCard.setOnClickListener(v -> {
            Intent intent = new Intent(ScannersActivity.this, CallSmsScannerActivity.class);
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
