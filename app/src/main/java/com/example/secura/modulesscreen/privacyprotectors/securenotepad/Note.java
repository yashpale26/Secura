package com.example.secura.modulesscreen.privacyprotectors.securenotepad;

/**
 * A simple model class representing a Secure Notepad note.
 */
public class Note {
    private long id;
    private String title;
    private String content;
    private long timestamp;

    public Note(long id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = System.currentTimeMillis(); // Automatically set timestamp on creation
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setters (if you need to modify title/content after creation)
    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }
}