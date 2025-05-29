#!/bin/bash

echo "=== FINAL VERIFICATION: HEVC and WAV File Recognition Fixes ==="
echo "Date: $(date)"
echo

# Function to check if app is installed
check_app_installed() {
    if adb shell pm list packages | grep -q "com.brouken.player"; then
        echo "✓ Just Player app is installed"
        return 0
    else
        echo "✗ Just Player app is not installed"
        return 1
    fi
}

# Function to check test files
check_test_files() {
    echo "Checking test files on device:"
    files=$(adb shell ls /sdcard/Download/ 2>/dev/null | grep -E "\.(hevc|wav)$" | wc -l)
    if [ "$files" -gt 0 ]; then
        echo "✓ Found $files HEVC/WAV test files:"
        adb shell ls -la /sdcard/Download/ | grep -E "\.(hevc|wav)$"
        return 0
    else
        echo "✗ No HEVC/WAV test files found"
        return 1
    fi
}

# Function to check for NullPointerExceptions
check_for_crashes() {
    echo "Checking app logs for NullPointerExceptions..."
    adb logcat -c  # Clear logs
    adb shell am force-stop com.brouken.player
    adb shell am start -n com.brouken.player/.PlayerActivity > /dev/null 2>&1
    sleep 5
    
    crashes=$(adb logcat -d | grep -c "NullPointerException.*getMimeTypeFromExtension\|NullPointerException.*startsWith")
    if [ "$crashes" -eq 0 ]; then
        echo "✓ No MIME type related NullPointerExceptions found"
        return 0
    else
        echo "✗ Found $crashes MIME type related crashes"
        adb logcat -d | grep -A 5 -B 5 "NullPointerException.*getMimeTypeFromExtension\|NullPointerException.*startsWith"
        return 1
    fi
}

# Function to verify code fixes are in place
verify_code_fixes() {
    echo "Verifying code fixes are in place..."
    
    # Check UiUtil.java HEVC fix
    if grep -q "video/hevc" "/Users/bramhabajannavar/Desktop/mad/Player/android-file-chooser/src/main/java/com/obsez/android/lib/filechooser/internals/UiUtil.java"; then
        echo "✓ UiUtil.java HEVC MIME type fix is present"
    else
        echo "✗ UiUtil.java HEVC MIME type fix is missing"
    fi
    
    # Check SubtitleUtils.java null-safe fix
    if grep -q "mimeType != null && mimeType.startsWith" "/Users/bramhabajannavar/Desktop/mad/Player/app/src/main/java/com/brouken/player/SubtitleUtils.java"; then
        echo "✓ SubtitleUtils.java null-safe MIME type check is present"
    else
        echo "✗ SubtitleUtils.java null-safe MIME type check is missing"
    fi
    
    # Check extension fallback
    if grep -q "fileName != null" "/Users/bramhabajannavar/Desktop/mad/Player/app/src/main/java/com/brouken/player/SubtitleUtils.java"; then
        echo "✓ SubtitleUtils.java extension-based fallback is present"
    else
        echo "✗ SubtitleUtils.java extension-based fallback is missing"
    fi
    
    # Check supported extensions
    if grep -q "hevc.*wav\|wav.*hevc" "/Users/bramhabajannavar/Desktop/mad/Player/app/src/main/java/com/brouken/player/Utils.java"; then
        echo "✓ Both HEVC and WAV extensions are in supportedExtensionsVideo"
    else
        echo "✗ HEVC or WAV extensions may be missing from supportedExtensionsVideo"
    fi
}

# Function to check APK build
check_apk_build() {
    apk_path="/Users/bramhabajannavar/Desktop/mad/Player/app/build/outputs/apk/latest/debug/Just.Player.v0.193-latest-universal-debug.apk"
    if [ -f "$apk_path" ]; then
        echo "✓ APK with fixes is available: $(basename "$apk_path")"
        echo "  Size: $(ls -lh "$apk_path" | awk '{print $5}')"
        return 0
    else
        echo "✗ APK with fixes not found"
        return 1
    fi
}

# Run all checks
echo "1. APP INSTALLATION CHECK"
check_app_installed
echo

echo "2. TEST FILES CHECK"
check_test_files  
echo

echo "3. CODE FIXES VERIFICATION"
verify_code_fixes
echo

echo "4. APK BUILD CHECK"
check_apk_build
echo

echo "5. CRASH CHECK"
check_for_crashes
echo

echo "=== SUMMARY ==="
echo "The fixes implemented:"
echo "1. Enhanced UiUtil.getMimeType() to handle HEVC files with custom MIME type"
echo "2. Made SubtitleUtils.isVideoFile() null-safe with extension-based fallback"
echo "3. Ensured both 'hevc' and 'wav' are in supportedExtensionsVideo array"
echo
echo "Expected behavior:"
echo "- HEVC files (.hevc) should now be visible in the file browser"
echo "- WAV files (.wav) should work again (regression fixed)"
echo "- No NullPointerExceptions when browsing files"
echo
echo "MANUAL TESTING REQUIRED:"
echo "1. Open Just Player app"
echo "2. Tap to browse for files"
echo "3. Navigate to Download folder"
echo "4. Verify HEVC and WAV files are visible and selectable"
echo "5. Try opening each file type to ensure no crashes"
echo
echo "Verification completed at: $(date)"
