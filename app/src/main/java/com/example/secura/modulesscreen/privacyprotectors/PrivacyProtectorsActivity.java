package com.example.secura.modulesscreen.privacyprotectors;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.secura.modulesscreen.privacyprotectors.applock.AppLockSettingsActivity;
import com.example.secura.modulesscreen.privacyprotectors.galleryvault.VaultActivity;
import com.example.secura.modulesscreen.privacyprotectors.intruderselfie.IntruderSelfieSettingsActivity;
import com.example.secura.modulesscreen.privacyprotectors.securenotepad.NotepadAuthActivity; // Import the NotepadAuthActivity
import com.google.android.material.card.MaterialCardView;
import com.example.secura.R;

public class PrivacyProtectorsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_protectors);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Privacy Protectors");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        MaterialCardView appLockCard = findViewById(R.id.card_app_lock);
        MaterialCardView galleryVaultCard = findViewById(R.id.card_gallery_vault);
        MaterialCardView intruderSelfieCard = findViewById(R.id.card_intruder_selfie);
        MaterialCardView secureNotepadCard = findViewById(R.id.card_secure_notepad);

        appLockCard.setOnClickListener(v -> {
            Intent intent = new Intent(PrivacyProtectorsActivity.this, AppLockSettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        galleryVaultCard.setOnClickListener(v -> {
            Intent intent = new Intent(PrivacyProtectorsActivity.this, VaultActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        intruderSelfieCard.setOnClickListener(v -> {
            Intent intent = new Intent(PrivacyProtectorsActivity.this, IntruderSelfieSettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Change this to start NotepadAuthActivity first
        secureNotepadCard.setOnClickListener(v -> {
            Intent intent = new Intent(PrivacyProtectorsActivity.this, NotepadAuthActivity.class); // Corrected line
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