package com.example.secura.modulesscreen.scanners.callandsmsscan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.secura.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CallSmsScannerActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CALL_LOG
    };

    private SwitchMaterial permissionsSwitch;
    private Button scanButton;
    private ProgressBar progressBar;
    private TextView resultSummaryTextView;
    private ListView spamCallsListView;
    private ListView spamSmsListView;

    private SpamItemAdapter spamCallsAdapter;
    private SpamItemAdapter spamSmsAdapter;

    private List<SpamItem> spamCallsList = new ArrayList<>();
    private List<SpamItem> spamSmsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_sms_scanner);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Call & SMS Scanner");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        permissionsSwitch = findViewById(R.id.permissionsSwitch);
        scanButton = findViewById(R.id.scanButton);
        progressBar = findViewById(R.id.progressBar);
        resultSummaryTextView = findViewById(R.id.resultSummaryTextView);
        spamCallsListView = findViewById(R.id.spamCallsListView);
        spamSmsListView = findViewById(R.id.spamSmsListView);

        spamCallsAdapter = new SpamItemAdapter(this, spamCallsList);
        spamSmsAdapter = new SpamItemAdapter(this, spamSmsList);

        spamCallsListView.setAdapter(spamCallsAdapter);
        spamSmsListView.setAdapter(spamSmsAdapter);

        // Configure ListView for multiple selections (Action Mode)
        spamCallsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        spamCallsListView.setMultiChoiceModeListener(new SpamMultiChoiceModeListener(spamCallsAdapter, spamCallsList, true));

        spamSmsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        spamSmsListView.setMultiChoiceModeListener(new SpamMultiChoiceModeListener(spamSmsAdapter, spamSmsList, false));

        permissionsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkAndRequestPermissions();
            } else {
                Toast.makeText(this, "Permissions are required to run the scanner.", Toast.LENGTH_SHORT).show();
            }
        });

        scanButton.setOnClickListener(v -> {
            if (permissionsSwitch.isChecked() && arePermissionsGranted()) {
                startScanning();
            } else {
                Toast.makeText(this, "Please grant required permissions first.", Toast.LENGTH_SHORT).show();
                permissionsSwitch.setChecked(false);
            }
        });

        permissionsSwitch.setChecked(arePermissionsGranted());
    }

    private boolean arePermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void checkAndRequestPermissions() {
        if (!arePermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            permissionsSwitch.setChecked(allGranted);
            if (!allGranted) {
                Toast.makeText(this, "Permissions denied. Cannot perform scan.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startScanning() {
        spamCallsList.clear();
        spamSmsList.clear();
        spamCallsAdapter.notifyDataSetChanged();
        spamSmsAdapter.notifyDataSetChanged();
        resultSummaryTextView.setText("");
        progressBar.setVisibility(View.VISIBLE);
        scanButton.setEnabled(false);

        new ScanTask().execute();
    }

    private class ScanTask extends AsyncTask<Void, Void, Void> {
        private int spamCallsCount = 0;
        private int spamSmsCount = 0;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            scanCallLogs();
            scanSmsMessages();
            return null;
        }

        private void scanCallLogs() {
            long thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);

            try (Cursor managedCursor = getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    null,
                    CallLog.Calls.DATE + " > ?",
                    new String[]{String.valueOf(thirtyDaysAgo)},
                    CallLog.Calls.DATE + " DESC"
            )) {
                if (managedCursor != null && managedCursor.moveToFirst()) {
                    int idCol = managedCursor.getColumnIndex(CallLog.Calls._ID); // Fetch the ID
                    int numberCol = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
                    int dateCol = managedCursor.getColumnIndex(CallLog.Calls.DATE);
                    int durationCol = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
                    int typeCol = managedCursor.getColumnIndex(CallLog.Calls.TYPE);

                    do {
                        long callId = managedCursor.getLong(idCol); // Get the ID
                        String phNumber = managedCursor.getString(numberCol);
                        long callDate = managedCursor.getLong(dateCol);
                        long callDuration = managedCursor.getLong(durationCol);
                        int callType = managedCursor.getInt(typeCol);

                        String contactName = getContactName(phNumber);
                        if (contactName == null) {
                            if (isSpamCall(phNumber, callDuration, callType)) {
                                spamCallsCount++;
                                // Pass the call ID to the SpamItem
                                spamCallsList.add(new SpamItem("Call", phNumber, callDate, callDuration, callId));
                            }
                        }
                    } while (managedCursor.moveToNext());
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        private boolean isSpamCall(String phoneNumber, long duration, int type) {
            if (phoneNumber == null || phoneNumber.isEmpty() || phoneNumber.equals("-1") || phoneNumber.equalsIgnoreCase("private") || phoneNumber.equalsIgnoreCase("unknown")) {
                return true;
            }
            if ((type == CallLog.Calls.INCOMING_TYPE || type == CallLog.Calls.MISSED_TYPE) && duration < 10) {
                if (type == CallLog.Calls.MISSED_TYPE && duration == 0) {
                    return true;
                }
                if (type == CallLog.Calls.INCOMING_TYPE && duration < 5) {
                    return true;
                }
            }
            if (phoneNumber.startsWith("+")) {
                if (phoneNumber.startsWith("+2") || phoneNumber.startsWith("+882") || phoneNumber.startsWith("+375") || phoneNumber.startsWith("+252")) {
                    return true;
                }
                if (phoneNumber.length() > 15) {
                    return true;
                }
            }
            if (phoneNumber.matches("^\\d{2,7}$")) {
                if (type == CallLog.Calls.MISSED_TYPE || (type == CallLog.Calls.INCOMING_TYPE && duration < 10)) {
                    return true;
                }
            }
            if (phoneNumber.matches("^[a-zA-Z0-9]{3,11}$")) {
                return true;
            }
            if (!phoneNumber.matches("^[\\d\\+\\*#]+$")) {
                return true;
            }
            return false;
        }

        private void scanSmsMessages() {
            long thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);

            try (Cursor cursor = getContentResolver().query(
                    Uri.parse("content://sms/inbox"),
                    null,
                    Telephony.Sms.DATE + " > ?",
                    new String[]{String.valueOf(thirtyDaysAgo)},
                    Telephony.Sms.DATE + " DESC"
            )) {
                if (cursor != null && cursor.moveToFirst()) {
                    int addressCol = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS);
                    int bodyCol = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY);
                    int dateCol = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE);
                    int idCol = cursor.getColumnIndexOrThrow(Telephony.Sms._ID);

                    do {
                        String address = cursor.getString(addressCol);
                        String body = cursor.getString(bodyCol);
                        long date = cursor.getLong(dateCol);
                        long id = cursor.getLong(idCol);

                        String contactName = getContactName(address);
                        boolean isSpam = isSpamSms(address, body, contactName);

                        if (isSpam) {
                            spamSmsCount++;
                            spamSmsList.add(new SpamItem("SMS", address, body, date, id));
                        }
                    } while (cursor.moveToNext());
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        private boolean isSpamSms(String address, String body, String contactName) {
            if (contactName != null) {
                return false;
            }
            if (containsSpamKeyword(body)) {
                return true;
            }
            if (containsSuspiciousUrl(body)) {
                return true;
            }
            if (address.matches("^\\d{2,6}$")) {
                if (containsSpamKeyword(body) || containsSuspiciousUrl(body)) {
                    return true;
                }
                if (body.toLowerCase().contains("otp") && address.length() < 6 && address.length() > 0) {
                    return true;
                }
            }
            if (address.contains("@") || address.length() > 15) {
                return true;
            }
            return false;
        }

        private boolean containsSpamKeyword(String text) {
            String[] SPAM_KEYWORDS = {
                    "winner", "prize", "lottery", "congratulations", "claim now", "urgent",
                    "verify account", "free money", "discount code", "suspicious activity",
                    "your account has been locked", "delivery pending", "click link", "loan approved",
                    "credit score", "aadhaar", "pan card", "upi block", "otp", "kyc update",
                    "reward points", "card blocked", "unauthorized transaction", "pay fee",
                    "limited time offer", "exclusive deal", "investment opportunity", "crypto",
                    "transfer funds", "job offer from home", "secret shopper", "package tracking",
                    "bank account", "debit card", "credit card", "update your details", "OTP"
            };
            String lowerCaseText = text.toLowerCase();
            for (String keyword : SPAM_KEYWORDS) {
                if (lowerCaseText.contains(keyword)) {
                    return true;
                }
            }
            return false;
        }

        private boolean containsSuspiciousUrl(String text) {
            String[] SUSPICIOUS_URL_PATTERNS = {
                    "bit\\.ly", "tinyurl\\.com", "goo\\.gl", "ow\\.ly", "t\\.co",
                    "\\.xyz", "\\.top", "\\.loan", "\\.win", "\\.bid", "\\.buzz", "\\.icu",
                    "\\.ru", "\\.cn", "\\.tk", "\\.ml", "\\.ga", "\\.cf", "\\.gq"
            };
            String lowerCaseText = text.toLowerCase();
            Pattern urlPattern = Pattern.compile("https?://\\S+");
            Matcher matcher = urlPattern.matcher(lowerCaseText);
            while (matcher.find()) {
                String url = matcher.group();
                for (String suspiciousPattern : SUSPICIOUS_URL_PATTERNS) {
                    if (url.contains(suspiciousPattern)) {
                        return true;
                    }
                }
                if (url.matches("https?://\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(:\\d+)?/?\\S*")) {
                    return true;
                }
            }
            return false;
        }

        private String getContactName(String phoneNumber) {
            String contactName = null;
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                return null;
            }
            try (Cursor cursor = getContentResolver().query(
                    Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber)),
                    new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME},
                    null, null, null
            )) {
                if (cursor != null && cursor.moveToFirst()) {
                    contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return contactName;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            scanButton.setEnabled(true);

            String summary = String.format("Scan complete for the last 30 days.\nFound %d suspected spam calls and %d suspected spam SMS.", spamCallsCount, spamSmsCount);
            resultSummaryTextView.setText(summary);

            spamCallsAdapter.notifyDataSetChanged();
            spamSmsAdapter.notifyDataSetChanged();

            if (spamCallsList.isEmpty()) {
                findViewById(R.id.callsHeader).setVisibility(View.GONE);
                spamCallsListView.setVisibility(View.GONE);
            } else {
                findViewById(R.id.callsHeader).setVisibility(View.VISIBLE);
                spamCallsListView.setVisibility(View.VISIBLE);
            }

            if (spamSmsList.isEmpty()) {
                findViewById(R.id.smsHeader).setVisibility(View.GONE);
                spamSmsListView.setVisibility(View.GONE);
            } else {
                findViewById(R.id.smsHeader).setVisibility(View.VISIBLE);
                spamSmsListView.setVisibility(View.VISIBLE);
            }
        }
    }

    private class SpamMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {

        private SpamItemAdapter adapter;
        private List<SpamItem> items;
        private boolean isCallList;

        public SpamMultiChoiceModeListener(SpamItemAdapter adapter, List<SpamItem> items, boolean isCallList) {
            this.adapter = adapter;
            this.items = items;
            this.isCallList = isCallList;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            final int checkedCount = ((ListView) (isCallList ? spamCallsListView : spamSmsListView)).getCheckedItemCount();
            mode.setTitle(checkedCount + " Selected");
            adapter.notifyDataSetChanged();
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.contextual_menu_delete, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                deleteSelectedSpamItems();
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // No action needed here, as the adapter handles clearing the selection on notifyDataSetChanged
        }

        private void deleteSelectedSpamItems() {
            SparseBooleanArray checkedItems = ((ListView) (isCallList ? spamCallsListView : spamSmsListView)).getCheckedItemPositions();
            ArrayList<SpamItem> itemsToDelete = new ArrayList<>();
            for (int i = checkedItems.size() - 1; i >= 0; i--) {
                if (checkedItems.valueAt(i)) {
                    SpamItem spamItem = adapter.getItem(checkedItems.keyAt(i));
                    if (spamItem != null) {
                        itemsToDelete.add(spamItem);
                    }
                }
            }
            for (SpamItem item : itemsToDelete) {
                if (isCallList) {
                    deleteCallLog(item.getId()); // Use the item's unique ID for deletion
                } else {
                    deleteSms(item.getId());
                }
                items.remove(item);
            }
            adapter.notifyDataSetChanged();
            Toast.makeText(CallSmsScannerActivity.this, itemsToDelete.size() + (isCallList ? " calls" : " SMS") + " deleted.", Toast.LENGTH_SHORT).show();
            updateSummary();
        }
    }

    private void deleteCallLog(long id) {
        try {
            getContentResolver().delete(
                    CallLog.Calls.CONTENT_URI,
                    CallLog.Calls._ID + " = ?", // Use the ID for deletion
                    new String[]{String.valueOf(id)}
            );
        } catch (SecurityException e) {
            Toast.makeText(this, "Permission to delete calls not granted.", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteSms(long id) {
        try {
            getContentResolver().delete(
                    Uri.parse("content://sms/"),
                    "_id = ?",
                    new String[]{String.valueOf(id)}
            );
        } catch (SecurityException e) {
            Toast.makeText(this, "Permission to delete SMS not granted.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSummary() {
        int callCount = spamCallsList.size();
        int smsCount = spamSmsList.size();
        String summary = String.format("Scan complete for the last 30 days.\nFound %d suspected spam calls and %d suspected spam SMS.", callCount, smsCount);
        resultSummaryTextView.setText(summary);
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