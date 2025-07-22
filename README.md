# Pure Play - Android Video Player

[![License](https://img.shields.io/badge/License-Open%20Source-blue.svg)]()
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)]()
[![API Level](https://img.shields.io/badge/API-21%2B-orange.svg)]()
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-red.svg)]()

A modern, feature-rich Android video player built on Google's ExoPlayer library with extensive format support and advanced playback features.

## 🎯 Overview

Pure Play is a powerful, open-source video player designed for Android devices that prioritizes performance, compatibility, and user experience. Built on the robust ExoPlayer library with FFmpeg integration, it delivers superior audio and video playback capabilities while maintaining a clean, intuitive interface.

## ✨ Key Features

### 🎬 Media Playback
- **Advanced Video Codecs**: H.263, H.264 AVC, H.265 HEVC, MPEG-4 SP, VP8, VP9, AV1
- **High-Quality Audio**: Vorbis, Opus, FLAC, ALAC, PCM/WAVE, MP1, MP2, MP3, AMR, AAC, AC-3, E-AC-3, DTS, DTS-HD, TrueHD
- **Container Support**: MP4, MOV, WebM, MKV, Ogg, MPEG-TS, MPEG-PS, FLV
- **HDR Support**: HDR10+ and Dolby Vision on compatible hardware
- **Perfect Audio Sync**: Optimized for Bluetooth earphones/speakers

### 🌐 Streaming & Connectivity
- **Streaming Protocols**: DASH, HLS, SmoothStreaming, RTSP
- **Network Playback**: HTTP/HTTPS video streaming
- **Local File Support**: Comprehensive local media library access

### 📝 Subtitle Support
- **Multiple Formats**: SRT, SSA, ASS, TTML, VTT
- **External Subtitles**: Automatic loading and manual selection
- **Customization**: Full subtitle styling and positioning control

### 🎮 User Interface & Controls
- **Gesture Controls**:
  - Horizontal swipe and double-tap for seeking
  - Vertical swipe for brightness (left) and volume (right)
  - Pinch to zoom (Android 7+)
  - Touch lock (long press)
- **Picture-in-Picture**: Full PiP support on Android 8+ (resizable on Android 11+)
- **Playback Speed Control**: Variable speed playback
- **Audio/Subtitle Track Selection**: Multi-track support
- **Video Resize Options**: Fit/Crop modes
- **Volume Boost**: Enhanced audio amplification

### 🔧 Advanced Features
- **Auto Frame Rate Matching**: Automatic display refresh rate adjustment (Android TV/boxes, Android 6+)
- **Post-Playback Actions**: Delete file or skip to next
- **File Browser Integration**: Built-in file chooser with advanced filtering
- **Multiple Decoder Support**: Device and app decoder priority options
- **Tunneled Playback**: Enhanced 4K/HDR support
- **Skip Silence**: Automatic silence detection and skipping

## 🏗️ Technical Architecture

### 📦 Project Structure

```
MAD2/
├── app/                          # Main application module
│   ├── src/
│   │   ├── main/                # Core application code
│   │   ├── latest/              # Latest Android target
│   │   ├── legacy/              # Legacy Android support
│   │   └── accrescent/          # Accrescent store variant
│   └── libs/                    # ExoPlayer AAR libraries
├── android-file-chooser/        # Custom file browser module
├── doubletapplayerview/         # Double-tap player view component
├── fastlane/                    # App store deployment automation
└── build files & configs
```

### 🛠️ Technology Stack

- **Core Framework**: Android SDK (API 21+)
- **Media Engine**: ExoPlayer 1.8.0-alpha01 with FFmpeg extension
- **Build System**: Gradle with Android Gradle Plugin 8.8.2
- **Target SDK**: Android 14 (API 35)
- **Minimum SDK**: Android 5.0 (API 21)
- **Language**: Java 8
- **UI Framework**: AndroidX + Material Design Components

### 📚 Dependencies

#### Core Media Libraries
- `androidx.media3:media3-*` - ExoPlayer components
- `androidx.media3:media3-exoplayer-*` - Streaming protocol support

#### UI & UX Libraries
- `com.google.android.material:material` - Material Design components
- `androidx.recyclerview:recyclerview` - List views
- `com.getkeepsafe.taptargetview:taptargetview` - User onboarding
- `androidx.coordinatorlayout:coordinatorlayout` - Advanced layouts

#### Utility Libraries
- `com.squareup.okhttp3:okhttp` - Network operations
- `com.sigpwned:chardet4j` - Character encoding detection

## 🚀 Installation & Setup

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 35
- NDK 21.4.7075529
- Java 8 or later

### Building from Source

