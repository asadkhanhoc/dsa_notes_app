package com.asadullah.brainwave.model;


public class Note {
    public String title;
    public String content;
    public String url;
    public Note next;  // Reference to the next node in the linked list
    public String imagePath;

    public Note(String title, String content, String url, String imagePath) {
        this.title = title;
        this.content = content;
        this.url = url != null ? url.trim() : "";
        this.imagePath = imagePath;
        this.next = null;
    }

    public boolean hasUrl() {
        return url != null && !url.isEmpty();
    }


    public boolean hasImage() {
        return imagePath != null && !imagePath.isEmpty();
    }

    public String getFormattedUrl() {
        if (!hasUrl()) return "";
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }
}
