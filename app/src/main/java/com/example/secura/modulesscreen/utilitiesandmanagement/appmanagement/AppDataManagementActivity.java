package com.example.secura.modulesscreen.utilitiesandmanagement.appmanagement;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.secura.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class AppDataManagementActivity extends AppCompatActivity {

    private TextView appNameTextView, packageNameTextView, currentSizesTextView;
    private Button clearCacheButton, clearDataButton;
    private ProgressBar progressBar;

    private String currentPackageName;
    private AppInfoHelper appInfoHelper;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_data_management);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("App Data Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        appNameTextView = findViewById(R.id.dataMgmtAppName);
        packageNameTextView = findViewById(R.id.dataMgmtPackageName);
        currentSizesTextView = findViewById(R.id.currentSizesTextView);
        clearCacheButton = findViewById(R.id.clearCacheButton);
        clearDataButton = findViewById(R.id.clearDataButton);
        progressBar = findViewById(R.id.progressBar);

        appInfoHelper = new AppInfoHelper(this);
        executorService = Executors.newSingleThreadExecutor();

        currentPackageName = getIntent().getStringExtra("packageName");

        if (currentPackageName != null) {
            loadAppInfoForDataManagement(currentPackageName);
        } else {
            Toast.makeText(this, "No package name provided.", Toast.LENGTH_SHORT).show();
            finish();
        }

        clearCacheButton.setOnClickListener(v -> confirmClear("cache", currentPackageName));
        clearDataButton.setOnClickListener(v -> confirmClear("data", currentPackageName));
    }

    /**
     * Loads basic app information for display in data management view.
     * Note: Actual size fetching is complex for other apps.
     *
     * @param packageName The package name of the app.
     */
    private void loadAppInfoForDataManagement(String packageName) {
        progressBar.setVisibility(View.VISIBLE);
        appNameTextView.setVisibility(View.GONE);
        packageNameTextView.setVisibility(View.GONE);
        currentSizesTextView.setVisibility(View.GONE);
        clearCacheButton.setVisibility(View.GONE);
        clearDataButton.setVisibility(View.GONE);

        executorService.execute(() -> {
            final AppInfoHelper.AppInfo appDetails = appInfoHelper.getAppDetails(packageName);

            runOnUiThread(() -> {
                if (appDetails != null) {
                    appNameTextView.setText(appDetails.appName);
                    packageNameTextView.setText(appDetails.packageName);
                    // Displaying placeholder for sizes, as direct access is restricted.
                    currentSizesTextView.setText("Current Sizes: (View in system settings for accurate values)");

                    appNameTextView.setVisibility(View.VISIBLE);
                    packageNameTextView.setVisibility(View.VISIBLE);
                    currentSizesTextView.setVisibility(View.VISIBLE);
                    clearCacheButton.setVisibility(View.VISIBLE);
                    clearDataButton.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(AppDataManagementActivity.this, "Could not load app info.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                progressBar.setVisibility(View.GONE);
            });
        });
    }

    /**
     * Shows a confirmation dialog before redirecting to system settings for clearing data/cache.
     *
     * @param type        "cache" or "data"
     * @param packageName The package name of the app.
     */
    private void confirmClear(String type, String packageName) {
        String title = "Clear " + type.substring(0, 1).toUpperCase() + type.substring(1) + "?";
        String message = "This will take you to the system's App Info page. You can clear the " + type + " for " + appNameTextView.getText() + " from there.";

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Go to Settings", (dialog, which) -> openSystemAppSettings(packageName))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Opens the system app settings page for the given package.
     *
     * @param packageName The package name of the app.
     */
    private void openSystemAppSettings(String packageName) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", packageName, null);
            intent.setData(uri);
            startActivity(intent);
            Toast.makeText(this, "Redirecting to system app settings.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not open app settings.", Toast.LENGTH_SHORT).show();
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
