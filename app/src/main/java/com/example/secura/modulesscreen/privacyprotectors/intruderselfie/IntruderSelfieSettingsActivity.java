package com.example.secura.modulesscreen.privacyprotectors.intruderselfie;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.secura.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class IntruderSelfieSettingsActivity extends AppCompatActivity {

    private static final int REQ_ENABLE_ADMIN = 1;
    private static final int REQ_MANAGE_ALL_FILES = 2;
    private static final int REQ_CAMERA = 3;
    private static final int REQ_POST_NOTIF = 4;

    private DevicePolicyManager dpm;
    private ComponentName admin;
    private SwitchMaterial enableSwitch;
    private TextView statusText;
    private Button openGalleryBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intruder_selfie_settings);

        Toolbar toolbar = findViewById(R.id.toolbarIntruderSelfieSettings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        admin = new ComponentName(this, IntruderSelfieAdminReceiver.class);

        enableSwitch = findViewById(R.id.enableIntruderSelfieSwitch);
        statusText = findViewById(R.id.intruderSelfieStatusText);
        openGalleryBtn = findViewById(R.id.viewIntruderSelfiePhotosButton);

        updateUI();

        enableSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                if (!hasAllPerms()) {
                    requestAllPerms();
                    enableSwitch.setChecked(false);
                } else {
                    requestDeviceAdmin();
                }
            } else {
                if (dpm.isAdminActive(admin)) dpm.removeActiveAdmin(admin);
                updateUI();
            }
        });

        openGalleryBtn.setOnClickListener(v ->
                startActivity(new Intent(this, IntruderSelfieGalleryActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void requestDeviceAdmin() {
        Intent i = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
        i.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Enable this to allow capturing intruder selfies.");
        startActivityForResult(i, REQ_ENABLE_ADMIN);
    }

    private boolean hasAllPerms() {
        boolean cam = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean storageOk = Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager();
        boolean notifOk = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        return cam && storageOk && notifOk;
    }

    private void requestAllPerms() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_POST_NOTIF);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Intent i = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            i.setData(Uri.fromParts("package", getPackageName(), null));
            startActivityForResult(i, REQ_MANAGE_ALL_FILES);
        }
    }

    private void updateUI() {
        boolean adminActive = dpm.isAdminActive(admin);
        boolean permsOk = hasAllPerms();
        enableSwitch.setChecked(adminActive);
        statusText.setText(
                (adminActive ? "Device Administrator: Enabled\n" : "Device Administrator: Disabled\n") +
                        (permsOk ? "Permissions: OK\n" : "Permissions missing (Camera/Storage/Notifications)\n") +
                        "Intruder Selfie: " + ((adminActive && permsOk) ? "Ready" : "Not Ready")
        );
        openGalleryBtn.setEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] perms, @NonNull int[] res) {
        super.onRequestPermissionsResult(requestCode, perms, res);
        updateUI();
        if (requestCode == REQ_CAMERA) {
            if (res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED) {
                if (hasAllPerms()) requestDeviceAdmin();
            } else {
                Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updateUI();
        if (requestCode == REQ_ENABLE_ADMIN) {
            Toast.makeText(this,
                    resultCode == RESULT_OK ? "Device Administrator enabled." : "Device Administrator not enabled.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
