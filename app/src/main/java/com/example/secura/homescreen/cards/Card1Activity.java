package com.example.secura.homescreen.cards;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.secura.R;
import com.example.secura.homescreen.HomeActivity;
import com.example.secura.homescreen.HomeFragment;
import com.example.secura.homescreen.HomeFragment;
import com.example.secura.modulesscreen.scanners.malwarescan.MalwareScannerActivity;

public class Card1Activity extends AppCompatActivity {
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card1);

        Button startScanButton = findViewById(R.id.btn_start_scan);
        startScanButton.setOnClickListener(v -> {
            // TODO: Replace PlaceholderActivity.class with the actual class for the next activity.
            Intent intent = new Intent(Card1Activity.this, MalwareScannerActivity.class);
            startActivity(intent);
        });

        // Find the back button and set its click listener
        Button backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the home screen
                Intent intent = new Intent(Card1Activity.this, HomeActivity.class);
                startActivity(intent);
                finish(); // Optional: Close the current activity
            }
        });
    }
}