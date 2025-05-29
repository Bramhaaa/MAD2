# HEVC Playback Fix - Testing Guide

## Summary of Changes Made

### 1. UiUtil.getMimeType() Enhancement

**File**: `android-file-chooser/src/main/java/com/obsez/android/lib/filechooser/internals/UiUtil.java`

**Problem**: Android's `MimeTypeMap` doesn't recognize `.hevc` file extension, returning `null` for MIME type.

**Solution**: Added custom HEVC MIME type handling:

```java
// Handle HEVC files specifically since Android doesn't recognize .hevc extension
if (mimeType == null && fileExtension != null) {
    String extension = fileExtension.toLowerCase();
    if ("hevc".equals(extension)) {
        mimeType = "video/hevc";
    }
}
```

### 2. SubtitleUtils.isVideoFile() Robust Detection

**File**: `app/src/main/java/com/brouken/player/SubtitleUtils.java`

**Problem**: Method threw `NullPointerException` when `file.getType()` returned `null` for HEVC files.

**Solution**: Added null-safe fallback to extension-based detection:

```java
public static boolean isVideoFile(DocumentFile file) {
    if (!file.isFile()) {
        return false;
    }

    String mimeType = file.getType();

    // First check if we have a valid MIME type and it starts with "video/"
    if (mimeType != null && mimeType.startsWith("video/")) {
        return true;
    }

    // If MIME type is null or doesn't start with "video/", check by file extension
    // This handles cases like .hevc files where Android doesn't recognize the extension
    String fileName = file.getName();
    if (fileName != null) {
        String lowercaseName = fileName.toLowerCase();
        for (String extension : Utils.supportedExtensionsVideo) {
            if (lowercaseName.endsWith("." + extension)) {
                return true;
            }
        }
    }

    return false;
}
```

## Testing Instructions

### Verification Steps:

1. **Build Status**: ✅ PASSED - Project builds successfully with no compilation errors

2. **Code Review**: ✅ PASSED - Both fix implementations are correctly in place:

   - UiUtil.getMimeType() has HEVC fallback logic
   - SubtitleUtils.isVideoFile() has null-safe extension checking

3. **HEVC Support Confirmation**: ✅ CONFIRMED - Utils.java contains "hevc" in supportedExtensionsVideo array

### Manual Testing Required:

1. **Install APK**: Install the built debug APK on an Android device
2. **Prepare HEVC Test File**: Get a sample `.hevc` video file
3. **File Browse Test**:
   - Open the app's file browser
   - Navigate to the HEVC file
   - Verify the file appears in the video file list (no more filtering out)
4. **Playback Test**:
   - Tap on the HEVC file
   - Verify the player opens and attempts to play the file
   - Check that no crashes occur during file selection

### Expected Behavior:

**Before Fix**:

- HEVC files were filtered out of file lists
- Clicking HEVC files could cause crashes due to null MIME type
- isVideoFile() returned false for HEVC files

**After Fix**:

- HEVC files appear in video file browsers
- HEVC files are properly recognized as video files
- No null pointer exceptions when handling HEVC files
- Proper MIME type "video/hevc" is assigned to .hevc files

### Technical Details:

The root cause was Android's limitation where the system `MimeTypeMap` doesn't include .hevc extension mapping. This caused a cascade of issues:

1. `MimeTypeMap.getMimeTypeFromExtension("hevc")` returned `null`
2. `DocumentFile.getType()` returned `null` for HEVC files
3. `mimeType.startsWith("video/")` threw `NullPointerException`
4. Files were incorrectly classified as non-video files

Our fix provides two layers of protection:

1. **MIME Type Level**: Custom HEVC MIME type assignment in UiUtil
2. **File Detection Level**: Extension-based fallback in SubtitleUtils

This ensures HEVC files work throughout the entire app pipeline from file browsing to playback.
