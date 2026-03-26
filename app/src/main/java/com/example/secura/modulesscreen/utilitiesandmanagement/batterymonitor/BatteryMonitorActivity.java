package com.example.secura.modulesscreen.utilitiesandmanagement.batterymonitor;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.secura.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class BatteryMonitorActivity extends AppCompatActivity {

    private SwitchMaterial permissionSwitch;
    private ProgressBar batteryProgressBar;             // old bar (kept but unused now)
    private ProgressBar batteryProgressAnimated;        // circular ring
    private TextView batteryPercentText;                // % inside ring
    private FrameLayout batteryRingContainer;           // container for ring + text
    private TextView batteryInfoTextView;
    private Button powerSavingButton;

    private ObjectAnimator batteryAnimator;             // smooth % animation
    private Animation pulseAnimation;                   // glow animation when charging

    // Old spin animator reference (kept but unused now)
    // private ObjectAnimator chargingSpinAnimator;

    private final BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBatteryInfo(intent);
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_monitor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Battery Monitor");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Init
        permissionSwitch = findViewById(R.id.permission_switch);
        // batteryProgressBar = findViewById(R.id.battery_progress_bar); // old, hidden in layout
        batteryInfoTextView = findViewById(R.id.battery_info_text);
        powerSavingButton = findViewById(R.id.power_saving_button);

        batteryRingContainer = findViewById(R.id.battery_ring_container);
        batteryProgressAnimated = findViewById(R.id.battery_progress_animated);
        batteryPercentText = findViewById(R.id.battery_percent_text);

        // Load glow/pulse animation (this will be our charging effect)
        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_glow2);

        // Listeners
        permissionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) requestUsageStatsPermission();
        });

        powerSavingButton.setOnClickListener(v -> togglePowerSaver());
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryInfoReceiver, filter);
        Intent batteryStatus = registerReceiver(null, filter);
        updateBatteryInfo(batteryStatus);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(batteryInfoReceiver);
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

    private void updateBatteryInfo(Intent batteryStatus) {
        if (batteryStatus == null) return;

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float) scale;
        int percentage = (int) (batteryPct * 100);

        // Old bar kept, but comment this line to avoid NPE
        // if (batteryProgressBar != null) batteryProgressBar.setProgress(percentage);

        // Animate circular ring smoothly
        if (batteryAnimator != null && batteryAnimator.isRunning()) batteryAnimator.cancel();
        batteryAnimator = ObjectAnimator.ofInt(
                batteryProgressAnimated,
                "progress",
                batteryProgressAnimated.getProgress(),
                percentage
        );
        batteryAnimator.setDuration(900);
        batteryAnimator.setInterpolator(new DecelerateInterpolator());
        batteryAnimator.start();

        // Update % text
        batteryPercentText.setText(percentage + "%");

        // Zone colors
        int zoneColorRes = percentage <= 15 ? R.color.battery_red
                : (percentage <= 50 ? R.color.battery_yellow : R.color.battery_green);
        int zoneColor = getColorCompat(zoneColorRes);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryProgressAnimated.setProgressTintList(ColorStateList.valueOf(zoneColor));
            batteryPercentText.setTextColor(zoneColor); // also tint % text
        }

        // Info text
        String statusText = "Battery Level: " + percentage + "%\n";

        // Charging
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;
        if (isCharging) {
            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            boolean wirelessCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS;

            String chargeSource;
            if (usbCharge) chargeSource = "USB";
            else if (acCharge) chargeSource = "AC";
            else if (wirelessCharge) chargeSource = "Wireless";
            else chargeSource = "Unknown";

            statusText += "Charging: Yes (" + chargeSource + ")\n";

            // 🔥 New breathing glow effect
            if (batteryRingContainer.getAnimation() == null) {
                batteryRingContainer.startAnimation(pulseAnimation);
            }

        } else {
            statusText += "Charging: No\n";
            batteryRingContainer.clearAnimation();
        }

        // Health
        int health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        String healthStatus;
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD: healthStatus = "Good"; break;
            case BatteryManager.BATTERY_HEALTH_COLD: healthStatus = "Cold"; break;
            case BatteryManager.BATTERY_HEALTH_DEAD: healthStatus = "Dead"; break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT: healthStatus = "Overheat"; break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE: healthStatus = "Over Voltage"; break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE: healthStatus = "Unspecified Failure"; break;
            default: healthStatus = "Unknown"; break;
        }
        statusText += "Health: " + healthStatus + "\n";

        // Temp
        int temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        if (temperature != -1) {
            double tempCelsius = temperature / 10.0;
            statusText += "Temperature: " + tempCelsius + "°C\n";
        }

        batteryInfoTextView.setText(statusText);

        if (percentage <= 15) showLowBatteryNotification(percentage);
    }

    private void showLowBatteryNotification(int percentage) {
        Toast.makeText(this, "Low Battery Warning: " + percentage + "% remaining!", Toast.LENGTH_SHORT).show();
    }

    private void togglePowerSaver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Intent intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
            if (intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
            else Toast.makeText(this, "Power saving settings not available on this device.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Power saving is not supported on your device's Android version.", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestUsageStatsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            if (intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
            else Toast.makeText(this, "Usage Stats settings not available.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Usage Stats permission not required on this Android version.", Toast.LENGTH_SHORT).show();
        }
    }

    private int getColorCompat(int resId) {
        return ContextCompat.getColor(this, resId);
    }
}
