package com.example.secura.modulesscreen.systemmonitor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secura.R;
import com.example.secura.modulesscreen.systemmonitor.ScanResultAdapter;
import com.example.secura.modulesscreen.systemmonitor.ScanResult;
import com.example.secura.modulesscreen.systemmonitor.SecurityMonitorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SecurityMonitorActivity extends AppCompatActivity {

    private static final String TAG = "SecurityMonitorActivity";
    private static final int REQUEST_PERMISSION = 100;
    private static final String NOTIFICATION_LISTENER_PERMISSION = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE";
    private static final String USAGE_STATS_PERMISSION = "android.permission.PACKAGE_USAGE_STATS";

    private Switch permissionsSwitch;
    private Button refreshButton;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView statusTextView;
    private ScanResultAdapter adapter;
    private List<ScanResult> scanResults;

    private BroadcastReceiver downloadReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_monitor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Security Monitor");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize UI components
        permissionsSwitch = findViewById(R.id.permissionsSwitch);
        refreshButton = findViewById(R.id.refreshButton);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        statusTextView = findViewById(R.id.statusTextView);

        // Setup RecyclerView
        scanResults = new ArrayList<>();
        adapter = new ScanResultAdapter(this, scanResults);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Set up switch listener
        permissionsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Direct a user to the settings screen to enable the permissions
                    requestPermissions();
                } else {
                    stopMonitorService();
                }
            }
        });

        // Set up refresh button listener
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSecurityScan();
            }
        });

        // Initial check for permissions and service status
        checkPermissionsAndSetSwitch();
        // Register broadcast receiver for downloads
        downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                    Uri data = intent.getData();
                    if (data != null) {
                        String filePath = data.getPath();
                        if (filePath != null && filePath.toLowerCase().endsWith(".apk")) {
                            Log.d(TAG, "New APK downloaded or opened: " + filePath);
                            // Simulate detection of a potential threat
                            simulateMaliciousDetection("New App Downloaded", "New app is being installed. Scanning for potential threats...", null);
                        }
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_VIEW);
        filter.addDataScheme("content");
        filter.addDataScheme("file");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.registerReceiver(downloadReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            ContextCompat.registerReceiver(this, downloadReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissionsAndSetSwitch();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downloadReceiver != null) {
            unregisterReceiver(downloadReceiver);
        }
    }

    private void requestPermissions() {
        if (!isPermissionGranted(USAGE_STATS_PERMISSION)) {
            // Request Usage Stats permission
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        } else if (!isPermissionGranted(NOTIFICATION_LISTENER_PERMISSION)) {
            // Request Notification Listener permission
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        } else {
            // All permissions are granted, start the service
            startMonitorService();
        }
    }

    private void checkPermissionsAndSetSwitch() {
        boolean hasUsageStats = isPermissionGranted(USAGE_STATS_PERMISSION);
        boolean hasNotificationListener = isPermissionGranted(NOTIFICATION_LISTENER_PERMISSION);

        permissionsSwitch.setChecked(hasUsageStats && hasNotificationListener);

        if (permissionsSwitch.isChecked()) {
            startMonitorService();
        } else {
            stopMonitorService();
        }
    }

    private boolean isPermissionGranted(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (USAGE_STATS_PERMISSION.equals(permission)) {
                return Settings.ACTION_USAGE_ACCESS_SETTINGS.equals(Settings.ACTION_USAGE_ACCESS_SETTINGS) &&
                        !Settings.canDrawOverlays(this) &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                        isUsageStatsGranted();
            }
            if (NOTIFICATION_LISTENER_PERMISSION.equals(permission)) {
                return Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners").contains(getApplicationContext().getPackageName());
            }
        }
        return false;
    }

    private boolean isUsageStatsGranted() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return Settings.ACTION_USAGE_ACCESS_SETTINGS.equals(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void startMonitorService() {
        Intent serviceIntent = new Intent(this, SecurityMonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        Toast.makeText(this, "Security Monitor service started.", Toast.LENGTH_SHORT).show();
    }

    private void stopMonitorService() {
        Intent serviceIntent = new Intent(this, SecurityMonitorService.class);
        stopService(serviceIntent);
        Toast.makeText(this, "Security Monitor service stopped.", Toast.LENGTH_SHORT).show();
    }

    private void startSecurityScan() {
        new SecurityScanTask().execute();
    }

    private void simulateMaliciousDetection(String title, String reason, Drawable icon) {
        ScanResult result = new ScanResult(title, reason, icon);
        scanResults.add(result);
        adapter.notifyDataSetChanged();
        statusTextView.setText(scanResults.size() + " threats found. Please review.");
    }

    // Disclaimer: This is a simplified educational example. A real IDS/IPS requires
    // extensive research, root access, and a comprehensive threat database.
    private class SecurityScanTask extends AsyncTask<Void, ScanResult, Void> {

        private List<String> highRiskPermissions = new ArrayList<>();
        private List<String> simulatedMalware = new ArrayList<>();

        public SecurityScanTask() {
            // Populate a list of high-risk permissions
            highRiskPermissions.add("android.permission.READ_SMS");
            highRiskPermissions.add("android.permission.READ_CALL_LOG");
            highRiskPermissions.add("android.permission.READ_CONTACTS");
            highRiskPermissions.add("android.permission.CAMERA");
            highRiskPermissions.add("android.permission.ACCESS_FINE_LOCATION");

            // Simulate a list of known malicious apps (example only)
            simulatedMalware.add("com.android.malicious.example1");
            simulatedMalware.add("com.example.adware");
        }

        @Override
        protected void onPreExecute() {
            scanResults.clear();
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.VISIBLE);
            refreshButton.setEnabled(false);
            statusTextView.setText("Scanning device for threats...");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            PackageManager pm = getPackageManager();
            List<PackageInfo> installedApps = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);

            Random random = new Random();

            for (PackageInfo packageInfo : installedApps) {
                // Get app details
                String appName = packageInfo.applicationInfo.loadLabel(pm).toString();
                String packageName = packageInfo.packageName;
                Drawable appIcon = packageInfo.applicationInfo.loadIcon(pm);

                // Simulate malware detection
                if (simulatedMalware.contains(packageName)) {
                    publishProgress(new ScanResult(appName, "Detected as known malware.", appIcon));
                    continue;
                }

                // Simulate background permission abuse detection
                if (random.nextInt(100) < 5) { // 5% chance of a random app being flagged
                    publishProgress(new ScanResult(appName, "Detected suspicious background activity.", appIcon));
                }

                // Check for high-risk permissions
                if (packageInfo.requestedPermissions != null) {
                    for (String permission : packageInfo.requestedPermissions) {
                        if (highRiskPermissions.contains(permission)) {
                            publishProgress(new ScanResult(appName, "Using high-risk permission: " + permission, appIcon));
                            break;
                        }
                    }
                }

                try {
                    Thread.sleep(100); // Simulate scanning time
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(ScanResult... values) {
            scanResults.add(values[0]);
            adapter.notifyItemInserted(scanResults.size() - 1);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.GONE);
            refreshButton.setEnabled(true);
            if (scanResults.isEmpty()) {
                statusTextView.setText("No threats detected. Your device is safe.");
            } else {
                statusTextView.setText(scanResults.size() + " threats found. Please review.");
            }
        }
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
