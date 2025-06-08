package com.brouken.player;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages network video links with persistent storage and metadata extraction
 */
public class NetworkLinkManager {
    private static final String TAG = "NetworkLinkManager";
    private static final String PREFS_NAME = "network_links";
    private static final String KEY_LINKS = "links_json";
    private static final int THUMBNAIL_WIDTH = 320;
    private static final int THUMBNAIL_HEIGHT = 180;
    
    private final Context context;
    private final SharedPreferences prefs;
    private final File thumbnailsDir;
    private final ExecutorService executorService;
    private List<NetworkVideoLink> links;
    
    public NetworkLinkManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.thumbnailsDir = new File(context.getCacheDir(), "video_thumbnails");
        this.executorService = Executors.newFixedThreadPool(3);
        
        // Create thumbnails directory
        if (!thumbnailsDir.exists()) {
            thumbnailsDir.mkdirs();
        }
        
        // Load existing links
        loadLinks();
    }
    
    /**
     * Add a new network video link
     */
    public NetworkVideoLink addLink(String url, String customTitle) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        
        // Check if link already exists
        for (NetworkVideoLink existingLink : links) {
            if (existingLink.getUrl().equals(url)) {
                return existingLink; // Return existing link
            }
        }
        
        NetworkVideoLink link = new NetworkVideoLink(url, customTitle);
        links.add(0, link); // Add at the beginning for recent-first ordering
        
        // Save changes
        saveLinks();
        
        // Extract metadata asynchronously
        extractMetadataAsync(link);
        
        return link;
    }
    
    /**
     * Remove a network video link
     */
    public boolean removeLink(String linkId) {
        NetworkVideoLink linkToRemove = null;
        for (NetworkVideoLink link : links) {
            if (link.getId().equals(linkId)) {
                linkToRemove = link;
                break;
            }
        }
        
        if (linkToRemove != null) {
            links.remove(linkToRemove);
            
            // Delete thumbnail if exists
            if (linkToRemove.hasThumbnail()) {
                new File(linkToRemove.getThumbnailPath()).delete();
            }
            
            saveLinks();
            return true;
        }
        
        return false;
    }
    
    /**
     * Update link title
     */
    public boolean updateLinkTitle(String linkId, String newTitle) {
        for (NetworkVideoLink link : links) {
            if (link.getId().equals(linkId)) {
                link.setTitle(newTitle);
                saveLinks();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Mark link as accessed (for usage tracking)
     */
    public void markLinkAccessed(String linkId) {
        for (NetworkVideoLink link : links) {
            if (link.getId().equals(linkId)) {
                link.incrementAccessCount();
                saveLinks();
                break;
            }
        }
    }
    
    /**
     * Get all network video links
     */
    public List<NetworkVideoLink> getAllLinks() {
        return new ArrayList<>(links);
    }
    
    /**
     * Get links sorted by different criteria
     */
    public List<NetworkVideoLink> getLinksSorted(SortOrder sortOrder) {
        List<NetworkVideoLink> sortedLinks = new ArrayList<>(links);
        
        switch (sortOrder) {
            case DATE_ADDED_DESC:
                Collections.sort(sortedLinks, (a, b) -> Long.compare(b.getDateAdded(), a.getDateAdded()));
                break;
            case DATE_ADDED_ASC:
                Collections.sort(sortedLinks, (a, b) -> Long.compare(a.getDateAdded(), b.getDateAdded()));
                break;
            case TITLE_ASC:
                Collections.sort(sortedLinks, (a, b) -> a.getDisplayTitle().compareToIgnoreCase(b.getDisplayTitle()));
                break;
            case TITLE_DESC:
                Collections.sort(sortedLinks, (a, b) -> b.getDisplayTitle().compareToIgnoreCase(a.getDisplayTitle()));
                break;
            case MOST_ACCESSED:
                Collections.sort(sortedLinks, (a, b) -> Integer.compare(b.getAccessCount(), a.getAccessCount()));
                break;
            case LAST_ACCESSED:
                Collections.sort(sortedLinks, (a, b) -> Long.compare(b.getLastAccessed(), a.getLastAccessed()));
                break;
        }
        
        return sortedLinks;
    }
    
    /**
     * Search links by title or URL
     */
    public List<NetworkVideoLink> searchLinks(String query) {
        if (TextUtils.isEmpty(query)) {
            return getAllLinks();
        }
        
        List<NetworkVideoLink> results = new ArrayList<>();
        String lowercaseQuery = query.toLowerCase();
        
        for (NetworkVideoLink link : links) {
            if (link.getDisplayTitle().toLowerCase().contains(lowercaseQuery) ||
                link.getUrl().toLowerCase().contains(lowercaseQuery) ||
                link.getDescription().toLowerCase().contains(lowercaseQuery)) {
                results.add(link);
            }
        }
        
        return results;
    }
    
    /**
     * Get link by ID
     */
    public NetworkVideoLink getLinkById(String linkId) {
        for (NetworkVideoLink link : links) {
            if (link.getId().equals(linkId)) {
                return link;
            }
        }
        return null;
    }
    
    /**
     * Clear all links
     */
    public void clearAllLinks() {
        // Delete all thumbnails
        File[] thumbnailFiles = thumbnailsDir.listFiles();
        if (thumbnailFiles != null) {
            for (File file : thumbnailFiles) {
                file.delete();
            }
        }
        
        links.clear();
        saveLinks();
    }
    
    /**
     * Get total number of links
     */
    public int getLinksCount() {
        return links.size();
    }
    
    /**
     * Extract metadata from video URL asynchronously
     */
    private void extractMetadataAsync(NetworkVideoLink link) {
        executorService.submit(() -> {
            try {
                extractVideoMetadata(link);
                generateThumbnail(link);
                saveLinks(); // Save updated metadata
            } catch (Exception e) {
                Log.e(TAG, "Error extracting metadata for: " + link.getUrl(), e);
            }
        });
    }
    
    /**
     * Extract video metadata using ExoPlayer
     */
    private void extractVideoMetadata(NetworkVideoLink link) {
        try {
            // This is a simplified metadata extraction
            // In a real implementation, you might use MediaMetadataRetriever or ExoPlayer
            
            // For now, we'll extract basic info from URL patterns
            String url = link.getUrl().toLowerCase();
            
            // Detect common video formats
            if (url.contains(".mp4")) {
                link.setFormat("MP4");
            } else if (url.contains(".mkv")) {
                link.setFormat("MKV");
            } else if (url.contains(".avi")) {
                link.setFormat("AVI");
            } else if (url.contains(".mov")) {
                link.setFormat("MOV");
            } else if (url.contains(".webm")) {
                link.setFormat("WebM");
            } else if (url.contains("m3u8")) {
                link.setFormat("HLS");
            } else if (url.contains("mpd")) {
                link.setFormat("DASH");
            } else {
                link.setFormat("Stream");
            }
            
            // Extract title from common video hosting patterns
            if (url.contains("youtube.com") || url.contains("youtu.be")) {
                // YouTube URL detected
                link.setTitle("YouTube Video");
            } else if (url.contains("vimeo.com")) {
                link.setTitle("Vimeo Video");
            } else if (url.contains("dailymotion.com")) {
                link.setTitle("Dailymotion Video");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting metadata", e);
        }
    }
    
    /**
     * Generate thumbnail for video (placeholder implementation)
     */
    private void generateThumbnail(NetworkVideoLink link) {
        try {
            // This is a placeholder implementation
            // In a real scenario, you would use MediaMetadataRetriever or ExoPlayer to extract a frame
            
            // For now, we'll create a simple colored bitmap as placeholder
            Bitmap placeholder = createPlaceholderThumbnail(link);
            
            String thumbnailFileName = "thumb_" + link.getId() + ".jpg";
            File thumbnailFile = new File(thumbnailsDir, thumbnailFileName);
            
            try (FileOutputStream out = new FileOutputStream(thumbnailFile)) {
                placeholder.compress(Bitmap.CompressFormat.JPEG, 80, out);
                link.setThumbnailPath(thumbnailFile.getAbsolutePath());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating thumbnail", e);
        }
    }
    
    /**
     * Create a placeholder thumbnail
     */
    private Bitmap createPlaceholderThumbnail(NetworkVideoLink link) {
        Bitmap bitmap = Bitmap.createBitmap(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Bitmap.Config.ARGB_8888);
        
        // Create a simple colored background based on the link's hash
        int color = 0xFF000000 | (link.getUrl().hashCode() & 0x00FFFFFF);
        bitmap.eraseColor(color);
        
        return bitmap;
    }
    
    /**
     * Load links from SharedPreferences
     */
    private void loadLinks() {
        links = new ArrayList<>();
        
        try {
            String jsonString = prefs.getString(KEY_LINKS, "[]");
            JSONArray jsonArray = new JSONArray(jsonString);
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonLink = jsonArray.getJSONObject(i);
                NetworkVideoLink link = linkFromJson(jsonLink);
                if (link != null) {
                    links.add(link);
                }
            }
            
        } catch (JSONException e) {
            Log.e(TAG, "Error loading links", e);
            links = new ArrayList<>();
        }
    }
    
    /**
     * Save links to SharedPreferences
     */
    private void saveLinks() {
        try {
            JSONArray jsonArray = new JSONArray();
            
            for (NetworkVideoLink link : links) {
                JSONObject jsonLink = linkToJson(link);
                if (jsonLink != null) {
                    jsonArray.put(jsonLink);
                }
            }
            
            prefs.edit()
                    .putString(KEY_LINKS, jsonArray.toString())
                    .apply();
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving links", e);
        }
    }
    
    /**
     * Convert NetworkVideoLink to JSON
     */
    private JSONObject linkToJson(NetworkVideoLink link) {
        try {
            JSONObject json = new JSONObject();
            json.put("id", link.getId());
            json.put("url", link.getUrl());
            json.put("title", link.getTitle());
            json.put("description", link.getDescription());
            json.put("duration", link.getDuration());
            json.put("format", link.getFormat());
            json.put("thumbnailPath", link.getThumbnailPath());
            json.put("dateAdded", link.getDateAdded());
            json.put("lastAccessed", link.getLastAccessed());
            json.put("accessCount", link.getAccessCount());
            json.put("isValidUrl", link.isValidUrl());
            
            return json;
        } catch (JSONException e) {
            Log.e(TAG, "Error converting link to JSON", e);
            return null;
        }
    }
    
    /**
     * Convert JSON to NetworkVideoLink
     */
    private NetworkVideoLink linkFromJson(JSONObject json) {
        try {
            String url = json.getString("url");
            NetworkVideoLink link = new NetworkVideoLink(url);
            
            if (json.has("id")) link.setTitle(json.optString("id", link.getId()));
            if (json.has("title")) link.setTitle(json.optString("title", link.getTitle()));
            if (json.has("description")) link.setDescription(json.optString("description"));
            if (json.has("duration")) link.setDuration(json.optLong("duration", -1));
            if (json.has("format")) link.setFormat(json.optString("format"));
            if (json.has("thumbnailPath")) link.setThumbnailPath(json.optString("thumbnailPath"));
            if (json.has("dateAdded")) link.setDateAdded(json.optLong("dateAdded", System.currentTimeMillis()));
            if (json.has("lastAccessed")) link.setLastAccessed(json.optLong("lastAccessed", System.currentTimeMillis()));
            if (json.has("accessCount")) link.setAccessCount(json.optInt("accessCount", 0));
            if (json.has("isValidUrl")) link.setValidUrl(json.optBoolean("isValidUrl", true));
            
            return link;
        } catch (JSONException e) {
            Log.e(TAG, "Error converting JSON to link", e);
            return null;
        }
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    /**
     * Update an existing network video link
     */
    public boolean updateLink(NetworkVideoLink updatedLink) {
        for (int i = 0; i < links.size(); i++) {
            NetworkVideoLink existingLink = links.get(i);
            if (existingLink.getId().equals(updatedLink.getId())) {
                links.set(i, updatedLink);
                saveLinks();
                return true;
            }
        }
        return false;
    }

    /**
     * Sort order options for links
     */
    public enum SortOrder {
        DATE_ADDED_DESC,
        DATE_ADDED_ASC,
        TITLE_ASC,
        TITLE_DESC,
        MOST_ACCESSED,
        LAST_ACCESSED
    }
}
