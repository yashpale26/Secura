package com.example.secura.modulesscreen.privacyprotectors.intruderselfie;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleService;

import com.example.secura.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class IntruderSelfieCaptureService extends LifecycleService {

    private static final String TAG = "IntruderSelfieService";
    private static final String CHANNEL_ID = "IntruderSelfieChannel";
    private static final int NOTIFICATION_ID = 101;

    private IntruderSelfieCameraHelper cameraHelper;
    private final Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service Created");
        createNotificationChannel();
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "Service onStartCommand");

        // Foreground to survive while device is locked
        startForeground(NOTIFICATION_ID, createNotification());

        // V3 behavior: this service acts as LifecycleOwner
        cameraHelper = new IntruderSelfieCameraHelper(this, this);

        // Give the camera a bit of time to spin up (slightly longer than V3)
        handler.postDelayed(() -> {
            cameraHelper.takeIntruderSelfie();
            // Stop shortly after to let capture complete
            handler.postDelayed(this::stopSelf, 3500);
        }, 900); // V3 used 500ms; 900ms is safer on some devices

        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Intruder Selfie Service";
            String description = "Captures photos of intruders on failed unlock attempts.";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Intruder Selfie Active")
                .setContentText("Capturing photo on failed unlock attempt…")
                .setSmallIcon(R.drawable.ic_security_shield)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service Destroyed");
        try {
            ListenableFuture<ProcessCameraProvider> fut = ProcessCameraProvider.getInstance(this);
            fut.addListener(() -> {
                try {
                    ProcessCameraProvider provider = fut.get();
                    provider.unbindAll();
                } catch (ExecutionException | InterruptedException ignored) {}
            }, ContextCompat.getMainExecutor(this));
        } catch (Exception ignored) {}
        handler.removeCallbacksAndMessages(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return null;
    }
}
