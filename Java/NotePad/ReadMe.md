# 🟣 PurplePad

**Ultra-Secure Notes & Privacy App for Android**

PurplePad is a feature-rich, privacy-focused note-taking application with advanced security features including an incognito browser, encrypted file vault, and emergency data destruction.

---

## 🌟 Features

### 📝 Core Note-Taking Features
- **Rich Text Notes** - Create and manage unlimited notes with markdown support
- **Folders & Organization** - Organize notes into custom folders
- **Tags System** - Tag notes with keywords for easy categorization
- **Search** - Fast full-text search across all notes
- **Pin Notes** - Pin important notes to the top
- **Locked Notes** - Encrypt individual notes with biometric protection
- **Markdown Preview** - Write in markdown and preview formatted text
- **Checklists** - Create interactive to-do lists with checkboxes
- **Dark/Light Theme** - Switch between dark and light modes

### 🔒 Advanced Privacy & Security

#### 1. **Secret Vault** (4-Tap Access)
- **Encrypted File Storage** - Store photos, videos, and documents in an isolated, encrypted vault
- **Complete Isolation** - Files stored in app's internal storage, invisible to other apps and system galleries
- **Thumbnail Preview** - Auto-generated thumbnails for images and videos
- **Secure Deletion** - Files are overwritten before deletion
- **No System Access** - Vault files do not appear in system file browsers or media scanners
- **AES-256 Encryption** - Military-grade encryption for all stored files

**How to Access:** Tap anywhere on the screen **4 times** within 800ms

#### 2. **Private Browser** (3-Tap Access)
- **Tor Integration** - Route traffic through Tor network for true anonymity (requires Orbot app)
- **DNS-over-HTTPS** - Encrypted DNS queries via Cloudflare to prevent DNS leaks
- **Anti-Fingerprinting** - Advanced JavaScript injection to spoof device fingerprints
  - Canvas fingerprinting protection with noise injection
  - WebGL fingerprinting protection
  - Audio context fingerprinting protection
  - Randomized screen dimensions and specs
  - Spoofed navigator properties
  - Blocked device APIs (USB, Bluetooth, sensors, geolocation, media devices)
- **WebRTC Leak Prevention** - Blocks WebRTC to prevent IP leaks
- **Tracking Blocker** - Blocks known analytics and tracking domains
- **User-Agent Rotation** - Randomizes user agent on each session
- **No History/Cookies** - Zero persistence browsing
- **Mobile Viewport** - Proper mobile rendering
- **Enter Key Navigation** - Press Enter to navigate
- **Panic Double-Tap** - Double-tap back button to clear all data and open Reddit

**How to Access:** Tap anywhere on the screen **3 times** within 600ms

#### 3. **Emergency Data Destruction** (3-Second Hold)
- **Total App Wipe** - Complete and irreversible data destruction
- **DoD 5220.22-M Standard** - Military-grade file overwriting (3 passes)
  - Pass 1: Random data
  - Pass 2: All 1s (0xFF)
  - Pass 3: All 0s (0x00)
- **Database Corruption** - Overwrites all SQLite databases with random data
- **Cache Clearing** - Wipes all caches and temporary files
- **Preference Wiping** - Securely deletes all SharedPreferences
- **WebView Data Clearing** - Removes all browser data
- **Auto-Uninstall** - Attempts to uninstall the app after destruction

**How to Activate:** Press and hold anywhere on the main screen for **3 seconds**. A red overlay with progress bar will appear. Release to cancel.

### 📎 File Management
- **Attachments** - Attach files to notes
- **File Revision History** - Track changes and restore previous versions
- **Soft Delete** - Notes go to trash before permanent deletion
- **Auto-Purge** - Automatically purge old trash (configurable)
- **Manual Purge** - Manually empty trash with secure deletion

### 🔐 Encryption & Security
- **AES/GCM Encryption** - Strong encryption for locked notes (256-bit)
- **Master Key Storage** - Keys stored in Android's EncryptedSharedPreferences
- **Secure Database** - SQLite with `PRAGMA secure_delete=ON`
- **Legacy XOR Fallback** - Simple XOR encryption for backward compatibility
- **No Backup** - App data excluded from Android backups (android:allowBackup="false")
- **Secure File Deletion** - Random data overwrite before file deletion

### 🌐 Tor & Network Privacy
- **Orbot Integration** - Seamless integration with Orbot for Tor connectivity
- **Tor Status Monitoring** - Real-time Tor connection status
- **SOCKS Proxy Support** - Routes HTTP traffic through Tor SOCKS5 proxy (127.0.0.1:9050)
- **Tor Circuit Info** - View Tor connection state and test connectivity
- **Auto Tor Detection** - Automatically detects and uses Tor if available
- **Install Helper** - Direct link to install Orbot from Play Store

