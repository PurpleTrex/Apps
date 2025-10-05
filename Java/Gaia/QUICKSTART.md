# Gaia - Quick Start Guide

## 🚀 Getting Started in 5 Minutes

This guide will help you get the Gaia app running on your development environment quickly.

---

## Prerequisites

### Required Software
- **Android Studio**: Arctic Fox (2020.3.1) or later
  - Download: https://developer.android.com/studio
- **Java JDK**: Version 17 or later
  - Download: https://adoptium.net/
- **Android SDK**: API Level 24 or higher
  - Installed via Android Studio

### Recommended
- **Physical Android Device**: For GPS testing
- **2GB+ free disk space**: For map tiles and build cache

---

## Step 1: Clone or Open Project

### If you already have the repository:
```bash
cd /path/to/Apps/Java/Gaia
```

### If starting fresh:
The project is located at: `Java/Gaia/` in the Apps repository.

---

## Step 2: Open in Android Studio

1. Launch Android Studio
2. Click "Open an Existing Project"
3. Navigate to the `Java/Gaia` folder
4. Click "OK"

Android Studio will:
- Sync Gradle files
- Download dependencies
- Index the project

**First sync may take 2-5 minutes** depending on your internet speed.

---

## Step 3: Sync Gradle

If Gradle doesn't sync automatically:

1. Click "File" → "Sync Project with Gradle Files"
2. Wait for sync to complete
3. Check the "Build" panel for any errors

### Common Issues:
- **SDK not found**: Set Android SDK location in File → Project Structure
- **Gradle version**: Project uses Gradle 8.3.0 (auto-downloaded)
- **Network issues**: Ensure internet connection for first build

---

## Step 4: Configure Device

### Option A: Physical Device (Recommended for GPS)

1. Enable Developer Options on your phone:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Return to Settings → Developer Options

2. Enable USB Debugging:
   - In Developer Options, enable "USB Debugging"

3. Connect device via USB:
   - Connect phone to computer
   - Allow USB debugging when prompted
   - Device should appear in Android Studio toolbar

### Option B: Emulator (For UI testing)

1. Click "Device Manager" in Android Studio
2. Click "Create Device"
3. Select a device (e.g., Pixel 5)
4. Select system image: API 34 (Android 14)
5. Click "Finish"
6. Start the emulator

**Note**: Emulator GPS requires manual location input and won't use real GPS.

---

## Step 5: Build and Run

### Build the App

1. Click "Build" → "Make Project" (or Ctrl+F9 / Cmd+F9)
2. Wait for build to complete (first build: 1-3 minutes)
3. Check "Build" panel for success message

### Run on Device

1. Select your device from dropdown (top toolbar)
2. Click "Run" button (green play icon) or press Shift+F10
3. App will install and launch automatically

### Expected Output

You should see:
1. Permission request for location access
2. Map view of San Francisco
3. Search bar at top
4. Privacy notice at bottom
5. Floating action buttons for location and draw route

---

## Step 6: Grant Permissions

When the app launches for the first time:

1. **Location Permission**: Tap "Grant Permissions" or "Allow"
   - Required for GPS navigation
   - Choose "Allow while using the app"

2. **Notification Permission** (Android 13+):
   - Optional, but recommended for navigation alerts
   - Choose "Allow"

---

## Step 7: Explore Features

### Test the Map
- Pan by dragging
- Zoom with pinch gesture
- Tap "My Location" FAB to center on current location

### Download Maps
1. Tap download icon (top-right)
2. Search for a city (e.g., "San Francisco")
3. Tap to download (simulated, 3 seconds)
4. Check mark appears when complete

### Configure Settings
1. Tap menu icon (⋮) → Settings
2. Try different route types
3. Toggle Sensitive Mode
4. Toggle Distraction-Free Mode

### Test Navigation
1. Return to home screen
2. Tap menu icon (⋮) → (Future: Start Navigation)
3. Or use the navigation screen demo

**Note**: Full navigation with routing will require additional routing engine integration in future development.

---

## 🐛 Troubleshooting

### Build Fails

**Error**: "SDK location not found"
- **Solution**: File → Project Structure → SDK Location → Set Android SDK path

