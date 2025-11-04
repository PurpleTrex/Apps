# Zelda NES Emulator for Android

A dedicated Android app for running The Legend of Zelda NES ROM with automatic launch and touch controls.

## Features

- ✨ **Auto-Launch**: Opens directly into gameplay, no menus or UI
- 🎮 **Touch Controls**: Full NES controller on screen with haptic feedback
- 💾 **Auto-Save States**: Automatically saves and restores your progress
- ⚡ **Fast Forward**: Speed up gameplay when needed
- 🎯 **60 FPS**: Smooth gameplay targeting native NES framerate
- 📱 **Landscape Mode**: Locked landscape orientation for optimal experience
- 🔊 **Vibration Feedback**: Haptic response for button presses
- 🌙 **Immersive Mode**: Full-screen gameplay with hidden system UI

## Screenshots

Touch controls include:
- **D-Pad** (bottom-left): Directional movement
- **A & B buttons** (bottom-right): Action buttons in classic NES layout
- **Select & Start** (bottom-center): Menu navigation

## Requirements

- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK with API 26+ (Android 8.0+)
- Android device or emulator with API 26+
- Your NES ROM file: `zelda.nes`

## Project Structure

```
ZeldaEmulator/
├── app/
│   ├── src/main/
│   │   ├── java/com/personal/zeldaemulator/
│   │   │   ├── MainActivity.kt           # Main activity with auto-launch
│   │   │   ├── EmulatorView.kt          # Emulator core with save states
│   │   │   └── TouchControlsOverlay.kt  # Touch controls with haptics
│   │   ├── cpp/
│   │   │   ├── emulator_jni.cpp         # JNI bridge to native emulator
│   │   │   └── CMakeLists.txt           # Native build configuration
│   │   ├── res/
│   │   │   └── layout/
│   │   │       └── activity_main.xml    # Main layout
│   │   ├── assets/
│   │   │   └── zelda.nes               # ROM file (add your own)
│   │   ├── jniLibs/                    # Native libraries (optional)
│   │   │   ├── arm64-v8a/
│   │   │   ├── armeabi-v7a/
│   │   │   ├── x86/
│   │   │   └── x86_64/
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## Setup Instructions

### 1. Clone or Open Project

Open this project in Android Studio:
```bash
cd Android/ZeldaEmulator
# Open with Android Studio
```

### 2. Add Your ROM

**IMPORTANT**: You must provide your own legally obtained ROM file.

1. Place your `zelda.nes` ROM file in:
   ```
   app/src/main/assets/zelda.nes
   ```

2. If the assets folder doesn't exist, create it:
   ```bash
   mkdir -p app/src/main/assets
   ```

### 3. (Optional) Add LibRetro Core

The current implementation includes a mock emulator for demonstration. To use a real NES emulator:

1. Download FCEUmm core from LibRetro:
   ```
   https://buildbot.libretro.com/nightly/android/latest/
   ```

2. Extract and place cores in:
   ```
   app/src/main/jniLibs/arm64-v8a/libfceumm.so
   app/src/main/jniLibs/armeabi-v7a/libfceumm.so
   app/src/main/jniLibs/x86/libfceumm.so
   app/src/main/jniLibs/x86_64/libfceumm.so
   ```

3. Update `CMakeLists.txt` to link the core (uncomment the lines at the bottom)

### 4. Build and Run

#### Option A: Using Android Studio

1. Open project in Android Studio
2. Wait for Gradle sync to complete
3. Connect Android device or start emulator
4. Click **Run** (▶️) button
5. Select your device
6. App will launch directly into the game

#### Option B: Using Gradle Command Line

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Build and install
./gradlew installDebug
```

The APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 5. Build Release APK

For a release build:

1. Generate a signing key:
   ```bash
   keytool -genkey -v -keystore zelda-release.jks \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias zelda
   ```

2. Update `app/build.gradle` with signing config:
   ```gradle
   android {
       signingConfigs {
           release {
               storeFile file('zelda-release.jks')
               storePassword 'your_password'
               keyAlias 'zelda'
               keyPassword 'your_password'
           }
       }
       buildTypes {
           release {
               signingConfig signingConfigs.release
               ...
           }
       }
   }
   ```

3. Build release APK:
   ```bash
   ./gradlew assembleRelease
   ```

## Controls

### Touch Controls

- **D-Pad (Blue)**: Move Link
  - Supports 4-way directional input
  - Dead zone in center prevents accidental presses
  - Slide finger to change directions smoothly

- **A Button (Red)**: Primary action
  - Attack with sword
  - Confirm menu selections

- **B Button (Yellow)**: Secondary action
  - Use selected item
  - Cancel menu selections

- **Start Button**: Pause/Inventory
- **Select Button**: Switch equipment

### Haptic Feedback

Each button press provides subtle vibration feedback. This can be disabled by modifying `TouchControlsOverlay.kt`:

```kotlin
var hapticFeedbackEnabled = false
```

## Features Explained

### Auto-Save States

The app automatically saves your game state when:
- App is paused (home button, switching apps)
- App is closed

State is automatically restored when you reopen the app, so you continue exactly where you left off.

Save file location:
```
/data/data/com.personal.zeldaemulator/files/zelda_autosave.state
```

### Fast Forward

To enable fast forward mode, modify `EmulatorView.kt`:

```kotlin
// In MainActivity or add a gesture
emulatorView.fastForwardEnabled = true
```

This doubles the emulation speed (120 FPS target).

### Performance Optimization

