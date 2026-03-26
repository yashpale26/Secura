package com.example.secura.modulesscreen.utilitiesandmanagement.appmanagement;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.secura.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class AppUsageStatsActivity extends AppCompatActivity {

    private TextView appNameTextView, packageNameTextView, totalUsageTimeTextView, lastTimeUsedTextView,
            dailyUsageHeader, weeklyUsageHeader;
    private ProgressBar progressBar;
    private String currentPackageName;
    private AppInfoHelper appInfoHelper;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_usage_stats);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("App Usage Statistics");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        appNameTextView = findViewById(R.id.usageAppName);
        packageNameTextView = findViewById(R.id.usagePackageName);
        totalUsageTimeTextView = findViewById(R.id.totalUsageTime);
        lastTimeUsedTextView = findViewById(R.id.lastTimeUsed);
        dailyUsageHeader = findViewById(R.id.dailyUsageHeader);
        weeklyUsageHeader = findViewById(R.id.weeklyUsageHeader);
        progressBar = findViewById(R.id.progressBar);

        appInfoHelper = new AppInfoHelper(this);
        executorService = Executors.newSingleThreadExecutor();

        currentPackageName = getIntent().getStringExtra("packageName");

        if (currentPackageName != null) {
            loadAppUsageStats(currentPackageName);
        } else {
            Toast.makeText(this, "No package name provided.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Loads app usage statistics for the specified package.
     *
     * @param packageName The package name of the app.
     */
    private void loadAppUsageStats(String packageName) {
        progressBar.setVisibility(View.VISIBLE);
        // Hide all content until loaded
        appNameTextView.setVisibility(View.GONE);
        packageNameTextView.setVisibility(View.GONE);
        totalUsageTimeTextView.setVisibility(View.GONE);
        lastTimeUsedTextView.setVisibility(View.GONE);
        dailyUsageHeader.setVisibility(View.GONE);
        weeklyUsageHeader.setVisibility(View.GONE);
        // Remove existing dynamic views for daily/weekly stats if any
        ((LinearLayout)findViewById(R.id.dailyStatsContainer)).removeAllViews();
        ((LinearLayout)findViewById(R.id.weeklyStatsContainer)).removeAllViews();

        executorService.execute(() -> {
            // Get app details for name and icon
            final AppInfoHelper.AppInfo appDetails = appInfoHelper.getAppDetails(packageName);

            if (!appInfoHelper.hasUsageStatsPermission()) {
                runOnUiThread(() -> {
                    Toast.makeText(AppUsageStatsActivity.this,
                            "Usage Stats permission is not granted. Cannot display usage data.",
                            Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    finish(); // Or show a message to direct user to settings
                });
                return;
            }

            // Get usage stats for daily and weekly intervals
            List<UsageStats> dailyStats = appInfoHelper.getAppUsageStats(packageName, UsageStatsManager.INTERVAL_DAILY);
            List<UsageStats> weeklyStats = appInfoHelper.getAppUsageStats(packageName, UsageStatsManager.INTERVAL_WEEKLY);

            long totalTimeInForeground = 0;
            long lastTimeUsed = 0;
            if (dailyStats != null && !dailyStats.isEmpty()) {
                for (UsageStats stats : dailyStats) {
                    totalTimeInForeground += stats.getTotalTimeInForeground();
                    if (stats.getLastTimeUsed() > lastTimeUsed) {
                        lastTimeUsed = stats.getLastTimeUsed();
                    }
                }
            }

            // Sort stats by last time used (most recent first)
            Collections.sort(dailyStats, (s1, s2) -> Long.compare(s2.getLastTimeUsed(), s1.getLastTimeUsed()));
            Collections.sort(weeklyStats, (s1, s2) -> Long.compare(s2.getLastTimeUsed(), s1.getLastTimeUsed()));

            final long finalTotalTimeInForeground = totalTimeInForeground;
            final long finalLastTimeUsed = lastTimeUsed;

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (appDetails != null) {
                    appNameTextView.setText(appDetails.appName);
                    packageNameTextView.setText(appDetails.packageName);
                    appNameTextView.setVisibility(View.VISIBLE);
                    packageNameTextView.setVisibility(View.VISIBLE);
                }

                totalUsageTimeTextView.setText("Total Usage (Last 7 Days): " + AppInfoHelper.formatDuration(finalTotalTimeInForeground));
                totalUsageTimeTextView.setVisibility(View.VISIBLE);

                if (finalLastTimeUsed > 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm", Locale.getDefault());
                    lastTimeUsedTextView.setText("Last Used: " + sdf.format(new Date(finalLastTimeUsed)));
                    lastTimeUsedTextView.setVisibility(View.VISIBLE);
                } else {
                    lastTimeUsedTextView.setText("Last Used: No recent usage recorded.");
                    lastTimeUsedTextView.setVisibility(View.VISIBLE);
                }

                // Display Daily Usage Stats
                dailyUsageHeader.setVisibility(View.VISIBLE);
                LinearLayout dailyContainer = findViewById(R.id.dailyStatsContainer);
                if (dailyStats != null && !dailyStats.isEmpty()) {
                    for (UsageStats stats : dailyStats) {
                        if (stats.getTotalTimeInForeground() > 0) {
                            TextView dailyEntry = new TextView(AppUsageStatsActivity.this);
                            String date = new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date(stats.getFirstTimeStamp()));
                            dailyEntry.setText(date + ": " + AppInfoHelper.formatDuration(stats.getTotalTimeInForeground()));
                            dailyEntry.setTextSize(14);
                            dailyEntry.setTextColor(getResources().getColor(R.color.black));
                            dailyEntry.setPadding(0, 4, 0, 4);
                            dailyContainer.addView(dailyEntry);
                        }
                    }
                    if (dailyContainer.getChildCount() == 0) {
                        TextView noData = new TextView(AppUsageStatsActivity.this);
                        noData.setText("No daily usage data found.");
                        noData.setTextSize(14);
                        noData.setTextColor(getResources().getColor(R.color.text_secondary));
                        dailyContainer.addView(noData);
                    }
                } else {
                    TextView noData = new TextView(AppUsageStatsActivity.this);
                    noData.setText("No daily usage data found.");
                    noData.setTextSize(14);
                    noData.setTextColor(getResources().getColor(R.color.text_secondary));
                    dailyContainer.addView(noData);
                }

                // Display Weekly Usage Stats
                weeklyUsageHeader.setVisibility(View.VISIBLE);
                LinearLayout weeklyContainer = findViewById(R.id.weeklyStatsContainer);
                if (weeklyStats != null && !weeklyStats.isEmpty()) {
                    for (UsageStats stats : weeklyStats) {
                        if (stats.getTotalTimeInForeground() > 0) {
                            TextView weeklyEntry = new TextView(AppUsageStatsActivity.this);
                            String weekStart = new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date(stats.getFirstTimeStamp()));
                            String weekEnd = new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date(stats.getLastTimeStamp()));
                            weeklyEntry.setText(weekStart + " - " + weekEnd + ": " + AppInfoHelper.formatDuration(stats.getTotalTimeInForeground()));
                            weeklyEntry.setTextSize(14);
                            weeklyEntry.setTextColor(getResources().getColor(R.color.black));
                            weeklyEntry.setPadding(0, 4, 0, 4);
                            weeklyContainer.addView(weeklyEntry);
                        }
                    }
                    if (weeklyContainer.getChildCount() == 0) {
                        TextView noData = new TextView(AppUsageStatsActivity.this);
                        noData.setText("No weekly usage data found.");
                        noData.setTextSize(14);
                        noData.setTextColor(getResources().getColor(R.color.text_secondary));
                        weeklyContainer.addView(noData);
                    }
                } else {
                    TextView noData = new TextView(AppUsageStatsActivity.this);
                    noData.setText("No weekly usage data found.");
                    noData.setTextSize(14);
                    noData.setTextColor(getResources().getColor(R.color.text_secondary));
                    weeklyContainer.addView(noData);
                }
            });
        });
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
