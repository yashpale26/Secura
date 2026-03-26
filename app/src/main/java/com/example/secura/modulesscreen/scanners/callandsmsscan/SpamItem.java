package com.example.secura.modulesscreen.scanners.callandsmsscan;

public class SpamItem {
    private String type;
    private String sender;
    private String message;
    private long date;
    private long duration;
    private long id; // Added ID field for unique identification

    // Constructor for Call items
    public SpamItem(String type, String sender, long date, long duration, long id) {
        this.type = type;
        this.sender = sender;
        this.date = date;
        this.duration = duration;
        this.id = id;
        this.message = null; // Call items don't have a message
    }

    // Constructor for SMS items
    public SpamItem(String type, String sender, String message, long date, long id) {
        this.type = type;
        this.sender = sender;
        this.message = message;
        this.date = date;
        this.id = id;
        this.duration = 0; // SMS items don't have a duration
    }

    // Getters
    public String getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public long getDate() {
        return date;
    }

    public long getDuration() {
        return duration;
    }

    public long getId() {
        return id;
    }
}