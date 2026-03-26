package com.example.secura.modulesscreen.privacyprotectors.galleryvault;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.secura.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VaultActivity extends AppCompatActivity {

    private static final String TAG = "VaultActivity";
    private static final int PERMISSION_REQUEST_CODE = 2;
    private static final int MANAGE_EXTERNAL_STORAGE_PERMISSION_REQUEST = 3;
    private static final int SELECT_MEDIA_FOR_VAULT = 4;

    private Button selectMediaButton;
    private Button viewHiddenMediaButton;

    public static final String HIDDEN_FOLDER_NAME = ".ZSecurityVault";
    private ExecutorService executorService;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault);

        Toolbar toolbar = findViewById(R.id.vaultToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        selectMediaButton = findViewById(R.id.selectMediaButton);
        viewHiddenMediaButton = findViewById(R.id.viewHiddenMediaButton);

        executorService = Executors.newSingleThreadExecutor();

        selectMediaButton.setOnClickListener(v -> checkPermissionsAndOpenGallerySelection());
        viewHiddenMediaButton.setOnClickListener(v -> {
            Intent intent = new Intent(VaultActivity.this, HiddenMediaActivity.class);
            startActivity(intent);
        });
    }

    private void checkPermissionsAndOpenGallerySelection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                openGallerySelection();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                Toast.makeText(this, "Please grant 'All files access' for the app to hide/unhide media.", Toast.LENGTH_LONG).show();
                startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_PERMISSION_REQUEST);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallerySelection();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void openGallerySelection() {
        Intent intent = new Intent(VaultActivity.this, GallerySelectionActivity.class);
        startActivityForResult(intent, SELECT_MEDIA_FOR_VAULT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallerySelection();
            } else {
                Toast.makeText(this, "Storage permission denied. Cannot select media.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_MEDIA_FOR_VAULT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> selectedPaths = data.getStringArrayListExtra("selected_media_paths");
            if (selectedPaths != null && !selectedPaths.isEmpty()) {
                for (String path : selectedPaths) {
                    hideMedia(path);
                }
                Toast.makeText(this, selectedPaths.size() + " media items hidden!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No media selected.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == MANAGE_EXTERNAL_STORAGE_PERMISSION_REQUEST) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    openGallerySelection();
                } else {
                    Toast.makeText(this, "Manage external storage permission denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void hideMedia(String sourcePath) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Attempting to hide file: " + sourcePath);

                File sourceFile = new File(sourcePath);
                if (!sourceFile.exists()) {
                    runOnUiThread(() -> Toast.makeText(this, "Source file not found: " + sourceFile.getAbsolutePath(), Toast.LENGTH_LONG).show());
                    Log.e(TAG, "Source file does not exist: " + sourceFile.getAbsolutePath());
                    return;
                }

                File hiddenDir = getHiddenDirectory();
                if (!hiddenDir.exists()) {
                    hiddenDir.mkdirs();
                }

                File destinationFile = new File(hiddenDir, sourceFile.getName());

                boolean moved = sourceFile.renameTo(destinationFile);
                if (!moved) {
                    // If renameTo fails, try copying and then deleting the original
                    copyFile(sourceFile, destinationFile);
                    boolean deleted = sourceFile.delete();
                    Log.d(TAG, "Original file deleted after copy: " + deleted);
                }
                Log.d(TAG, "File moved to hidden directory: " + destinationFile.getAbsolutePath());
                // Invalidate gallery for original file
                MediaScannerConnection.scanFile(this,
                        new String[]{sourceFile.getAbsolutePath()},
                        null,
                        (path, uri) -> Log.d(TAG, "Scanned " + path + " (removed from gallery)"));

            } catch (Exception e) {
                Log.e(TAG, "Failed to hide media: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "Failed to hide media: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private File getHiddenDirectory() {
        return new File(getExternalFilesDir(null), HIDDEN_FOLDER_NAME);
    }

    private void copyFile(File source, File destination) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(destination);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing input stream", e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing output stream", e);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}