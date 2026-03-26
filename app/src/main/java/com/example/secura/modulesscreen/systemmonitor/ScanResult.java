package com.example.secura.modulesscreen.systemmonitor;

import android.graphics.drawable.Drawable;

public class ScanResult {
    private String title;
    private String reason;
    private Drawable icon;

    public ScanResult(String title, String reason, Drawable icon) {
        this.title = title;
        this.reason = reason;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public String getReason() {
        return reason;
    }

    public Drawable getIcon() {
        return icon;
    }
}