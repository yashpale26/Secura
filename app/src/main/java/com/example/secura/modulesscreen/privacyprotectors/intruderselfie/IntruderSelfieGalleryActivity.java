package com.example.secura.modulesscreen.privacyprotectors.intruderselfie;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secura.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IntruderSelfieGalleryActivity extends AppCompatActivity {

    private static final String TAG = "SelfieGalleryActivity";
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<SelfieItem> selfieItems;
    private Button deleteSelectedButton;
    private ExecutorService deleteExecutor;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intruder_selfie_gallery);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbarIntruderSelfieGallery);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        recyclerView = findViewById(R.id.selfieRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        selfieItems = new ArrayList<>();
        imageAdapter = new ImageAdapter(this, selfieItems);
        recyclerView.setAdapter(imageAdapter);

        deleteSelectedButton = findViewById(R.id.deleteSelectedButton);
        deleteSelectedButton.setOnClickListener(v -> deleteSelectedSelfies());

        deleteExecutor = Executors.newSingleThreadExecutor();

        loadIntruderSelfies();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadIntruderSelfies() {
        selfieItems.clear();

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.RELATIVE_PATH,
                MediaStore.Images.Media.DATE_ADDED
        };
        String selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
        String[] selectionArgs = new String[]{"%ZSecurityIntruderSelfies%"};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = getContentResolver().query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder)) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    long dateAdded = cursor.getLong(dateAddedColumn) * 1000;
                    Uri contentUri = ContentUris.withAppendedId(collection, id);
                    selfieItems.add(new SelfieItem(contentUri.toString(), dateAdded));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading selfies: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to load selfies.", Toast.LENGTH_SHORT).show();
        }

        if (selfieItems.isEmpty()) {
            Toast.makeText(this, "No intruder selfies found.", Toast.LENGTH_SHORT).show();
        }
        imageAdapter.notifyDataSetChanged();
    }

    private void deleteSelectedSelfies() {
        List<SelfieItem> selectedToDelete = new ArrayList<>(imageAdapter.getSelectedSelfies());
        if (selectedToDelete.isEmpty()) {
            Toast.makeText(this, "No selfies selected for deletion.", Toast.LENGTH_SHORT).show();
            return;
        }

        // This is a placeholder for a custom dialog
        // You should implement a custom dialog here instead of an alert
        // A simple custom dialog implementation is provided as a comment below.

        deleteExecutor.execute(() -> {
            int deletedCount = 0;
            for (SelfieItem selfie : selectedToDelete) {
                try {
                    Uri contentUri = Uri.parse(selfie.getImagePath());
                    int rowsAffected = getContentResolver().delete(contentUri, null, null);
                    if (rowsAffected > 0) {
                        deletedCount++;
                    } else {
                        Log.w(TAG, "Failed to delete URI: " + contentUri);
                    }
                } catch (SecurityException e) {
                    Log.e(TAG, "Permission denied for deleting selfie: " + selfie.getImagePath() + ". Error: " + e.getMessage());
                    runOnUiThread(() -> Toast.makeText(this, "Permission denied to delete some photos.", Toast.LENGTH_SHORT).show());
                } catch (Exception e) {
                    Log.e(TAG, "Error deleting selfie: " + selfie.getImagePath() + ". Error: " + e.getMessage());
                }
            }
            final int finalDeletedCount = deletedCount;
            runOnUiThread(() -> {
                if (finalDeletedCount > 0) {
                    Toast.makeText(this, finalDeletedCount + " selfie(s) deleted.", Toast.LENGTH_SHORT).show();
                    imageAdapter.clearSelectedSelfies();
                    loadIntruderSelfies();
                } else {
                    Toast.makeText(this, "No selfies were deleted.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteExecutor.shutdown();
    }
}
