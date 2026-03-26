package com.example.secura.modulesscreen.privacyprotectors.galleryvault;

import android.annotation.SuppressLint;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secura.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HiddenMediaActivity extends AppCompatActivity implements HiddenMediaAdapter.OnMediaActionListener {

    private static final String TAG = "HiddenMediaActivity";
    private RecyclerView hiddenMediaRecyclerView;
    private HiddenMediaAdapter adapter;
    private List<File> hiddenMediaList;

    private static final String HIDDEN_FOLDER_NAME = ".ZSecurityVault";
    private ExecutorService executorService;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hidden_media);

        Toolbar toolbar = findViewById(R.id.hiddenMediaToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        hiddenMediaRecyclerView = findViewById(R.id.hiddenMediaRecyclerView);
        hiddenMediaRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        executorService = Executors.newSingleThreadExecutor();

        loadHiddenMedia();
        adapter = new HiddenMediaAdapter(this, hiddenMediaList, this);
        hiddenMediaRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHiddenMedia();
        adapter.updateData(hiddenMediaList);
    }

    private void loadHiddenMedia() {
        hiddenMediaList = new ArrayList<>();
        File hiddenDir = getHiddenDirectory();
        Log.d(TAG, "Loading from hidden directory: " + hiddenDir.getAbsolutePath());
        if (hiddenDir.exists() && hiddenDir.isDirectory()) {
            File[] files = hiddenDir.listFiles();
            if (files != null) {
                Log.d(TAG, "Found " + files.length + " files in hidden directory.");
                for (File file : files) {
                    Log.d(TAG, "Hidden file: " + file.getName() + ", exists: " + file.exists());
                }
                hiddenMediaList.addAll(Arrays.asList(files));
            } else {
                Log.d(TAG, "Hidden directory listFiles() returned null.");
            }
        } else {
            Log.d(TAG, "Hidden directory does not exist or is not a directory.");
        }
        if (hiddenMediaList.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(this, "No hidden media found.", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onUnhideClick(File mediaFile) {
        unhideMedia(mediaFile);
    }

    private void unhideMedia(File mediaFile) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Attempting to unhide: " + mediaFile.getAbsolutePath());
                File publicDir;
                String mimeType = getMimeType(mediaFile.getName());
                if (mimeType != null && mimeType.startsWith("image")) {
                    publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                } else if (mimeType != null && mimeType.startsWith("video")) {
                    publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                } else {
                    publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                }

                if (!publicDir.exists()) {
                    publicDir.mkdirs();
                }

                File destinationFile = new File(publicDir, mediaFile.getName());
                boolean moved = mediaFile.renameTo(destinationFile);
                if (!moved) {
                    copyFile(mediaFile, destinationFile);
                    boolean deleted = mediaFile.delete();
                    Log.d(TAG, "Original hidden file deleted after copy: " + deleted);
                }
                Log.d(TAG, "File unhidden to: " + destinationFile.getAbsolutePath());
                MediaScannerConnection.scanFile(this,
                        new String[]{destinationFile.getAbsolutePath()},
                        null,
                        (path, uri) -> Log.d(TAG, "Scanned " + path + " (added to gallery)"));
                runOnUiThread(() -> {
                    Toast.makeText(this, "Media unhidden successfully!", Toast.LENGTH_SHORT).show();
                    loadHiddenMedia();
                    adapter.updateData(hiddenMediaList);
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to unhide media: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "Failed to unhide media: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private String getMimeType(String fileName) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
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