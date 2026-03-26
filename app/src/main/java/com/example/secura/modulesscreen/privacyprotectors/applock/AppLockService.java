package com.example.secura.modulesscreen.privacyprotectors.applock;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.Set;

public class AppLockService extends AccessibilityService {

    private static final String TAG = "AppLockService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            CharSequence packageNameCharSeq = event.getPackageName();
            if (packageNameCharSeq == null) return;

            String packageName = packageNameCharSeq.toString();

            SharedPreferences prefs = getSharedPreferences("app_lock_prefs", MODE_PRIVATE);
            Set<String> lockedApps = prefs.getStringSet("locked_apps", null);
            String tempUnlocked = prefs.getString("temp_unlocked_app", "");

            if (lockedApps != null && lockedApps.contains(packageName)) {
                if (!packageName.equals(tempUnlocked)) {
                    Log.d(TAG, "Locked app detected: " + packageName);
                    Intent intent = new Intent(this, AppLockActivity.class);
                    intent.putExtra("locked_package", packageName);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Log.d(TAG, "Temporary unlock allowed: " + packageName);
                    prefs.edit().remove("temp_unlocked_app").apply();
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "AppLockService interrupted");
    }
}
