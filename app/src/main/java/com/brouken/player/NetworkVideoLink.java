package com.brouken.player;

import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;
import java.util.Objects;

/**
 * Represents a network video link with metadata for the library
 */
public class NetworkVideoLink {
    private String id;
    private String url;
    private String title;
    private String description;
    private long duration; // in milliseconds
    private String format;
    private String thumbnailPath; // Local path to cached thumbnail
    private long dateAdded;
    private long lastAccessed;
    private int accessCount;
    private boolean isValidUrl;
    
    public NetworkVideoLink(String url) {
        this.id = generateId(url);
        this.url = url;
        this.title = extractTitleFromUrl(url);
        this.description = "";
        this.duration = -1;
        this.format = "";
        this.thumbnailPath = null;
        this.dateAdded = System.currentTimeMillis();
        this.lastAccessed = this.dateAdded;
        this.accessCount = 0;
        this.isValidUrl = Utils.isSupportedNetworkUri(Uri.parse(url));
    }
    
    public NetworkVideoLink(String url, String title) {
        this(url);
        if (!TextUtils.isEmpty(title)) {
            this.title = title;
        }
    }
    
    // Generate a unique ID based on the URL
    private String generateId(String url) {
        return "link_" + Math.abs(url.hashCode()) + "_" + System.currentTimeMillis();
    }
    
    // Extract a basic title from URL
    private String extractTitleFromUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            String path = uri.getPath();
            if (path != null && !path.isEmpty()) {
                String filename = path.substring(path.lastIndexOf('/') + 1);
                if (!filename.isEmpty()) {
                    // Remove common video file extensions for display
                    return filename.replaceAll("\\.(mp4|mkv|avi|mov|m4v|3gp|webm|flv)$", "");
                }
            }
            
            String host = uri.getHost();
            if (host != null) {
                return "Video from " + host;
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        
        return "Network Video";
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
        this.isValidUrl = Utils.isSupportedNetworkUri(Uri.parse(url));
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public void setDuration(long duration) {
        this.duration = duration;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public String getThumbnailPath() {
        return thumbnailPath;
    }
    
    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }
    
    public boolean hasThumbnail() {
        return thumbnailPath != null && new File(thumbnailPath).exists();
    }
    
    public long getDateAdded() {
        return dateAdded;
    }
    
    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }
    
    public long getLastAccessed() {
        return lastAccessed;
    }
    
    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }
    
    public int getAccessCount() {
        return accessCount;
    }
    
    public void setAccessCount(int accessCount) {
        this.accessCount = accessCount;
    }
    
    public void incrementAccessCount() {
        this.accessCount++;
        this.lastAccessed = System.currentTimeMillis();
    }
    
    public boolean isValidUrl() {
        return isValidUrl;
    }
    
    public void setValidUrl(boolean validUrl) {
        isValidUrl = validUrl;
    }
    
    // Display helpers
    public String getDisplayTitle() {
        return TextUtils.isEmpty(title) ? "Network Video" : title;
    }
    
    public String getDurationString() {
        if (duration <= 0) {
            return "Unknown";
        }
        
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60);
        } else {
            return String.format("%d:%02d", minutes, seconds % 60);
        }
    }
    
    public String getHostname() {
        try {
            Uri uri = Uri.parse(url);
            return uri.getHost();
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkVideoLink that = (NetworkVideoLink) o;
        return Objects.equals(id, that.id) || Objects.equals(url, that.url);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, url);
    }
    
    @Override
    public String toString() {
        return "NetworkVideoLink{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", duration=" + duration +
                ", format='" + format + '\'' +
                '}';
    }
}
