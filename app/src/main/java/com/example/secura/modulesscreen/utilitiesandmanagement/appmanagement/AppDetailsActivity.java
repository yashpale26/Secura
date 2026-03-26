package com.example.secura.modulesscreen.utilitiesandmanagement.appmanagement;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.secura.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class AppDetailsActivity extends AppCompatActivity {

    private ImageView appIcon;
    private TextView appName, packageName, versionName, versionCode, installTime, updateTime,
            appSize, dataSize, cacheSize, codeSize, isSystemApp;
    private Button openAppButton, uninstallButton, forceStopButton, clearCacheButton, clearDataButton,
            managePermissionsButton, viewUsageStatsButton;
    private ProgressBar progressBar;
    private LinearLayout contentLayout;

    private String currentPackageName;
    private AppInfoHelper appInfoHelper;
    private ExecutorService executorService;

    // Request codes for starting activities for result
    private static final int REQUEST_UNINSTALL = 1001;
    private static final int REQUEST_APP_SETTINGS = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("App Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        appIcon = findViewById(R.id.appIcon);
        appName = findViewById(R.id.appName);
        packageName = findViewById(R.id.packageName);
        versionName = findViewById(R.id.versionName);
        versionCode = findViewById(R.id.versionCode);
        installTime = findViewById(R.id.installTime);
        updateTime = findViewById(R.id.updateTime);
        appSize = findViewById(R.id.appSize);
        dataSize = findViewById(R.id.dataSize);
        cacheSize = findViewById(R.id.cacheSize);
        codeSize = findViewById(R.id.codeSize);
        isSystemApp = findViewById(R.id.isSystemApp);

        openAppButton = findViewById(R.id.openAppButton);
        uninstallButton = findViewById(R.id.uninstallButton);
        forceStopButton = findViewById(R.id.forceStopButton);
        clearCacheButton = findViewById(R.id.clearCacheButton);
        clearDataButton = findViewById(R.id.clearDataButton);
        managePermissionsButton = findViewById(R.id.managePermissionsButton);
        viewUsageStatsButton = findViewById(R.id.viewUsageStatsButton);

        progressBar = findViewById(R.id.progressBar);
        contentLayout = findViewById(R.id.contentLayout);

        appInfoHelper = new AppInfoHelper(this);
        executorService = Executors.newSingleThreadExecutor();

        currentPackageName = getIntent().getStringExtra("packageName");

        if (currentPackageName != null) {
            loadAppDetails(currentPackageName);
        } else {
            Toast.makeText(this, "No package name provided.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set up click listeners for action buttons
        openAppButton.setOnClickListener(v -> openApp(currentPackageName));
        uninstallButton.setOnClickListener(v -> uninstallApp(currentPackageName));
        forceStopButton.setOnClickListener(v -> forceStopApp(currentPackageName));
        clearCacheButton.setOnClickListener(v -> clearAppData("cache", currentPackageName));
        clearDataButton.setOnClickListener(v -> clearAppData("data", currentPackageName));
        managePermissionsButton.setOnClickListener(v -> manageAppPermissions(currentPackageName));
        viewUsageStatsButton.setOnClickListener(v -> viewAppUsageStats(currentPackageName));
    }

    /**
     * Loads detailed information about the app in a background thread.
     *
     * @param packageName The package name of the app to load details for.
     */
    private void loadAppDetails(String packageName) {
        progressBar.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);

        executorService.execute(() -> {
            final AppInfoHelper.AppInfo appDetails = appInfoHelper.getAppDetails(packageName);

            runOnUiThread(() -> {
                if (appDetails != null) {
                    populateAppDetails(appDetails);
                    contentLayout.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(AppDetailsActivity.this, "Could not load app details.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                progressBar.setVisibility(View.GONE);
            });
        });
    }

    /**
     * Populates the UI with the retrieved app details.
     *
     * @param appDetails The AppInfo object containing the details.
     */
    private void populateAppDetails(AppInfoHelper.AppInfo appDetails) {
        appIcon.setImageDrawable(appDetails.icon);
        appName.setText(appDetails.appName);
        packageName.setText(appDetails.packageName);
        versionName.setText("Version: " + appDetails.versionName);
        versionCode.setText("Version Code: " + appDetails.versionCode);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm", Locale.getDefault());
        installTime.setText("Installed: " + sdf.format(new Date(appDetails.installTime)));
        updateTime.setText("Last Updated: " + sdf.format(new Date(appDetails.lastUpdateTime)));

        // Display sizes (note: data/cache might be 0 as direct access is restricted for other apps)
        codeSize.setText("Code Size: " + AppInfoHelper.formatFileSize(appDetails.codeSize, true));
        // You would need to get actual data/cache sizes from system or with privileged access
        dataSize.setText("Data Size: Not directly accessible*");
        cacheSize.setText("Cache Size: Not directly accessible*");
        appSize.setText("Total Size: Not directly accessible*"); // This would sum code+data+cache

        isSystemApp.setText(appDetails.isSystemApp ? "System App: Yes" : "System App: No");

        // Enable/Disable buttons based on context
        if (appDetails.packageName.equals(getPackageName())) { // Cannot uninstall self
            uninstallButton.setEnabled(false);
        } else {
            uninstallButton.setEnabled(true);
        }
        // Force stop, clear cache/data also typically require user interaction via system settings
        // or specific device owner permissions. For this demo, we'll link to system settings.
        forceStopButton.setEnabled(true); // Can always try to open settings to force stop
        clearCacheButton.setEnabled(true); // Can always try to open settings to clear cache
        clearDataButton.setEnabled(true); // Can always try to open settings to clear data
    }


    /**
     * Opens the selected application.
     *
     * @param packageName The package name of the app to open.
     */
    private void openApp(String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Cannot open this app.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Initiates the uninstallation process for the selected application.
     *
     * @param packageName The package name of the app to uninstall.
     */
    private void uninstallApp(String packageName) {
        if (packageName.equals(getPackageName())) {
            Toast.makeText(this, "Cannot uninstall this application from itself.", Toast.LENGTH_LONG).show();
            return;
        }

        // Show a confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Uninstall App")
                .setMessage("Are you sure you want to uninstall " + appName.getText() + "?")
                .setPositiveButton("Uninstall", (dialog, which) -> {
                    try {
                        Uri packageUri = Uri.parse("package:" + packageName);
                        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
                        startActivityForResult(uninstallIntent, REQUEST_UNINSTALL);
                    } catch (Exception e) {
                        Toast.makeText(AppDetailsActivity.this, "Error preparing uninstall: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Attempts to force stop the application by opening its system app info page.
     * Direct programmatic force stopping of other apps is not allowed for security reasons.
     *
     * @param packageName The package name of the app to force stop.
     */
    private void forceStopApp(String packageName) {
        showSystemAppSettings(packageName, "Force Stop");
    }

    /**
     * Attempts to clear the cache or data of the application by opening its system app info page.
     * Direct programmatic clearing of cache/data for other apps is not allowed.
     *
     * @param action      "cache" or "data"
     * @param packageName The package name of the app.
     */
    private void clearAppData(String action, String packageName) {
        showSystemAppSettings(packageName, "Clear " + action);
    }

    /**
     * Navigates to the system app settings page for permission management.
     *
     * @param packageName The package name of the app.
     */
    private void manageAppPermissions(String packageName) {
        // Direct to a custom activity first, or directly to system settings
        Intent intent = new Intent(this, PermissionManagementActivity.class);
        intent.putExtra("packageName", packageName);
        startActivity(intent);
    }

    /**
     * Navigates to the system app settings page or a custom activity for usage stats.
     *
     * @param packageName The package name of the app.
     */
    private void viewAppUsageStats(String packageName) {
        // Check if PACKAGE_USAGE_STATS permission is granted
        if (!appInfoHelper.hasUsageStatsPermission()) {
            // Inform user and request permission
            new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("To view app usage statistics, you need to grant 'Usage Access' permission for this app. Would you like to go to settings?")
                    .setPositiveButton("Go to Settings", (dialog, which) -> appInfoHelper.requestUsageStatsPermission())
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            Intent intent = new Intent(this, AppUsageStatsActivity.class);
            intent.putExtra("packageName", packageName);
            startActivity(intent);
        }
    }

    /**
     * Helper method to open the system app info page for the given package.
     *
     * @param packageName The package name of the app.
     * @param actionHint  A string indicating what action the user might want to perform there.
     */
    private void showSystemAppSettings(String packageName, String actionHint) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", packageName, null);
            intent.setData(uri);
            startActivityForResult(intent, REQUEST_APP_SETTINGS);
            Toast.makeText(this, "Navigate to system settings to " + actionHint + ".", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not open app settings.", Toast.LENGTH_SHORT).show();
            Log.e("AppDetailsActivity", "Error opening app settings: " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_UNINSTALL) {
            // After uninstall, the app details might not be available, so finish this activity
            // Or refresh the list in InstalledAppsListActivity
            Toast.makeText(this, "Uninstall process completed. You may need to refresh the app list.", Toast.LENGTH_SHORT).show();
            finish();
        } else if (requestCode == REQUEST_APP_SETTINGS) {
            // When returning from app settings, reload details to reflect any changes (e.g., sizes)
            if (currentPackageName != null) {
                loadAppDetails(currentPackageName);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }
}
