package com.example.secura.modulesscreen.securebrowser;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LinkBlocker {

    private static final String PREFS_NAME = "blocked_links_prefs";
    private static final String BLOCKED_LINKS_KEY = "blocked_links";
    private SharedPreferences sharedPreferences;

    // Use a HashMap to store the URL as the key and a String array for name and description.
    private HashMap<String, String[]> blockedLinks;

    public LinkBlocker(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Load the blocked links from SharedPreferences
        loadBlockedLinks();
    }

    // Load blocked links from SharedPreferences
    private void loadBlockedLinks() {
        blockedLinks = new HashMap<>();
        Set<String> blockedEntries = sharedPreferences.getStringSet(BLOCKED_LINKS_KEY, new HashSet<>());
        for (String entry : blockedEntries) {
            // Entry format: name|url|description
            String[] parts = entry.split("\\|", 3); // Use a limit of 3 to handle descriptions with '|'
            if (parts.length == 3) {
                blockedLinks.put(parts[1], new String[]{parts[0], parts[2]});
            } else if (parts.length == 2) {
                // Fallback for old entries with just url and description
                blockedLinks.put(parts[0], new String[]{"No Name", parts[1]});
            } else if (parts.length == 1) {
                // Fallback for even older entries with just url
                blockedLinks.put(parts[0], new String[]{"No Name", "No description provided."});
            }
        }
    }

    // Save blocked links to SharedPreferences
    private void saveBlockedLinks() {
        Set<String> blockedEntries = new HashSet<>();
        for (Map.Entry<String, String[]> entry : blockedLinks.entrySet()) {
            String url = entry.getKey();
            String name = entry.getValue()[0];
            String description = entry.getValue()[1];
            blockedEntries.add(name + "|" + url + "|" + description);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(BLOCKED_LINKS_KEY, blockedEntries);
        editor.apply();
    }

    // Check if a URL is in the blocked list
    public boolean isUrlBlocked(String url) {
        for (String blockedUrl : blockedLinks.keySet()) {
            if (url.contains(blockedUrl)) {
                return true;
            }
        }
        return false;
    }

    // Add a new URL to the blocked list
    public boolean addBlockedLink(String name, String url, String description) {
        // Normalize URL by removing "https://" or "http://" for consistent blocking
        String normalizedUrl = url.replace("https://", "").replace("http://", "");
        if (!isUrlBlocked(normalizedUrl)) {
            blockedLinks.put(normalizedUrl, new String[]{name, description});
            saveBlockedLinks();
            return true;
        }
        return false;
    }

    // Remove a URL from the blocked list
    public void unblockLink(String url) {
        blockedLinks.remove(url);
        saveBlockedLinks();
    }

    // Get the description for a blocked link
    public String getBlockedLinkDescription(String url) {
        for (Map.Entry<String, String[]> entry : blockedLinks.entrySet()) {
            if (url.contains(entry.getKey())) {
                return entry.getValue()[1];
            }
        }
        return "No description found.";
    }

    // Get the name for a blocked link
    public String getBlockedLinkName(String url) {
        for (Map.Entry<String, String[]> entry : blockedLinks.entrySet()) {
            if (url.contains(entry.getKey())) {
                return entry.getValue()[0];
            }
        }
        return "No Name found.";
    }

    // Get the total number of blocked links
    public int getBlockedLinksCount() {
        return blockedLinks.size();
    }

    // Get the full list of blocked links
    public Map<String, String[]> getAllBlockedLinks() {
        return blockedLinks;
    }
}