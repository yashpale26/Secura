package com.example.secura.modulesscreen.systemmonitor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.secura.MainActivity;
import com.example.secura.R;

public class SecurityMonitorService extends Service {

    private static final String TAG = "SecurityMonitorService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "SecuraMonitorChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SecurityMonitorService created.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SecurityMonitorService started.");
        startForeground(NOTIFICATION_ID, createNotification());
        // The service will simulate monitoring here.
        // For a real app, you would have a worker thread or an event bus
        // to handle security checks continuously.
        return START_STICKY; // Service will be restarted if killed by the system
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SecurityMonitorService destroyed.");
    }

    private Notification createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Secura Security Monitor",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Security Monitor Active")
                .setContentText("Your device is being monitored for threats.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
    }
}
