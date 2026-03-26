package com.example.secura.homescreen;

public class CardItem {
    private int imageResId; // Placeholder for image, currently 0
    private String title;
    private String description;
    private Class<?> targetActivity; // To hold the class of the target activity

    public CardItem(int imageResId, String title, String description, Class<?> targetActivity) {
        this.imageResId = imageResId;
        this.title = title;
        this.description = description;
        this.targetActivity = targetActivity;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Class<?> getTargetActivity() {
        return targetActivity;
    }
}