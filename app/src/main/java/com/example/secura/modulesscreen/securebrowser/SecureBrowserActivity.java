package com.example.secura.modulesscreen.securebrowser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.secura.R;
import com.google.android.material.button.MaterialButton;

// This is the updated SecureBrowserActivity.java file.
// It includes a new clear button for the URL bar, and a button to view blocked links.
// The dialog for adding a blocked link now also includes a "Name" field.
// This version adds back button functionality.

public class SecureBrowserActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private EditText urlInput;
    private ImageButton goButton;
    private ImageButton clearButton; // New clear button
    private ImageButton backButton; // New back button for the URL bar
    private MaterialButton addBlockButton;
    private MaterialButton viewBlockedLinksButton; // New button to view blocked links
    private LinkBlocker linkBlocker;
    private TextView blockListStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_browser);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Secure Browser");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize UI components
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        urlInput = findViewById(R.id.url_input);
        goButton = findViewById(R.id.go_button);
        clearButton = findViewById(R.id.clear_button); // Initialize clear button
        backButton = findViewById(R.id.back_button_browser); // Initialize back button
        addBlockButton = findViewById(R.id.add_block_button);
        viewBlockedLinksButton = findViewById(R.id.view_blocked_links_button); // Initialize view blocked links button
        blockListStatus = findViewById(R.id.blocked_links_count);

        // Initialize LinkBlocker with this activity's context
        linkBlocker = new LinkBlocker(this);

        // Setup WebView settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable JavaScript
        webSettings.setDomStorageEnabled(true); // Enable DOM storage
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT); // Set cache mode

        // Set WebView clients for custom behavior
        webView.setWebViewClient(new SecureWebViewClient(this, linkBlocker));
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
                progressBar.setProgress(newProgress);
            }
        });

        // Load a default homepage
        webView.loadUrl("https://www.google.com");
        urlInput.setText("https://www.google.com");

        // Set up the Go button click listener
        goButton.setOnClickListener(v -> {
            String url = urlInput.getText().toString().trim();
            if (!url.isEmpty()) {
                // Ensure URL has a scheme (e.g., http:// or https://)
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }
                loadUrl(url);
            }
        });

        // Set up the Clear button click listener
        clearButton.setOnClickListener(v -> urlInput.setText(""));

        // Set up the new back button click listener
        backButton.setOnClickListener(v -> {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                Toast.makeText(SecureBrowserActivity.this, "No history to go back to.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the Add Block button click listener
        addBlockButton.setOnClickListener(v -> showAddLinkDialog());

        // Set up the View Blocked Links button click listener
        viewBlockedLinksButton.setOnClickListener(v -> {
            Intent intent = new Intent(SecureBrowserActivity.this, BlockedLinksActivity.class);
            startActivity(intent);
        });

        // Update the blocked links count on start
        updateBlockListStatus();
    }

    private void loadUrl(String url) {
        // Check if the URL is blocked before loading
        if (linkBlocker.isUrlBlocked(url)) {
            showBlockedMessage(url);
        } else {
            webView.loadUrl(url);
            urlInput.setText(url);
        }
    }

    // Helper method to update the blocked links count
    private void updateBlockListStatus() {
        int count = linkBlocker.getBlockedLinksCount();
        blockListStatus.setText("Blocked Links: " + count);
    }

    // Displays an AlertDialog to add a new blocked link
    private void showAddLinkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_AlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_blocked_link, null);
        builder.setView(dialogView);

        final EditText inputName = dialogView.findViewById(R.id.dialog_name_input); // New Name input
        final EditText inputUrl = dialogView.findViewById(R.id.dialog_url_input);
        final EditText inputDescription = dialogView.findViewById(R.id.dialog_description_input);

        builder.setTitle("Add Blocked Link")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = inputName.getText().toString().trim();
                        String url = inputUrl.getText().toString().trim();
                        String description = inputDescription.getText().toString().trim();

                        if (!url.isEmpty()) {
                            if (name.isEmpty()) {
                                name = url; // Use URL as a default name if none is provided
                            }
                            if (linkBlocker.addBlockedLink(name, url, description)) {
                                Toast.makeText(SecureBrowserActivity.this, "Link added to block list.", Toast.LENGTH_SHORT).show();
                                updateBlockListStatus();
                            } else {
                                Toast.makeText(SecureBrowserActivity.this, "Link is already in the block list.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SecureBrowserActivity.this, "URL cannot be empty.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Custom WebViewClient to intercept and block URLs
    private class SecureWebViewClient extends WebViewClient {
        private Context context;
        private LinkBlocker blocker;

        SecureWebViewClient(Context context, LinkBlocker blocker) {
            this.context = context;
            this.blocker = blocker;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            // Check if the URL should be blocked
            if (blocker.isUrlBlocked(url)) {
                showBlockedMessage(url);
                return true; // Return true to prevent the URL from loading
            }
            return false; // Let the WebView handle the URL loading
        }
    }

    // Method to display a message when a link is blocked
    private void showBlockedMessage(String url) {
        String name = linkBlocker.getBlockedLinkName(url);
        String description = linkBlocker.getBlockedLinkDescription(url);

        new AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                .setTitle("Access Denied")
                .setMessage("This site has been blocked due to security concerns: \n\n" + url + "\n\nName: " + name + "\nDescription: " + description)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Override the back button on the device to navigate WebView history
    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBlockListStatus(); // Update the count when returning from the blocked links screen
    }
}
