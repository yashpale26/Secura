package com.example.secura.modulesscreen.chatbot;

public class ChatMessage {
    public static final int TYPE_CHATBOT = 0;
    public static final int TYPE_USER = 1;

    private String text;
    private int senderType;
    private long timestamp;

    public ChatMessage(String text, int senderType) {
        this.text = text;
        this.senderType = senderType;
        this.timestamp = System.currentTimeMillis();
    }

    public String getText() {
        return text;
    }

    public int getSenderType() {
        return senderType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // New method to allow updating the message text
    public void setText(String text) {
        this.text = text;
    }
}