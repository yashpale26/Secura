package com.example.secura.modulesscreen.privacyprotectors.intruderselfie;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class IntruderSelfieAdminReceiver extends DeviceAdminReceiver {

    private static final String TAG = "IntruderSelfieAdmin";
    private static final int FAILED_ATTEMPTS_THRESHOLD = 3;

    @Override
    public void onEnabled(Context context, Intent intent) {
        Log.d(TAG, "Device Admin Enabled");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return "Disabling Device Administrator will turn off the Intruder Selfie feature. Are you sure?";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Log.d(TAG, "Device Admin Disabled");
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        Log.d(TAG, "Password Failed Attempt Detected!");
        int failedAttempts = getManager(context).getCurrentFailedPasswordAttempts();
        Log.d(TAG, "Failed attempts: " + failedAttempts);

        // Use >= to be resilient if the OS skips reporting an attempt
        if (failedAttempts >= FAILED_ATTEMPTS_THRESHOLD) {
            Log.d(TAG, "Threshold reached. Attempting to take selfie.");
            Intent serviceIntent = new Intent(context, IntruderSelfieCaptureService.class);
            // Reliable on all versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        Log.d(TAG, "Password Succeeded.");
    }
}
