package com.example.secura.modulesscreen.utilitiesandmanagement.appmanagement;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.util.Log;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AppInfoHelper {

    private static final String TAG = "AppInfoHelper";
    private final Context context;
    private final PackageManager packageManager;

    public AppInfoHelper(Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();
    }

    /**
     * Represents detailed information about an installed application.
     */
    public static class AppInfo {
        public String appName;
        public String packageName;
        public Drawable icon;
        public String versionName;
        public int versionCode;
        public long installTime;
        public long lastUpdateTime;
        public long appSize; // Total size (code + data + cache)
        public long dataSize;
        public long cacheSize;
        public long codeSize;
        public boolean isSystemApp;
        public List<PermissionDetail> permissions; // List of permissions

        public AppInfo(String appName, String packageName, Drawable icon) {
            this.appName = appName;
            this.packageName = packageName;
            this.icon = icon;
            this.permissions = new ArrayList<>();
        }
    }

    /**
     * Represents a single permission detail.
     */
    public static class PermissionDetail {
        public String name;
        public String group;
        public String description;
        public boolean granted;
        public int protectionLevel; // e.g., dangerous, normal, signature

        public PermissionDetail(String name, String group, String description, boolean granted, int protectionLevel) {
            this.name = name;
            this.group = group;
            this.description = description;
            this.granted = granted;
            this.protectionLevel = protectionLevel;
        }

        public String getProtectionLevelString() {
            if (protectionLevel == PermissionInfo.PROTECTION_DANGEROUS) {
                return "Dangerous";
            } else if (protectionLevel == PermissionInfo.PROTECTION_NORMAL) {
                return "Normal";
            } else if (protectionLevel == PermissionInfo.PROTECTION_SIGNATURE) {
                return "Signature";
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && protectionLevel == PermissionInfo.PROTECTION_FLAG_PRIVILEGED) {
                return "Signature Privileged";
            } else {
                return "Other";
            }
        }
    }


    /**
     * Retrieves a list of all installed applications with basic information.
     *
     * @param includeSystemApps true to include system applications, false otherwise.
     * @return A list of AppInfo objects.
     */
    public List<AppInfo> getInstalledApps(boolean includeSystemApps) {
        List<AppInfo> appList = new ArrayList<>();
        List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA); // GET_META_DATA for basic info

        for (PackageInfo packageInfo : packages) {
            // Check if it's a system app
            boolean isSystemApp = (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

            if (!isSystemApp || includeSystemApps) {
                try {
                    String appName = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();
                    String packageName = packageInfo.packageName;
                    Drawable icon = packageManager.getApplicationIcon(packageInfo.applicationInfo);

                    AppInfo appInfo = new AppInfo(appName, packageName, icon);
                    appInfo.versionName = packageInfo.versionName;
                    appInfo.versionCode = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ?
                            (int) packageInfo.getLongVersionCode() : packageInfo.versionCode;
                    appInfo.installTime = packageInfo.firstInstallTime;
                    appInfo.lastUpdateTime = packageInfo.lastUpdateTime;
                    appInfo.isSystemApp = isSystemApp;

                    // Note: Getting precise size info (code, data, cache) for all apps without root or
                    // PACKAGE_USAGE_STATS permission (for specific scenarios) is complex and often restricted
                    // due to security and privacy. For demonstration, we'll try to get what's publicly available.
                    // For more accurate sizes, one would typically need to request `PackageStats`
                    // via `getPackageSizeInfo`, which is a system API and requires `android.permission.GET_PACKAGE_SIZE`
                    // (deprecated and not grantable to third-party apps) or `PACKAGE_USAGE_STATS` (for usage, not raw sizes).
                    // As a workaround, we can get an approximate size from the APK file.
                    File apkFile = new File(packageInfo.applicationInfo.sourceDir);
                    if (apkFile.exists()) {
                        appInfo.codeSize = apkFile.length(); // This is the APK size
                        appInfo.appSize = appInfo.codeSize; // Initialize with code size
                    }

                    appList.add(appInfo);
                } catch (Exception e) {
                    Log.e(TAG, "Error getting app info for " + packageInfo.packageName + ": " + e.getMessage());
                }
            }
        }
        return appList;
    }

    /**
     * Retrieves detailed information for a specific application, including sizes and permissions.
     *
     * @param packageName The package name of the application.
     * @return An AppInfo object with detailed information, or null if not found.
     */
    public AppInfo getAppDetails(String packageName) {
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName,
                    PackageManager.VERIFICATION_ALLOW |
                            PackageManager.GET_PERMISSIONS |
                            PackageManager.GET_RECEIVERS |
                            PackageManager.GET_SERVICES |
                            PackageManager.GET_PROVIDERS);

            ApplicationInfo appInfo = packageInfo.applicationInfo;

            String appName = packageManager.getApplicationLabel(appInfo).toString();
            Drawable icon = packageManager.getApplicationIcon(appInfo);

            AppInfo details = new AppInfo(appName, packageName, icon);
            details.versionName = packageInfo.versionName;
            details.versionCode = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ?
                    (int) packageInfo.getLongVersionCode() : packageInfo.versionCode;
            details.installTime = packageInfo.firstInstallTime;
            details.lastUpdateTime = packageInfo.lastUpdateTime;
            details.isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

            // Get approximate sizes (this is often the APK size on device)
            File apkFile = new File(appInfo.sourceDir);
            if (apkFile.exists()) {
                details.codeSize = apkFile.length();
            }

            // For data and cache size, you usually need to clear app data from system settings
            // or use specific APIs that require `PACKAGE_USAGE_STATS` or other privileged permissions.
            // Direct programmatic access to data/cache size for *other* apps is highly restricted.
            // A common approach is to launch the system's app info screen where sizes are displayed.
            // For a rough estimate, we'll leave data/cache as 0 unless a specific API is used,
            // or the user navigates to system settings for accurate values.
            // For example, on devices >= API 26 (Oreo), `PackageManager.getPackageStorageInfo` was introduced
            // but still requires permissions or special access.

            // List permissions
            if (packageInfo.requestedPermissions != null) {
                for (String permName : packageInfo.requestedPermissions) {
                    boolean granted = (packageManager.checkPermission(permName, packageName) == PackageManager.PERMISSION_GRANTED);
                    try {
                        PermissionInfo permInfo = packageManager.getPermissionInfo(permName, 0);
                        String permGroup = (permInfo.group != null) ?
                                (packageManager.getPermissionGroupInfo(permInfo.group, 0).loadLabel(packageManager).toString()) : "N/A";
                        String permDescription = (permInfo.loadDescription(packageManager) != null) ?
                                permInfo.loadDescription(packageManager).toString() : "No description available.";
                        details.permissions.add(new PermissionDetail(permName, permGroup, permDescription, granted, permInfo.protectionLevel));
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.w(TAG, "Permission info not found for: " + permName);
                        details.permissions.add(new PermissionDetail(permName, "Unknown", "No description available.", granted, -1));
                    }
                }
            }

            return details;

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "App not found: " + packageName + ", " + e.getMessage());
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error getting app details for " + packageName + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks if the PACKAGE_USAGE_STATS permission is granted.
     * This permission is required to access app usage statistics.
     *
     * @return true if permission is granted, false otherwise.
     */
    public boolean hasUsageStatsPermission() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            return false;
        }
        long time = System.currentTimeMillis();
        // Query for usage stats in a small time range to check if permission is granted
        List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);
        return stats != null && !stats.isEmpty();
    }

    /**
     * Prompts the user to grant PACKAGE_USAGE_STATS permission.
     */
    public void requestUsageStatsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            context.startActivity(intent);
        } else {
            // For older Android versions, PACKAGE_USAGE_STATS is not available or handled differently
            // or the app already has it by default for some system apps.
            // A Toast message can inform the user that this feature might not be available.
            Log.w(TAG, "Usage Stats permission not applicable or handled differently on this Android version.");
        }
    }

    /**
     * Gets usage statistics for a specific package.
     * Requires PACKAGE_USAGE_STATS permission.
     *
     * @param packageName The package name.
     * @param interval    The interval for usage stats (e.g., UsageStatsManager.INTERVAL_DAILY).
     * @return A list of UsageStats objects for the given package.
     */
    public List<UsageStats> getAppUsageStats(String packageName, int interval) {
        if (!hasUsageStatsPermission()) {
            Log.w(TAG, "PACKAGE_USAGE_STATS permission not granted. Cannot retrieve usage stats.");
            return Collections.emptyList();
        }

        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            return Collections.emptyList();
        }

        long endTime = System.currentTimeMillis();
        // Get stats for the last 7 days (or adjust interval as needed)
        long startTime = endTime - TimeUnit.DAYS.toMillis(7);

        // Query for usage stats for a specific package
        List<UsageStats> queryStats = usageStatsManager.queryUsageStats(interval, startTime, endTime);
        List<UsageStats> filteredStats = new ArrayList<>();

        if (queryStats != null) {
            for (UsageStats usageStats : queryStats) {
                if (usageStats.getPackageName().equals(packageName)) {
                    filteredStats.add(usageStats);
                }
            }
        }
        return filteredStats;
    }

    /**
     * Utility method to format byte sizes into human-readable strings.
     *
     * @param bytes The size in bytes.
     * @param si    True for SI units (1000), false for binary (1024).
     * @return Formatted string (e.g., "1.2 MB").
     */
    public static String formatFileSize(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        DecimalFormat format = new DecimalFormat("#.##");
        return format.format(bytes / Math.pow(unit, exp)) + " " + pre + "B";
    }

    /**
     * Utility method to format time in milliseconds into human-readable duration.
     *
     * @param milliseconds The time in milliseconds.
     * @return Formatted string (e.g., "2h 30m").
     */
    public static String formatDuration(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + " ms";
        }

        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        long hours = TimeUnit.MINUTES.toHours(minutes);
        long days = TimeUnit.HOURS.toDays(hours);

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours % 24 > 0) { // Only show hours if there are some remaining after days
            sb.append(hours % 24).append("h ");
        }
        if (minutes % 60 > 0) { // Only show minutes if there are some remaining after hours
            sb.append(minutes % 60).append("m ");
        }
        if (seconds % 60 > 0 && days == 0 && hours == 0) { // Only show seconds if less than a minute
            sb.append(seconds % 60).append("s");
        }
        return sb.toString().trim();
    }
}
