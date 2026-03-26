package com.example.secura.modulesscreen.privacyprotectors.applock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.CompoundButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.secura.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppLockSettingsActivity extends AppCompatActivity {

    private ListView listView;
    private AppListAdapter adapter;
    private List<ApplicationInfo> installedApps;
    private Set<String> lockedAppsSet;
    private SharedPreferences prefs;
    private Button saveBtn, unlockBtn;
    private EditText pinInputSetup;
    private Button setPinButton;
    private SwitchMaterial permissionSwitch; // Replaced CheckBox with Switch

    @SuppressLint({"CutPasteId", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock_settings);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        prefs = getSharedPreferences("app_lock_prefs", Context.MODE_PRIVATE);
        lockedAppsSet = prefs.getStringSet("locked_apps", new HashSet<>());

        listView = findViewById(R.id.appsListView);
        pinInputSetup = findViewById(R.id.pinInputSetup);
        setPinButton = findViewById(R.id.setPinButton);
        permissionSwitch = findViewById(R.id.permissionSwitch); // Changed ID to permissionSwitch
        saveBtn = findViewById(R.id.lockButton); // Changed ID to lockButton
        unlockBtn = findViewById(R.id.unlockButton);

        PackageManager pm = getPackageManager();
        installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        adapter = new AppListAdapter(this, installedApps, lockedAppsSet);
        listView.setAdapter(adapter);

        // Set PIN functionality
        setPinButton.setOnClickListener(v -> {
            String newPin = pinInputSetup.getText().toString().trim();
            if (TextUtils.isEmpty(newPin) || newPin.length() < 4) {
                Toast.makeText(this, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show();
            } else {
                prefs.edit().putString("custom_pin", newPin).apply();
                Toast.makeText(this, "New PIN saved!", Toast.LENGTH_SHORT).show();
            }
        });

        // Permission Switch functionality
        permissionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestUsageStatsPermission();
                requestAccessibilityPermission();
            }
        });

        // Lock button functionality
        saveBtn.setOnClickListener(v -> {
            Set<String> selectedApps = adapter.getSelectedApps();
            prefs.edit().putStringSet("locked_apps", selectedApps).apply();
            Toast.makeText(this, "Selected apps locked", Toast.LENGTH_SHORT).show();
            // Reset the adapter's selected apps to match the new locked state
            lockedAppsSet = prefs.getStringSet("locked_apps", new HashSet<>());
            adapter.setSelectedApps(lockedAppsSet);
            adapter.notifyDataSetChanged();
        });

        // Unlock button functionality
        unlockBtn.setOnClickListener(v -> {
            Set<String> selectedApps = adapter.getSelectedApps();
            Set<String> currentLockedApps = prefs.getStringSet("locked_apps", new HashSet<>());
            currentLockedApps.removeAll(selectedApps);
            prefs.edit().putStringSet("locked_apps", currentLockedApps).apply();
            // Reset the adapter's selected apps to match the new locked state
            lockedAppsSet = prefs.getStringSet("locked_apps", new HashSet<>());
            adapter.setSelectedApps(lockedAppsSet);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Selected apps unlocked", Toast.LENGTH_SHORT).show();
        });
    }

    // Handle back button click on the Toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestUsageStatsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!hasUsageStatsPermission()) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                Toast.makeText(this, "Please enable Usage Access for this app.", Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
        }
    }

    private boolean hasUsageStatsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                PackageManager packageManager = getPackageManager();
                android.app.AppOpsManager appOpsManager = (android.app.AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
                int mode = appOpsManager.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
                return mode == android.app.AppOpsManager.MODE_ALLOWED;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }
        return false;
    }

    private void requestAccessibilityPermission() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        Toast.makeText(this, "Please enable Accessibility Service for App Lock", Toast.LENGTH_LONG).show();
        startActivity(intent);
    }
}
