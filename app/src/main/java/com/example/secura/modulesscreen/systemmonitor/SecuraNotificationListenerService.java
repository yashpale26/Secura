package com.example.secura.modulesscreen.systemmonitor;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.example.secura.R;
import com.example.secura.modulesscreen.systemmonitor.ScanResult;

import java.util.HashSet;
import java.util.Set;

public class SecuraNotificationListenerService extends NotificationListenerService {

    private static final String TAG = "SecuraNotificationListenerService";
    private final Set<String> suspiciousKeywords = new HashSet<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Notification Listener Service started.");
        // Initialize a set of keywords to look for in notifications
        suspiciousKeywords.add("install");
        suspiciousKeywords.add("download");
        suspiciousKeywords.add("malware");
        suspiciousKeywords.add("virus");
        suspiciousKeywords.add("update");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // This method is called when a new notification is posted
        String packageName = sbn.getPackageName();
        String notificationText = sbn.getNotification().extras.getString("android.text");
        String notificationTitle = sbn.getNotification().extras.getString("android.title");

        Log.d(TAG, "New Notification from: " + packageName);
        Log.d(TAG, "Title: " + notificationTitle);
        Log.d(TAG, "Text: " + notificationText);

        if (notificationText != null) {
            for (String keyword : suspiciousKeywords) {
                if (notificationText.toLowerCase().contains(keyword) || (notificationTitle != null && notificationTitle.toLowerCase().contains(keyword))) {
                    try {
                        PackageManager pm = getPackageManager();
                        ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                        String appName = (String) pm.getApplicationLabel(appInfo);
                        Drawable appIcon = pm.getApplicationIcon(packageName);

                        // You can send a local broadcast here to update the UI
                        // For simplicity, we'll just log and a Toast for this example
                        String alertMessage = "Potential malicious notification detected from " + appName + ". Reason: " + keyword + " found in text.";
                        Log.d(TAG, "Alert: " + alertMessage);
                        // A broadcast can be sent to SecurityMonitorActivity to display this
                        // Intent broadcastIntent = new Intent("com.example.secura.SECURITY_ALERT");
                        // broadcastIntent.putExtra("alert_message", alertMessage);
                        // sendBroadcast(broadcastIntent);

                    } catch (PackageManager.NameNotFoundException e) {
                        Log.e(TAG, "Package name not found: " + packageName);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // This method is called when a notification is removed
        Log.d(TAG, "Notification removed: " + sbn.getPackageName());
    }
}
