package com.example.secura.modulesscreen.privacyprotectors.galleryvault;

import android.net.Uri;

public class MediaItem {
    private long id;
    private String displayName;
    private String path;
    private Uri uri;
    private String mimeType;
    private boolean isSelected;

    public MediaItem(long id, String displayName, String path, Uri uri, String mimeType) {
        this.id = id;
        this.displayName = displayName;
        this.path = path;
        this.uri = uri;
        this.mimeType = mimeType;
        this.isSelected = false; // Default to not selected
    }

    public long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPath() {
        return path;
    }

    public Uri getUri() {
        return uri;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}