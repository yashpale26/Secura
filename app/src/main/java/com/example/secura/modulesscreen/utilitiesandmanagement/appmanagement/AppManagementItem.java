package com.example.secura.modulesscreen.utilitiesandmanagement.appmanagement;

public class AppManagementItem {
    private String title;
    private String description; // Optional: A short description for each feature

    public AppManagementItem(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}