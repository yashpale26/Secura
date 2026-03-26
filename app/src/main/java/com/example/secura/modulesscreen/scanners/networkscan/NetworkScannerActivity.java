package com.example.secura.modulesscreen.scanners.networkscan;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.secura.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NetworkScannerActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;

    // UI Components
    private SwitchMaterial permissionSwitch;
    private Button scanButton;
    private LinearLayout loadingContainer;
    private ProgressBar progressBar;
    private TextView statusTextView;
    private MaterialCardView resultCard;
    private TextView wifiInfoTextView;
    private TextView wifiSafetyStatusTextView;

    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_scanner);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Network Scanner");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize UI components and system services
        permissionSwitch = findViewById(R.id.permissionSwitch);
        scanButton = findViewById(R.id.scanButton);
        loadingContainer = findViewById(R.id.loadingContainer);
        progressBar = findViewById(R.id.progressBar);
        statusTextView = findViewById(R.id.statusTextView);
        resultCard = findViewById(R.id.resultCard);
        wifiInfoTextView = findViewById(R.id.wifiInfoTextView);
        wifiSafetyStatusTextView = findViewById(R.id.wifiSafetyStatusTextView);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Initial check for permissions
        updatePermissionUI();

        // Handle permission switch toggle
        permissionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestLocationPermission();
            } else {
                updatePermissionUI();
            }
        });

        // Handle scan button click
        scanButton.setOnClickListener(v -> scanWifiDetails());
    }

    /**
     * Checks if the required location permission is granted.
     * @return true if permission is granted, false otherwise.
     */
    private boolean hasLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Permissions are not required on older Android versions
    }

    /**
     * Updates the UI elements related to permissions (switch and scan button).
     */
    private void updatePermissionUI() {
        boolean hasPermission = hasLocationPermission();
        permissionSwitch.setChecked(hasPermission);
        scanButton.setEnabled(hasPermission);
        if (hasPermission) {
            permissionSwitch.setText("Grant Required Permission");
        } else {
            permissionSwitch.setText("Grant Required Permission");
        }
    }

    /**
     * Requests the ACCESS_FINE_LOCATION permission from the user.
     */
    private void requestLocationPermission() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission is required for Wi-Fi scanning.", Toast.LENGTH_LONG).show();
            }
            updatePermissionUI();
        }
    }

    /**
     * Starts the network scanning and analysis process in a background thread.
     */
    private void scanWifiDetails() {
        // Show loading animation and hide previous results
        loadingContainer.setVisibility(View.VISIBLE);
        resultCard.setVisibility(View.GONE);
        wifiSafetyStatusTextView.setVisibility(View.GONE);
        scanButton.setEnabled(false);

        // Run the heavy lifting on a background thread
        new Thread(this::performNetworkScanAndAnalysis).start();
    }

    /**
     * Performs all network scanning and analysis tasks. This runs on a background thread.
     */
    private void performNetworkScanAndAnalysis() {
        updateStatus("Scanning for network details...");

        boolean isWifiConnected = isWifiConnected(connectivityManager);

        if (wifiManager == null || !wifiManager.isWifiEnabled() || !isWifiConnected) {
            updateUIWithResults(null, "Wi-Fi is not connected. Please connect to a network and try again.");
            return;
        }

        try {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();

            if (wifiInfo == null || dhcpInfo == null) {
                updateUIWithResults(null, "Wi-Fi connection info not accessible. Please try scanning again.");
                return;
            }

            // Get network details
            String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
            String gatewayIp = Formatter.formatIpAddress(dhcpInfo.gateway);

            // Format Wi-Fi details string
            String wifiDetails = String.format(Locale.getDefault(),
                    "📶 SSID: %s\n" +
                            "📡 BSSID (AP MAC): %s\n" +
                            "📱 Device Name: %s\n" +
                            "🌐 IP Address: %s\n" +
                            "🧭 Gateway IP: %s\n" +
                            "🔒 MAC Address (Device): %s\n" +
                            "⚡ Link Speed: %d Mbps\n" +
                            "📻 Frequency: %d MHz\n" +
                            "🆔 Network ID: %d\n" +
                            "📉 RSSI (Signal Strength): %d dBm\n" +
                            "🌐 DNS 1: %s\n" +
                            "🌐 DNS 2: %s",
                    wifiInfo.getSSID().replace("\"", ""),
                    wifiInfo.getBSSID(),
                    deviceName,
                    Formatter.formatIpAddress(wifiInfo.getIpAddress()),
                    gatewayIp,
                    wifiInfo.getMacAddress(),
                    wifiInfo.getLinkSpeed(),
                    wifiInfo.getFrequency(),
                    wifiInfo.getNetworkId(),
                    wifiInfo.getRssi(),
                    Formatter.formatIpAddress(dhcpInfo.dns1),
                    Formatter.formatIpAddress(dhcpInfo.dns2)
            );

            // Perform network safety analysis
            String safetyStatus = analyzeNetworkSafety(wifiInfo, gatewayIp);

            // Post results to the main UI thread
            updateUIWithResults(wifiDetails, safetyStatus);

        } catch (Exception e) {
            Log.e("NetworkScanner", "An error occurred during scan", e);
            updateUIWithResults(null, "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Checks if Wi-Fi is currently connected.
     */
    private boolean isWifiConnected(ConnectivityManager connectivityManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            }
        } else {
            @SuppressWarnings("deprecation")
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    /**
     * Performs a more advanced safety analysis of the network.
     * @param wifiInfo The current WifiInfo object.
     * @param gatewayIp The gateway's IP address.
     * @return A detailed safety status string.
     */
    private String analyzeNetworkSafety(WifiInfo wifiInfo, String gatewayIp) {
        StringBuilder safetyReport = new StringBuilder();
        List<String> warnings = new ArrayList<>();
        List<String> safeChecks = new ArrayList<>();

        // Check 1: Public Wi-Fi network (based on SSID)
        if (wifiInfo.getSSID() != null && wifiInfo.getSSID().toLowerCase(Locale.getDefault()).contains("public")) {
            warnings.add("Connected to a Public Wi-Fi network.");
        } else {
            safeChecks.add("Network does not appear to be a public hotspot.");
        }

        // Check 2: DNS Server configuration
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        if (dhcpInfo != null) {
            String dns1 = Formatter.formatIpAddress(dhcpInfo.dns1);
            String dns2 = Formatter.formatIpAddress(dhcpInfo.dns2);
            // Simple check to see if DNS is on the same local network as the gateway.
            // If DNS is a public one, it's likely safe. If it's on the local net, it could be a simple router, but also a malicious one.
            if (!dns1.equals(gatewayIp) && !dns2.equals(gatewayIp) && !dns1.equals("0.0.0.0") && !dns2.equals("0.0.0.0")) {
                safeChecks.add("DNS servers are configured correctly.");
            } else {
                warnings.add("DNS servers might be provided by the local network, which could be a risk.");
            }
        }

        // Check 3: Simple Port Scan on Gateway
        updateStatus("Checking for open ports on the gateway...");
        int[] commonPorts = {21, 22, 23, 80, 443, 8080, 8443};
        StringBuilder openPortsMessage = new StringBuilder();
        int openPortCount = 0;
        for (int port : commonPorts) {
            if (isPortOpen(gatewayIp, port, 500)) {
                openPortsMessage.append(port).append(" ");
                openPortCount++;
            }
        }

        if (openPortCount > 0) {
            warnings.add("The network gateway has " + openPortCount + " open ports: " + openPortsMessage.toString().trim() + ". This may indicate a vulnerability.");
        } else {
            safeChecks.add("No common ports were found open on the network gateway.");
        }

        // Build the final report
        if (warnings.isEmpty()) {
            safetyReport.append("✅ Wi-Fi Network appears safe.\n\n");
            safetyReport.append("Details:\n");
            for(String check : safeChecks) {
                safetyReport.append(" - ").append(check).append("\n");
            }
        } else {
            safetyReport.append("⚠️ Warning: Network has potential security risks.\n\n");
            safetyReport.append("Details:\n");
            for(String warning : warnings) {
                safetyReport.append(" - ").append(warning).append("\n");
            }
            if(!safeChecks.isEmpty()){
                safetyReport.append("\nPositive Checks:\n");
                for(String check : safeChecks) {
                    safetyReport.append(" - ").append(check).append("\n");
                }
            }
        }

        return safetyReport.toString();
    }

    /**
     * Attempts to connect to a specific port on a given host to check if it's open.
     * @param host The host IP address.
     * @param port The port number to check.
     * @param timeout The connection timeout in milliseconds.
     * @return true if the port is open, false otherwise.
     */
    private boolean isPortOpen(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Updates the UI from a background thread using a Handler.
     * @param text The text to display in the status TextView.
     */
    private void updateStatus(String text) {
        uiHandler.post(() -> statusTextView.setText(text));
    }

    /**
     * Finalizes the UI display with the results from the background scan.
     * @param wifiDetails The detailed Wi-Fi information string, or null if an error occurred.
     * @param safetyStatus The detailed safety report string.
     */
    private void updateUIWithResults(String wifiDetails, String safetyStatus) {
        uiHandler.post(() -> {
            loadingContainer.setVisibility(View.GONE);
            scanButton.setEnabled(true);

            if (wifiDetails != null) {
                wifiInfoTextView.setText(wifiDetails);
                resultCard.setVisibility(View.VISIBLE);
            } else {
                // Display error message
                wifiInfoTextView.setText(safetyStatus);
                resultCard.setVisibility(View.VISIBLE);
                wifiSafetyStatusTextView.setVisibility(View.GONE);
                return;
            }

            wifiSafetyStatusTextView.setText(safetyStatus);
            wifiSafetyStatusTextView.setVisibility(View.VISIBLE);

            if (safetyStatus.contains("Warning")) {
                wifiSafetyStatusTextView.setTextColor(getColor(R.color.error_red));
            } else {
                wifiSafetyStatusTextView.setTextColor(getColor(R.color.blue_dark));
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}