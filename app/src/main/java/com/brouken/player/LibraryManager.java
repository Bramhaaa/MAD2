package com.brouken.player;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Simple library manager for handling media files in app's private storage.
 * This is a minimal implementation for managing downloaded media files.
 */
public class LibraryManager {
    
    private final File libraryDir;
    
    public LibraryManager(Context context) {
        this.libraryDir = new File(context.getFilesDir(), "media_library");
        
        // Create directory if it doesn't exist
        if (!libraryDir.exists()) {
            libraryDir.mkdirs();
        }
    }
    
    /**
     * Get the library directory
     */
    public File getLibraryDirectory() {
        return libraryDir;
    }
    
    /**
     * Add a media file from local filesystem to library (synchronous)
     * Used for managing downloaded files
     * @param sourceFilePath Path to the source file
     * @param fileName Desired filename for the library
     * @return Path to the file in the library, or null if failed
     */
    public String addMediaFile(String sourceFilePath, String fileName) {
        try {
            File sourceFile = new File(sourceFilePath);
            if (!sourceFile.exists()) {
                return null;
            }
            
            // Generate unique filename
            String uniqueFileName = generateUniqueFilename(fileName);
            File destFile = new File(libraryDir, uniqueFileName);
            
            // Copy file using file streams
            try (FileInputStream inputStream = new FileInputStream(sourceFile);
                 FileOutputStream outputStream = new FileOutputStream(destFile)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Remove a media file from the library
     */
    public boolean removeMediaFromLibrary(String filename) {
        File mediaFile = new File(libraryDir, filename);
        return mediaFile.exists() && mediaFile.delete();
    }
    
    /**
     * Generate unique filename to avoid conflicts
     */
    private String generateUniqueFilename(String originalName) {
        String name = originalName;
        int counter = 1;
        
        while (new File(libraryDir, name).exists()) {
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex > 0) {
                String nameWithoutExt = originalName.substring(0, dotIndex);
                String extension = originalName.substring(dotIndex);
                name = nameWithoutExt + "_" + counter + extension;
            } else {
                name = originalName + "_" + counter;
            }
            counter++;
        }
        
        return name;
    }
    
    /**
     * Get total storage used by library
     */
    public long getTotalStorageUsed() {
        long total = 0;
        File[] files = libraryDir.listFiles();
        if (files != null) {
            for (File file : files) {
                total += file.length();
            }
        }
        return total;
    }
    
    /**
     * Clear entire library
     */
    public void clearLibrary() {
        File[] files = libraryDir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }
}