**Error**: "Gradle sync failed"
- **Solution**: File → Invalidate Caches → Invalidate and Restart

**Error**: "Dependencies not found"
- **Solution**: Check internet connection, retry Gradle sync

### App Crashes

**Error**: "Permission denied"
- **Solution**: Grant location permissions in app or device settings

**Error**: "Map not loading"
- **Solution**: 
  1. Check internet connection (for initial tile download)
  2. Ensure sufficient storage space
  3. Check logcat for specific errors

### GPS Not Working

**On Emulator**:
- **Solution**: Use Extended Controls (⋯) → Location to set GPS position

**On Physical Device**:
- **Solution**: 
  1. Ensure Location Services enabled in device settings
  2. Try outdoor testing for better GPS signal
  3. Check app has location permission

### Map Tiles Not Loading

**Error**: Blank map or gray tiles
- **Solution**:
  1. Check internet connection (required for first download)
  2. Wait a few seconds for tiles to load
  3. Clear app data and retry
  4. Check OSMDroid cache directory exists

---

## 📱 Testing Different Features

### Test Location Services
```kotlin
// In MainActivity, tap "My Location" FAB
// Expected: Map centers on current GPS position
```

### Test Settings Persistence
```kotlin
// 1. Go to Settings
// 2. Change route type to "Shortest"
// 3. Close app completely
// 4. Reopen app → Settings
// Expected: "Shortest" still selected
```

### Test Distraction-Free Mode
```kotlin
// 1. Open NavigationActivity (future)
// 2. Toggle Distraction-Free switch
// Expected: DND mode activates, notifications blocked
```

---

## 🔧 Development Tips

### Enable Debug Logging
In `GaiaApplication.kt`, you can add:
```kotlin
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())
}
```

### View Logs
- Open "Logcat" panel in Android Studio
- Filter by package: `com.gaia.maps`
- Look for errors or debug messages

### Modify UI
- All UI is in Compose files (`.kt`)
- Changes reflect immediately with Compose Preview
- Use `@Preview` annotations for rapid development

### Test on Multiple Devices
- Use "Run on multiple devices" in toolbar
- Test on different Android versions
- Verify Material 3 theming adapts

---

## 📚 Next Steps

### For Users
1. Download maps for your city
2. Configure navigation preferences
3. Test the app in your area
4. Provide feedback

### For Developers
1. Review code structure
2. Read TECHNICAL_REQUIREMENTS.md
3. Plan feature additions
4. Contribute improvements

### Suggested First Contribution
- Implement draw route touch handling
- Add incident reporting dialog
- Create Room database entities
- Implement actual map downloads via OSM APIs

---

## 📖 Additional Resources

### Documentation
- **README.md**: User-facing documentation
- **TECHNICAL_REQUIREMENTS.md**: Technical specifications
- **IMPLEMENTATION_SUMMARY.md**: What's been built

### Android Development
- **Jetpack Compose**: https://developer.android.com/jetpack/compose
- **Material Design 3**: https://m3.material.io/
- **OSMDroid**: https://github.com/osmdroid/osmdroid

### Libraries
- **GraphHopper**: https://www.graphhopper.com/
- **OpenStreetMap**: https://www.openstreetmap.org/

---

## ✅ Checklist

Before you start development, verify:

- [ ] Android Studio installed and updated
- [ ] JDK 17 installed
- [ ] Project synced successfully
- [ ] No Gradle errors
- [ ] Device/emulator configured
- [ ] App builds without errors
- [ ] App runs on device
- [ ] Location permission granted
- [ ] Map displays correctly
- [ ] Can navigate between screens

**If all checked**: You're ready to develop! 🎉

---

## 🆘 Getting Help

### Common Commands

**Clean build**:
```bash
./gradlew clean
./gradlew assembleDebug
```

**Check dependencies**:
```bash
./gradlew dependencies
```

**Run tests** (when added):
```bash
./gradlew test
```

### Where to Ask

- **Issues**: Open an issue in the repository
- **Questions**: Check TECHNICAL_REQUIREMENTS.md
- **Bugs**: Include logcat output and steps to reproduce

---

**Happy Coding!** 🚀

The Gaia team welcomes your contributions to making privacy-first offline navigation a reality.