1. **Clone the Repository**
   ```bash
   git clone https://github.com/Bramhaaa/MAD2.git
   cd MAD2
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned MAD2 directory

3. **Build the Project**
   ```bash
   ./gradlew assembleLatestUniversalDebug
   ```

4. **Install on Device**
   ```bash
   ./gradlew installLatestUniversalDebug
   ```

### Build Variants

The project supports multiple build variants:

#### Target SDK Flavors
- **latest**: Targets Android 14 (API 35)
- **legacy**: Targets Android 10 (API 29) for older devices

#### Distribution Flavors
- **universal**: Standard distribution
- **amazon**: Amazon Appstore optimization
- **accrescent**: Accrescent store variant

## 📱 Supported Formats

### Video Formats
| Codec | Container | Notes |
|-------|-----------|-------|
| H.264 AVC | MP4, MKV, MOV | Baseline Profile; Main Profile on Android 6+ |
| H.265 HEVC | MP4, MKV, MOV | Including custom .hevc files |
| VP8/VP9 | WebM, MKV | Google's open codecs |
| AV1 | MP4, WebM | Next-gen codec support |
| MPEG-4 SP | MP4, AVI | Legacy support |

### Audio Formats
| Codec | Quality | Notes |
|-------|---------|-------|
| FLAC, ALAC | Lossless | Audiophile quality |
| MP3, AAC | Lossy | Universal compatibility |
| AC-3, E-AC-3 | Surround | Dolby Digital support |
| DTS, DTS-HD | Hi-Res | High-resolution audio |
| TrueHD | Lossless | Dolby TrueHD support |

### Streaming Protocols
- **DASH**: Dynamic Adaptive Streaming
- **HLS**: HTTP Live Streaming
- **SmoothStreaming**: Microsoft Smooth Streaming
- **RTSP**: Real-Time Streaming Protocol

## 🐛 Recent Fixes & Improvements

### HEVC File Support Fix ✅
**Issue**: `.hevc` files weren't appearing in the file browser despite being listed as supported.

**Solution**: 
- Enhanced MIME type detection in `UiUtil.java`
- Added null-safe file type checking in `SubtitleUtils.java`
- Custom fallback for Android's missing HEVC MIME type recognition

### Audio Format Enhancements ✅
- Fixed MP3 audio-only playback
- Improved WAV file recognition
- Enhanced audio codec detection

## 🔧 Configuration

### File Access Permissions
Pure Play requires storage permissions to access local media files. The app will request these permissions on first launch.

### External Subtitle Loading
To enable automatic external subtitle loading:
1. Long press the file open button
2. Grant access to your video root folder
3. Subtitles will be automatically loaded for matching video files

### Display Settings
- **Auto Frame Rate Matching**: Automatically adjusts display refresh rate
- **HDR Support**: Enables HDR10+ and Dolby Vision (on compatible devices)
- **Tunneled Playback**: Optimizes 4K/HDR performance

## 🎯 Usage Examples

### Basic Video Playback
1. Launch Pure Play
2. Tap the open button
3. Navigate to your video file
4. Select and enjoy!

### Advanced Features
- **Gesture Seeking**: Swipe left/right to seek through video
- **Brightness Control**: Swipe up/down on left side of screen
- **Volume Control**: Swipe up/down on right side of screen
- **Speed Control**: Use playback speed controls in player UI
- **PiP Mode**: Minimize app to enter Picture-in-Picture mode

## 🔒 Privacy & Security

Pure Play is designed with privacy in mind:
- **No Ads**: Completely ad-free experience
- **No Tracking**: Zero user analytics or data collection
- **Minimal Permissions**: Only requests necessary file access permissions
- **Local Processing**: All media processing happens on-device

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

1. **Fork the Repository**
2. **Create a Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make Your Changes**
4. **Test Thoroughly**
5. **Submit a Pull Request**

### Code Style
- Follow Android development best practices
- Use meaningful variable and method names
- Add comments for complex logic
- Ensure backward compatibility

## 📝 License

This project is open source. Please check the license file for specific terms and conditions.

## 🙏 Acknowledgments

- **ExoPlayer Team**: For the robust media playback foundation
- **FFmpeg Project**: For comprehensive codec support
- **Android Community**: For continuous feedback and improvements
- **Contributors**: All developers who have contributed to this project

## 📞 Support

For bug reports, feature requests, or general questions:
- Create an issue on GitHub
- Check existing documentation
- Review recent fixes and improvements

## 🔮 Roadmap

- [ ] Enhanced subtitle customization options
- [ ] Additional streaming protocol support
- [ ] Improved TV interface
- [ ] Advanced audio equalization
- [ ] Cloud storage integration
- [ ] Playlist management features

---

**Pure Play** - *Simple, Powerful, Open Source Video Playback*
