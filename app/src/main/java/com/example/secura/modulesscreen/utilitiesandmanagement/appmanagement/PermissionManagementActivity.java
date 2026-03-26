package com.example.secura.modulesscreen.utilitiesandmanagement.appmanagement;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secura.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class PermissionManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PermissionAdapter adapter;
    private AppInfoHelper appInfoHelper;
    private ProgressBar progressBar;
    private TextView emptyTextView, permissionInfoTextView;
    private Button openAppSettingsButton;

    private String currentPackageName;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_management);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("App Permissions");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.permissionsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);
        permissionInfoTextView = findViewById(R.id.permissionInfoTextView);
        openAppSettingsButton = findViewById(R.id.openAppSettingsButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        appInfoHelper = new AppInfoHelper(this);
        executorService = Executors.newSingleThreadExecutor();

        currentPackageName = getIntent().getStringExtra("packageName");

        if (currentPackageName != null) {
            loadAppPermissions(currentPackageName);
            openAppSettingsButton.setOnClickListener(v -> openSystemAppSettings(currentPackageName));
        } else {
            Toast.makeText(this, "No package name provided.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Loads the permissions for the specified application in a background thread.
     *
     * @param packageName The package name of the app to load permissions for.
     */
    private void loadAppPermissions(String packageName) {
        progressBar.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        permissionInfoTextView.setVisibility(View.GONE);
        openAppSettingsButton.setVisibility(View.GONE);

        executorService.execute(() -> {
            AppInfoHelper.AppInfo appDetails = appInfoHelper.getAppDetails(packageName);
            final List<AppInfoHelper.PermissionDetail> permissions = (appDetails != null) ?
                    appDetails.permissions : new ArrayList<>();

            // Sort permissions alphabetically by name
            Collections.sort(permissions, Comparator.comparing(p -> p.name.toLowerCase()));

            runOnUiThread(() -> {
                if (appDetails != null) {
                    if (!appDetails.permissions.isEmpty()) {
                        adapter = new PermissionAdapter(PermissionManagementActivity.this, permissions);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setVisibility(View.VISIBLE);
                        permissionInfoTextView.setVisibility(View.VISIBLE);
                        openAppSettingsButton.setVisibility(View.VISIBLE);
                    } else {
                        emptyTextView.setText("No permissions declared for this app.");
                        emptyTextView.setVisibility(View.VISIBLE);
                    }
                } else {
                    emptyTextView.setText("Could not load app permissions.");
                    emptyTextView.setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.GONE);
            });
        });
    }

    /**
     * Opens the system app settings page for the given package to manage permissions.
     *
     * @param packageName The package name of the app.
     */
    private void openSystemAppSettings(String packageName) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", packageName, null);
            intent.setData(uri);
            startActivity(intent);
            Toast.makeText(this, "Redirecting to system app settings to manage permissions.", Toast.LENGTH_LONG).show();
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
