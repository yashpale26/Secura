package com.example.secura.modulesscreen.privacyprotectors.galleryvault;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secura.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GallerySelectionActivity extends AppCompatActivity implements GalleryMediaAdapter.OnMediaItemClickListener {

    private static final String TAG = "GallerySelectionAct";
    private RecyclerView galleryRecyclerView;
    private GalleryMediaAdapter adapter;
    private ArrayList<MediaItem> mediaList;
    private Set<String> selectedMediaPaths;
    private Button hideSelectedMediaButton;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_selection);

        Toolbar toolbar = findViewById(R.id.gallerySelectionToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        galleryRecyclerView = findViewById(R.id.galleryRecyclerView);
        hideSelectedMediaButton = findViewById(R.id.hideSelectedMediaButton);
        galleryRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mediaList = new ArrayList<>();
        selectedMediaPaths = new HashSet<>();
        executorService = Executors.newSingleThreadExecutor();
        adapter = new GalleryMediaAdapter(this, mediaList, this);
        galleryRecyclerView.setAdapter(adapter);
        hideSelectedMediaButton.setOnClickListener(v -> hideSelectedMedia());
        loadMedia();
    }

    private void loadMedia() {
        executorService.execute(() -> {
            ArrayList<MediaItem> fetchedMedia = new ArrayList<>();
            Uri collectionImage = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projectionImage = new String[]{
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.MIME_TYPE
            };
            String sortOrderImage = MediaStore.Images.Media.DATE_ADDED + " DESC";
            try (Cursor cursor = getContentResolver().query(collectionImage, projectionImage, null, null, sortOrderImage)) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                int mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    String path = cursor.getString(dataColumn);
                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    String mimeType = cursor.getString(mimeTypeColumn);
                    if (path == null) {
                        path = PathUtil.getPath(this, contentUri);
                    }
                    if (path != null) {
                        fetchedMedia.add(new MediaItem(id, name, path, contentUri, mimeType));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error querying images: " + e.getMessage(), e);
            }
            Uri collectionVideo = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            String[] projectionVideo = new String[]{
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.MIME_TYPE
            };
            String sortOrderVideo = MediaStore.Video.Media.DATE_ADDED + " DESC";
            try (Cursor cursor = getContentResolver().query(collectionVideo, projectionVideo, null, null, sortOrderVideo)) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                int mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    String path = cursor.getString(dataColumn);
                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                    String mimeType = cursor.getString(mimeTypeColumn);
                    if (path == null) {
                        path = PathUtil.getPath(this, contentUri);
                    }
                    if (path != null) {
                        fetchedMedia.add(new MediaItem(id, name, path, contentUri, mimeType));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error querying videos: " + e.getMessage(), e);
            }
            runOnUiThread(() -> {
                mediaList.clear();
                mediaList.addAll(fetchedMedia);
                adapter.notifyDataSetChanged();
                if (mediaList.isEmpty()) {
                    Toast.makeText(this, "No photos or videos found on device.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    public void onMediaItemClick(MediaItem item) {
        item.setSelected(!item.isSelected());
        if (item.isSelected()) {
            selectedMediaPaths.add(item.getPath());
        } else {
            selectedMediaPaths.remove(item.getPath());
        }
        adapter.notifyDataSetChanged();
    }

    private void hideSelectedMedia() {
        if (selectedMediaPaths.isEmpty()) {
            Toast.makeText(this, "Please select at least one media item to hide.", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<String> pathsToHide = new ArrayList<>(selectedMediaPaths);
        Intent resultIntent = new Intent();
        resultIntent.putStringArrayListExtra("selected_media_paths", pathsToHide);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}