#!/bin/bash

# HEVC Fix Verification Script for Just Player

echo "======================================"
echo "HEVC Fix Verification for Just Player"
echo "======================================"
echo ""

# Check if APK was built
APK_PATH="./app/build/outputs/apk/latestUniversal/debug/Just.Player.v0.193-latest-universal-debug.apk"

if [ -f "$APK_PATH" ]; then
    echo "✅ Debug APK found: $APK_PATH"
    APK_SIZE=$(ls -lh "$APK_PATH" | awk '{print $5}')
    echo "   Size: $APK_SIZE"
    echo ""
else
    echo "❌ Debug APK not found at expected path"
    echo "   Expected: $APK_PATH"
    echo ""
fi

# Check for Android devices
echo "📱 Checking for connected Android devices..."
if command -v adb >/dev/null 2>&1; then
    DEVICES=$(adb devices | grep -v "List of devices" | grep "device" | wc -l)
    if [ "$DEVICES" -gt 0 ]; then
        echo "✅ Found $DEVICES connected Android device(s)"
        adb devices
        echo ""
        
        # Offer to install APK
        if [ -f "$APK_PATH" ]; then
            read -p "Install APK to connected device? (y/n): " -n 1 -r
            echo ""
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                echo "📲 Installing APK..."
                adb install -r "$APK_PATH"
                echo ""
            fi
        fi
    else
        echo "⚠️  No Android devices found"
        echo "   Connect an Android device and enable USB debugging"
        echo ""
    fi
else
    echo "⚠️  ADB not found in PATH"
    echo "   Install Android SDK platform-tools to use ADB"
    echo ""
fi

# Check if we modified the key files
echo "🔍 Verifying code changes..."

# Check UiUtil.java changes
UIUTIL_FILE="./android-file-chooser/src/main/java/com/obsez/android/lib/filechooser/internals/UiUtil.java"
if [ -f "$UIUTIL_FILE" ]; then
    if grep -q "video/hevc" "$UIUTIL_FILE"; then
        echo "✅ UiUtil.java: HEVC MIME type fallback implemented"
    else
        echo "❌ UiUtil.java: HEVC MIME type fallback NOT found"
    fi
else
    echo "❌ UiUtil.java: File not found"
fi

# Check SubtitleUtils.java changes
SUBTITLE_FILE="./app/src/main/java/com/brouken/player/SubtitleUtils.java"
if [ -f "$SUBTITLE_FILE" ]; then
    if grep -q "supportedExtensionsVideo" "$SUBTITLE_FILE"; then
        echo "✅ SubtitleUtils.java: Extension-based fallback implemented"
    else
        echo "❌ SubtitleUtils.java: Extension-based fallback NOT found"
    fi
else
    echo "❌ SubtitleUtils.java: File not found"
fi

# Check if HEVC is in supported extensions
UTILS_FILE="./app/src/main/java/com/brouken/player/Utils.java"
if [ -f "$UTILS_FILE" ]; then
    if grep -A 20 "supportedExtensionsVideo" "$UTILS_FILE" | grep -q "hevc"; then
        echo "✅ Utils.java: 'hevc' found in supportedExtensionsVideo array"
    else
        echo "❌ Utils.java: 'hevc' NOT found in supportedExtensionsVideo array"
    fi
else
    echo "❌ Utils.java: File not found"
fi

echo ""
echo "📋 Manual Testing Steps:"
echo "1. Install the APK on your Android device"
echo "2. Get a sample .hevc video file"
echo "3. Place it on your device storage"
echo "4. Open Just Player and browse files"
echo "5. Verify .hevc files are now visible and selectable"
echo "6. Test playback functionality"
echo ""
echo "🎯 Expected Results:"
echo "- HEVC files appear in file browser (no longer filtered out)"
echo "- No crashes when selecting HEVC files"
echo "- Proper MIME type handling for HEVC files"
echo ""