---

## 🚀 Installation

### Option 1: Build from Source

#### Prerequisites
- Android Studio (or command-line tools)
- Android SDK (API 24+)
- JDK 17
- Python 3.7+ (for build script)

#### Steps
1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/PurplePad.git
   cd PurplePad
   ```

2. **Build using Python script:**
   ```bash
   python build.py
   ```
   
   This will:
   - Download and configure Android SDK
   - Set up Gradle wrapper
   - Build the debug APK
   - Create Android Virtual Device (optional)

3. **Or build with Gradle directly:**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Find APK:**
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

### Option 2: Install Pre-built APK

1. Download the latest APK from [Releases](https://github.com/yourusername/PurplePad/releases)
2. Enable "Install from Unknown Sources" in Android settings
3. Install the APK

### Installing to Device

#### Via ADB:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

#### Manual:
1. Transfer APK to your device
2. Open file and tap "Install"
3. Grant necessary permissions

---

## 🔑 Quick Start Guide

### Basic Usage
1. **Create a Note** - Tap the `+` button
2. **Edit Note** - Tap on any note to edit
3. **Add Tags** - Enter comma-separated tags
4. **Lock Note** - Toggle the "Locked" switch (requires biometric)
5. **Search** - Use the search bar at the top
6. **Dark Mode** - Tap "Dark"/"Light" chip

### Secret Features Access

| Feature | Gesture | Timing | Description |
|---------|---------|--------|-------------|
| **Private Browser** | 3 Taps | Within 600ms | Ultra-private Tor browser |
| **Secret Vault** | 4 Taps | Within 800ms | Encrypted file storage |
| **Data Destruction** | Hold 3s | 3 seconds | Complete app wipe |
| **Panic Exit (Browser)** | Double-tap Back | Within 500ms | Clear & open Reddit |

### Tor Setup
1. Install [Orbot](https://play.google.com/store/apps/details?id=org.torproject.android) from Play Store
2. Open Orbot and start Tor
3. Open PurplePad Private Browser (3 taps)
4. Status bar will show "✓ Tor Connected" when active

---

## 🛡️ Security Features Explained

### Privacy Browser Protection Layers

1. **Network Layer**
   - Tor SOCKS5 proxy (if Orbot running)
   - DNS-over-HTTPS (Cloudflare 1.1.1.1)
   - No DNS leaks
   - Encrypted connections only

2. **Fingerprinting Protection**
   - Canvas fingerprinting → Noise injection
   - WebGL fingerprinting → Hash spoofing
   - Audio fingerprinting → Random noise
   - Screen specs → Randomized values
   - Navigator API → Spoofed values
   - Timezone → Randomized offset

3. **Tracking Prevention**
   - Google Analytics blocked
   - Facebook Pixel blocked
   - All major trackers blocked
   - WebRTC disabled
   - Geolocation denied
   - No cookies or storage

4. **Identity Rotation**
   - User-Agent changes every session
   - Device metadata randomized
   - DNS cache cleared on rotation

### Secret Vault Security

- **AES-256-CBC Encryption** - Each file encrypted with unique IV
- **Internal Storage Only** - Files stored in app's private directory
- **No Media Scanning** - `.nomedia` prevents gallery apps from indexing
- **Encrypted Thumbnails** - Preview images also encrypted
- **Secure Deletion** - Files overwritten before removal
- **No Cloud Backup** - Files never backed up to cloud

### Emergency Destruction

The 3-second hold destruction feature will:
1. Overwrite all databases with random data (3 passes)
2. Corrupt SQLite files beyond recovery
3. Overwrite all files in app directory
4. Delete all caches and preferences
5. Clear WebView data and cookies
6. Wipe secret vault files
7. Launch system uninstaller
8. Exit immediately

**⚠️ Warning:** This is irreversible! All data will be permanently lost.

---

## 📱 Permissions

| Permission | Purpose |
|------------|---------|
| `INTERNET` | Private browser and Tor connectivity |
| `REQUEST_DELETE_PACKAGES` | Self-uninstall during emergency destruction |

**Note:** App requests minimal permissions and does NOT request:
- Camera/Microphone
- Location
- Contacts
- SMS/Phone
- Storage (uses scoped storage)

---

## 🏗️ Technical Stack

- **Language:** Kotlin 1.9.22
- **UI Framework:** Jetpack Compose (Material3)
- **Database:** Room 2.6.1
- **Encryption:** Android Security Crypto Library
- **Network:** OkHttp 4.12.0 + DNS-over-HTTPS
- **Markdown:** CommonMark 0.21.0
- **Async:** Kotlin Coroutines + Flow
- **Image Loading:** Coil 2.5.0
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)

### Key Dependencies
```kotlin
// Compose BOM 2024.02.01
androidx.compose.material3
androidx.compose.ui

