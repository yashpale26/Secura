package com.example.secura.modulesscreen.privacyprotectors.intruderselfie;

public class SelfieItem {
    private final String imagePath;
    private final long timestamp; // Store as long (milliseconds)
    private boolean isSelected;

    public SelfieItem(String imagePath, long timestamp) {
        this.imagePath = imagePath;
        this.timestamp = timestamp;
        this.isSelected = false; // Default to not selected
    }

    public String getImagePath() {
        return imagePath;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}