The emulator targets 60 FPS with these optimizations:
- Hardware-accelerated rendering
- Efficient frame timing with sleep compensation
- Aspect-ratio-preserving scaling
- RGB565 to ARGB8888 conversion in native code

### Debug Logging

View emulator logs:
```bash
adb logcat | grep -E "NESEmulator|EmulatorView"
```

## Customization

### Adjust Control Opacity

In `TouchControlsOverlay.kt`:
```kotlin
var controlsAlpha = 128  // Range: 0 (invisible) to 255 (opaque)
```

### Reposition Controls

Modify button layout in `TouchControlsOverlay.onSizeChanged()`:
```kotlin
val padding = 100f  // Increase to move controls inward
val buttonSize = 150f  // Make buttons larger
```

### Change Button Colors

In `TouchControlsOverlay.onDraw()`:
```kotlin
// A button color
paint.color = Color.argb(controlsAlpha, 255, 0, 0)  // RGB values

// D-pad color
paint.color = Color.argb(controlsAlpha, 0, 0, 255)
```

### Disable Vibration

In `TouchControlsOverlay.kt`:
```kotlin
var hapticFeedbackEnabled = false
```

Or remove the `VIBRATE` permission from `AndroidManifest.xml`.

## Troubleshooting

### Black Screen

**Cause**: ROM file not found or failed to load

**Solution**:
1. Check that `zelda.nes` exists in `app/src/main/assets/`
2. Check logcat: `adb logcat | grep NESEmulator`
3. Verify ROM is valid NES format

### Controls Not Responding

**Cause**: Touch events not registering

**Solution**:
1. Check that TouchControlsOverlay is in the layout
2. Verify `setInputListener()` is called in MainActivity
3. Enable debug logging in TouchControlsOverlay

### Performance Issues

**Cause**: Running on low-end device or emulator

**Solution**:
1. Test on physical device instead of emulator
2. Close other apps to free memory
3. Reduce control opacity to improve rendering
4. Check FPS in logcat: `adb logcat | grep FPS`

### Build Errors

**Cause**: NDK not installed or Gradle sync issues

**Solution**:
1. Install NDK via Android Studio SDK Manager
2. File → Invalidate Caches → Restart
3. Clean and rebuild: `./gradlew clean build`

### App Crashes on Launch

**Cause**: Native library loading failure

**Solution**:
1. Check ABI filters match your device
2. Rebuild native libraries: `./gradlew clean assembleDebug`
3. Check logcat for UnsatisfiedLinkError

## Architecture

### Native Layer (C++)

`emulator_jni.cpp` provides:
- JNI bridge between Java and emulator core
- Frame buffer management (256x240 pixels)
- Input state handling (8 buttons)
- Save/load state functionality
- Mock emulator (replace with LibRetro core for real emulation)

### Kotlin Layer

**MainActivity.kt**:
- Immersive fullscreen setup
- ROM loading from assets
- Auto-save/load on lifecycle events
- Screen wake lock

**EmulatorView.kt**:
- Surface rendering with SurfaceView
- Emulation thread (60 FPS)
- Aspect ratio preservation
- FPS tracking and frame timing
- Native method declarations

**TouchControlsOverlay.kt**:
- Multi-touch handling
- D-pad with dead zone
- Button press visualization
- Haptic feedback
- Customizable opacity

## Advanced Features

### Add Settings Screen

Create a settings activity to allow users to:
- Toggle haptic feedback
- Adjust control opacity
- Remap buttons
- Enable/disable fast forward

### Add Multiple Save Slots

Modify save state logic to support multiple slots:
```kotlin
fun saveState(slot: Int) {
    val file = File(filesDir, "zelda_slot_$slot.state")
    emulatorView.saveState(file)
}
```

### Add Audio Support

Implement audio in native layer using AudioTrack:
```kotlin
private val audioTrack = AudioTrack(
    AudioManager.STREAM_MUSIC,
    44100,  // Sample rate
    AudioFormat.CHANNEL_OUT_STEREO,
    AudioFormat.ENCODING_PCM_16BIT,
    bufferSize,
    AudioTrack.MODE_STREAM
)
```

## Legal Notice

⚠️ **IMPORTANT**: This emulator is for **personal use only**.

- NES ROMs are copyrighted material
- You must own the original game cartridge to legally use the ROM
- Distribution of ROM files is illegal
- This app will not pass Google Play Store review with bundled ROM
- Use at your own risk and responsibility

## Contributing

This is a personal project for educational purposes. If you want to extend it:

1. Fork the repository
2. Add your features
3. Test thoroughly on multiple devices
4. Submit pull request with detailed description

## Resources

- **LibRetro Documentation**: https://docs.libretro.com/
- **Android NDK Guide**: https://developer.android.com/ndk
- **NES Dev Wiki**: https://www.nesdev.org/
- **FCEUmm Core**: https://github.com/libretro/libretro-fceumm

## License

This project is provided as-is for educational purposes. The emulator core (if using LibRetro) is subject to its own license (GPL). ROMs are copyrighted by their respective owners.

---

## Quick Start Checklist

- [ ] Android Studio installed with SDK 26+
- [ ] Project opened in Android Studio
- [ ] ROM file (`zelda.nes`) placed in `app/src/main/assets/`
- [ ] Gradle sync completed successfully
- [ ] Android device connected or emulator running
- [ ] App built and running
- [ ] Touch controls visible and responsive
- [ ] Game saves and restores on pause/resume

**Enjoy playing The Legend of Zelda on your Android device!** 🎮