// Room Database
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1

// Security
androidx.security:security-crypto:1.1.0-alpha06
androidx.biometric:biometric:1.2.0-alpha05

// Network
com.squareup.okhttp3:okhttp:4.12.0
com.squareup.okhttp3:okhttp-dnsoverhttps:4.12.0

// Tor (optional - requires Orbot)
// io.matthewnelson.kotlin-components:kmp-tor:2.0.0
```

---

## 🔧 Configuration

### Build Variants
- **Debug** - Development build with logging
- **Release** - Production build (requires signing)

### Gradle Configuration
```kotlin
minSdk = 24
targetSdk = 34
compileSdk = 34

kotlinCompilerExtensionVersion = "1.5.8"
```

### Customization
Edit `app/src/main/java/com/example/notepad/ui/theme/` for:
- Colors (primary: #6A34D9 purple)
- Typography
- Shapes

---

## 🚨 Important Warnings

### ⚠️ Emergency Destruction
- **IRREVERSIBLE** - All data is permanently destroyed
- No recovery possible after activation
- Use only in genuine emergencies
- Test in a VM/emulator first

### ⚠️ Tor Privacy
- Tor provides anonymity, not encryption (use HTTPS)
- Some websites block Tor exit nodes
- Slower than direct connections
- Requires Orbot app to be running
- IP masking only works if Tor is connected

### ⚠️ Secret Vault
- Files are encrypted but key is in app storage
- If device is compromised, files can be decrypted
- Uninstalling app deletes all vault files permanently
- No cloud backup - files only on device

---

## 🐛 Troubleshooting

### Private Browser Issues

**Problem:** Google still shows my location  
**Solution:** Location is detected via IP address. Enable Tor by installing Orbot.

**Problem:** Browser shows "Tor OFF"  
**Solution:** 
1. Install Orbot from Play Store
2. Open Orbot and start Tor
3. Wait for "Connected" status
4. Reopen PurplePad browser

**Problem:** Websites are blocked  
**Solution:** Some sites block Tor. Use "Identity Rotation" (refresh button) to get new circuit.

### Secret Vault Issues

**Problem:** Can't add files to vault  
**Solution:** Grant storage permission when prompted by file picker.

**Problem:** Files not showing thumbnails  
**Solution:** Thumbnails generated on import. Large videos may take time.

**Problem:** Can't open files from vault  
**Solution:** Install appropriate app to open file type (e.g., video player for MP4).

### Build Issues

**Problem:** Build fails with SDK not found  
**Solution:** Run `python build.py` to auto-download SDK.

**Problem:** Gradle daemon fails  
**Solution:** 
```bash
./gradlew --stop
./gradlew clean build
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

---

## 🔐 Security Disclosure

If you discover a security vulnerability, please email: security@purplepad.app

**Do NOT open a public issue for security vulnerabilities.**

---

## 📞 Support

- **Issues:** [GitHub Issues](https://github.com/yourusername/PurplePad/issues)
- **Discussions:** [GitHub Discussions](https://github.com/yourusername/PurplePad/discussions)
- **Email:** support@purplepad.app

---

## 🙏 Acknowledgments

- **Tor Project** - For Tor network and Orbot
- **Cloudflare** - For DNS-over-HTTPS
- **Android Team** - For excellent privacy APIs
- **Material Design** - For beautiful UI components

---

## ⚖️ Disclaimer

This app is provided for legitimate privacy purposes. Users are responsible for complying with local laws. The developers are not responsible for misuse of this software.

**Use at your own risk. All data destruction features are irreversible.**

---

## 📊 Changelog

### v1.0.0 (Initial Release)
- ✅ Core note-taking with folders, tags, search
- ✅ Locked notes with encryption
- ✅ Private browser with Tor support
- ✅ Secret vault for encrypted files
- ✅ Emergency data destruction
- ✅ DNS-over-HTTPS
- ✅ Anti-fingerprinting
- ✅ Panic double-tap exit
- ✅ 3-tap browser, 4-tap vault access

---

**Made with 🟣 for Privacy**
