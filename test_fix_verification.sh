#!/bin/bash

echo "=== HEVC and WAV File Recognition Test ==="
echo "Testing fixes for HEVC file visibility and WAV regression"
echo

# Clear logcat to start fresh
adb logcat -c

echo "1. Launching Just Player app..."
adb shell am start -n com.brouken.player/.PlayerActivity
sleep 3

echo "2. Testing file extension recognition in app..."

# Test HEVC file recognition
echo "Testing HEVC file recognition..."
adb shell am start -a android.intent.action.VIEW -d "file:///sdcard/Download/test_video.hevc" -t "video/*" com.brouken.player

sleep 2

# Test WAV file recognition  
echo "Testing WAV file recognition..."
adb shell am start -a android.intent.action.VIEW -d "file:///sdcard/Download/test_audio.wav" -t "audio/*" com.brouken.player

sleep 2

echo "3. Checking app logs for any crashes or errors..."
adb logcat -d | grep -E "(brouken|player|HEVC|hevc|wav|WAV|NullPointer|Exception)" | tail -20

echo
echo "4. Testing file browser access to Download folder..."
# Simulate opening file browser to Downloads
adb shell am start -a android.intent.action.GET_CONTENT -t "*/*" --grant-read-uri-permission

echo
echo "=== Test Summary ==="
echo "- HEVC files should now be visible in file browser"
echo "- WAV files should be working again"
echo "- No NullPointerExceptions should occur"
echo "- Check the logs above for any issues"
echo
echo "Manual verification needed:"
echo "1. Open the app and browse to Download folder"
echo "2. Verify both test_video.hevc and test_audio.wav are visible"
echo "3. Try selecting each file to ensure no crashes"
