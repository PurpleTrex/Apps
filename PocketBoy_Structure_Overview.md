# PocketBoy Repository Structure Overview

## Root Directory Organization
```
/home/user/Apps/PocketBoy/
├── CMakeLists.txt              # Main CMake build configuration
├── src/                          # All source code
├── externals/                    # External dependencies
├── dist/                         # Distribution resources (Qt themes, icons)
├── CMakeModules/                 # CMake modules
├── .github/                      # GitHub workflows
├── hooks/                        # Git hooks
└── tools/                        # Build tools
```

## 1. MAIN SOURCE CODE ORGANIZATION - src/

### 1.1 Android App Code (`src/android/app/`)
**Location:** `/home/user/Apps/PocketBoy/src/android/app/`

#### Directory Structure:
```
src/android/app/
├── src/main/
│   ├── java/org/citra/citra_emu/
│   │   ├── CitraApplication.kt           # App entry point, notifications setup
│   │   ├── NativeLibrary.kt              # JNI bindings to C++ emulation core
│   │   ├── activities/                   # Android Activities
│   │   │   └── EmulationActivity.kt      # Game emulation screen
│   │   ├── ui/
│   │   │   └── main/
│   │   │       └── MainActivity.kt       # Main home screen UI
│   │   ├── features/                     # Feature modules
│   │   │   ├── cheats/                   # CHEAT SYSTEM
│   │   │   │   ├── model/
│   │   │   │   │   ├── Cheat.kt
│   │   │   │   │   ├── CheatEngine.kt
│   │   │   │   │   └── CheatsViewModel.kt
│   │   │   │   └── ui/
│   │   │   │       ├── CheatsActivity.kt
│   │   │   │       ├── CheatsFragment.kt
│   │   │   │       ├── CheatListFragment.kt
│   │   │   │       ├── CheatDetailsFragment.kt
│   │   │   │       └── CheatsAdapter.kt
│   │   │   ├── hotkeys/                  # HOTKEY SYSTEM
│   │   │   │   ├── Hotkey.kt
│   │   │   │   └── HotkeyUtility.kt
│   │   │   └── settings/                 # SETTINGS & PREFERENCES
│   │   │       ├── model/
│   │   │       │   ├── AbstractSetting.kt
│   │   │       │   ├── BooleanSetting.kt
│   │   │       │   ├── IntSetting.kt
│   │   │       │   ├── FloatSetting.kt
│   │   │       │   ├── StringSetting.kt
│   │   │       │   ├── SettingSection.kt
│   │   │       │   ├── Settings.kt       # Settings data model (contains button keys)
│   │   │       │   ├── SettingsViewModel.kt
│   │   │       │   └── view/             # Settings UI view models
│   │   │       │       ├── HeaderSetting.kt
│   │   │       │       ├── InputBindingSetting.kt  # BUTTON CUSTOMIZATION
│   │   │       │       ├── SingleChoiceSetting.kt
│   │   │       │       ├── SwitchSetting.kt
│   │   │       │       ├── SliderSetting.kt
│   │   │       │       └── ... (other setting types)
│   │   │       ├── ui/                   # Settings UI implementation
│   │   │       │   ├── SettingsActivity.kt
│   │   │       │   ├── SettingsFragment.kt
│   │   │       │   ├── SettingsAdapter.kt
│   │   │       │   ├── SettingsFragmentPresenter.kt
│   │   │       │   └── viewholder/       # ViewHolders for different setting types
│   │   │       │       ├── InputBindingSettingViewHolder.kt
│   │   │       │       ├── SingleChoiceViewHolder.kt
│   │   │       │       └── ... (other viewholders)
│   │   │       └── utils/
│   │   │           └── SettingsFile.kt   # INI file read/write for settings
│   │   ├── fragments/                    # UI Fragments
│   │   ├── adapters/                     # RecyclerView Adapters
│   │   ├── model/                        # Data models
│   │   ├── display/                      # Screen layout & display
│   │   ├── utils/                        # Utility classes
│   │   ├── overlay/                      # On-screen overlay
│   │   └── viewmodel/                    # ViewModels
│   ├── res/                              # Android Resources
│   │   ├── values/
│   │   │   ├── strings.xml               # App strings (app_name: "Azahar")
│   │   │   ├── styles.xml                # Style definitions
│   │   │   ├── themes.xml                # THEME DEFINITIONS
│   │   │   ├── citra_colors.xml          # COLOR PALETTE (Blue, Red, Green, Orange, Purple)
│   │   │   ├── arrays.xml
│   │   │   └── dimens.xml
│   │   ├── drawable/                     # Vector drawables (62 files)
│   │   │   ├── ic_citra.xml              # APP ICON
│   │   │   ├── ic_citra_full.xml
│   │   │   ├── ic_citra_monochrome.xml
│   │   │   ├── button_home.xml           # Button graphics
│   │   │   └── ... (many other icons)
│   │   ├── drawable-hdpi/,
│   │   ├── drawable-xhdpi/,              # High-DPI bitmap resources
│   │   ├── drawable-xxhdpi/,
│   │   ├── drawable-xxxhdpi/
│   │   ├── mipmap-anydpi-v26/            # ADAPTIVE LAUNCHER ICON
│   │   │   └── ic_launcher.xml           # Adaptive icon definition
│   │   ├── layout/                       # Fragment/Activity layouts
│   │   │   ├── activity_main.xml
│   │   │   ├── activity_emulation.xml
│   │   │   ├── activity_settings.xml
│   │   │   ├── fragment_*.xml
│   │   │   └── ... (26+ layout files)
│   │   ├── menu/                         # Menu definitions
│   │   ├── navigation/                   # Navigation graph (Jetpack Navigation)
│   │   │   ├── home_navigation.xml
│   │   │   ├── emulation_navigation.xml
│   │   │   └── cheats_navigation.xml
│   │   ├── anim/                         # Fragment animations
│   │   └── values-*/ (multiple language variants)
│   │       └── strings.xml               # Localized strings
│   └── AndroidManifest.xml               # App manifest with permissions, activities
├── build.gradle.kts                      # BUILD CONFIGURATION
└── proguard-rules.pro                    # ProGuard obfuscation rules
```

### 1.2 Core Emulation Code (`src/`)
**Location:** `/home/user/Apps/PocketBoy/src/`

This contains the C++ emulation engine:
```
src/
├── core/                        # Core 3DS emulation
│   ├── cheats/                  # CHEAT ENGINE (C++ backend)
│   │   ├── cheat_base.h/cpp
│   │   ├── cheats.h/cpp
│   │   └── gateway_cheat.h/cpp  # Gateway format cheat codes
│   ├── hle/                     # High-Level Emulation of OS
│   ├── file_sys/                # Filesystem handling
│   ├── loader/                  # ROM loader
│   └── frontend/                # Frontend interface definitions
├── video_core/                  # GPU emulation
│   ├── renderer_opengl/
│   ├── renderer_vulkan/
│   ├── renderer_software/
│   └── ...
├── audio_core/                  # Audio emulation
├── input_common/                # Input handling
└── common/                      # Common utilities
```

---

## 2. UI CODE LOCATION

### 2.1 Main UI Components
- **Home Screen:** `/home/user/Apps/PocketBoy/src/android/app/src/main/java/org/citra/citra_emu/ui/main/MainActivity.kt`
- **Emulation Screen:** `/home/user/Apps/PocketBoy/src/android/app/src/main/java/org/citra/citra_emu/activities/EmulationActivity.kt`
- **Settings Screen:** `/home/user/Apps/PocketBoy/src/android/app/src/main/java/org/citra/citra_emu/features/settings/ui/SettingsActivity.kt`
- **Cheats Screen:** `/home/user/Apps/PocketBoy/src/android/app/src/main/java/org/citra/citra_emu/features/cheats/ui/CheatsActivity.kt`

### 2.2 Fragment-Based Navigation
- Uses Jetpack Navigation for fragment-based UI
- Navigation graphs defined in: `/home/user/Apps/PocketBoy/src/android/app/src/main/res/navigation/`
- Layout files: `/home/user/Apps/PocketBoy/src/android/app/src/main/res/layout/`

### 2.3 Themes & Styling
- **Theme definitions:** `/home/user/Apps/PocketBoy/src/android/app/src/main/res/values/themes.xml`
  - Multiple color themes: Blue, Red, Green, Orange, Purple
  - Night mode variants: `/home/user/Apps/PocketBoy/src/android/app/src/main/res/values-night/themes.xml`
- **Color palette:** `/home/user/Apps/PocketBoy/src/android/app/src/main/res/values/citra_colors.xml`
- **Style definitions:** `/home/user/Apps/PocketBoy/src/android/app/src/main/res/values/styles.xml`

---

## 3. CONFIGURATION FILES

### 3.1 Build Configuration
- **Gradle Build:** `/home/user/Apps/PocketBoy/src/android/app/build.gradle.kts`
  - Application ID: `io.github.lime3ds.android`
  - Min SDK: 29, Target SDK: 35
  - Version code auto-calculated from timestamp
  - Build types: debug, release, relWithDebInfo
  - CMake configuration for native code

### 3.2 App Manifest
- **Path:** `/home/user/Apps/PocketBoy/src/android/app/src/main/AndroidManifest.xml`
- **Key elements:**
  - App name: Azahar
  - Main launcher: `org.citra.citra_emu.ui.main.MainActivity`
  - Activities:
    - EmulationActivity (game emulation)
    - SettingsActivity
    - CheatsActivity
  - Permissions: INTERNET, CAMERA, RECORD_AUDIO, POST_NOTIFICATIONS

### 3.3 CMake Configuration
- **Root CMakeLists.txt:** `/home/user/Apps/PocketBoy/CMakeLists.txt` (20KB)
- **Android CMake config:** Passed via gradle.kts with flags like:
  - `-DENABLE_QT=0` (no Qt)
  - `-DENABLE_SDL2=0` (no SDL)
  - `-DANDROID_ARM_NEON=true` (NEON support)

---

## 4. SETTINGS & PREFERENCES HANDLING

### 4.1 Settings Architecture (MVP Pattern)
```
Settings.kt (Model)
    ↓
SettingsFile.kt (File I/O)
    ↓
SettingsFragmentPresenter.kt (Presenter)
    ↓
SettingsFragment.kt (View)
```

### 4.2 Settings Structure
- **File:** `/home/user/Apps/PocketBoy/src/android/app/src/main/java/org/citra/citra_emu/features/settings/model/Settings.kt`
- **Settings sections:** (defined as constants)
  - SECTION_CORE
  - SECTION_SYSTEM
  - SECTION_CAMERA
  - SECTION_CONTROLS (button customization)
  - SECTION_RENDERER
  - SECTION_LAYOUT
  - SECTION_AUDIO
  - SECTION_DEBUG
  - SECTION_THEME
  - SECTION_CUSTOM_LANDSCAPE
  - SECTION_CUSTOM_PORTRAIT
  - SECTION_PERFORMANCE_OVERLAY
  - SECTION_STORAGE

### 4.3 Settings File Handling
- **Class:** `SettingsFile.kt`
- **Format:** INI files (uses ini4j library)
- **Storage Location:** User directory (DocumentFile-based for Android scoped storage)
- **File name:** "config" (config.ini)
- **Functions:**
  - `readFile()` - Read INI files
  - `saveFile()` - Save INI files
  - `readCustomGameSettings()` - Per-game settings
  - `getSettingsFile()` - Get DocumentFile reference

### 4.4 Button Customization Keys
Defined in `Settings.kt`:
```kotlin
KEY_BUTTON_A, KEY_BUTTON_B, KEY_BUTTON_X, KEY_BUTTON_Y
KEY_BUTTON_SELECT, KEY_BUTTON_START, KEY_BUTTON_HOME
KEY_BUTTON_UP, KEY_BUTTON_DOWN, KEY_BUTTON_LEFT, KEY_BUTTON_RIGHT
KEY_BUTTON_L, KEY_BUTTON_R, KEY_BUTTON_ZL, KEY_BUTTON_ZR
KEY_CIRCLEPAD_AXIS_VERTICAL, KEY_CIRCLEPAD_AXIS_HORIZONTAL
KEY_CSTICK_AXIS_VERTICAL, KEY_CSTICK_AXIS_HORIZONTAL
KEY_DPAD_AXIS_VERTICAL, KEY_DPAD_AXIS_HORIZONTAL
```

---

## 5. CHEATS, ACHIEVEMENTS & BUTTON CUSTOMIZATION

### 5.1 Cheats System
**Location:** `/home/user/Apps/PocketBoy/src/android/app/src/main/java/org/citra/citra_emu/features/cheats/`

**Components:**
- **Model Layer:**
  - `Cheat.kt` - Cheat data class
  - `CheatEngine.kt` - Cheat execution engine
  - `CheatsViewModel.kt` - ViewModel for state management

- **UI Layer:**
  - `CheatsActivity.kt` - Main cheats screen
  - `CheatsFragment.kt` - Fragment container
  - `CheatListFragment.kt` - List of cheats for a game
  - `CheatDetailsFragment.kt` - Edit/create cheat details
  - `CheatsAdapter.kt` - RecyclerView adapter

**C++ Backend:**
- Location: `/home/user/Apps/PocketBoy/src/core/cheats/`
- Files:
  - `cheat_base.h/cpp` - Base cheat class
  - `cheats.h/cpp` - Cheats system
  - `gateway_cheat.h/cpp` - Gateway code format support

**Navigation:**
- Cheats navigation graph: `/home/user/Apps/PocketBoy/src/android/app/src/main/res/navigation/cheats_navigation.xml`
- Accessible from emulation screen menu

### 5.2 Hotkeys System
**Location:** `/home/user/Apps/PocketBoy/src/android/app/src/main/java/org/citra/citra_emu/features/hotkeys/`

**Files:**
- `Hotkey.kt` - Hotkey data class
- `HotkeyUtility.kt` - Hotkey utilities

**Hotkey Constants (in Settings.kt):**
- HOTKEY_SCREEN_SWAP
- HOTKEY_CYCLE_LAYOUT
- HOTKEY_CLOSE_GAME
- HOTKEY_PAUSE_OR_RESUME
- HOTKEY_QUICKSAVE / HOTKEY_QUICKlOAD (note: typo in original)
- HOTKEY_TURBO_LIMIT

### 5.3 Button Customization
**Implementation:**
- Setting type: `InputBindingSetting.kt`
- ViewHolder: `InputBindingSettingViewHolder.kt`
- Stored in SECTION_CONTROLS
- Settings UI automatically generates input binding fields

**Accessible via:**
- Settings → Controls section
- Per-game customization possible

### 5.4 No Achievements Found
- Repository does not appear to have a dedicated achievements system
- Only cheats and hotkeys are implemented

---

## 6. BRANDING - FILES & STRUCTURE

### 6.1 App Name Strings
**Primary Location:** `/home/user/Apps/PocketBoy/src/android/app/src/main/res/values/strings.xml`
```xml
<string name="app_name" translatable="false">Azahar</string>
```

**Branding References Throughout Strings:**
- "Azahar" used in:
  - `app_notification_channel_name`
  - `app_notification_running`
  - Multiple UI descriptions and help texts

**Language Variants:** `/home/user/Apps/PocketBoy/src/android/app/src/main/res/values-*/strings.xml`
- values-fr, values-it, values-de, values-el, values-nl, values-b+ja+JP, etc. (20+ languages)

### 6.2 Icons & Logos
**Icons Location:** `/home/user/Apps/PocketBoy/src/android/app/src/main/res/drawable/`

**Key Icon Files:**
- `ic_citra.xml` (27KB) - Main Citra logo
- `ic_citra_full.xml` (28KB) - Full Citra logo with text
- `ic_citra_monochrome.xml` (23KB) - Monochrome version
- `ic_launcher.xml` (mipmap-anydpi-v26) - Adaptive launcher icon
- Button graphics: `button_home.xml`, `button_home_pressed.xml`
- Various UI icons: `ic_back.xml`, `ic_controller.xml`, `ic_audio.xml`, etc. (62 total)

**Bitmap Icons (per density):**
- `/home/user/Apps/PocketBoy/src/android/app/src/main/res/drawable-hdpi/`
- `/home/user/Apps/PocketBoy/src/android/app/src/main/res/drawable-xhdpi/`
- `/home/user/Apps/PocketBoy/src/android/app/src/main/res/drawable-xxhdpi/`
- `/home/user/Apps/PocketBoy/src/android/app/src/main/res/drawable-xxxhdpi/`

### 6.3 Themes & Color Schemes
**Themes File:** `/home/user/Apps/PocketBoy/src/android/app/src/main/res/values/themes.xml`

**Available Themes:**
- Theme.Citra.Blue (primary)
- Theme.Citra.Red
- Theme.Citra.Green
- Theme.Citra.Orange
- Theme.Citra.Purple
- Night mode variants for each

**Color Palette:** `/home/user/Apps/PocketBoy/src/android/app/src/main/res/values/citra_colors.xml`
- Contains color definitions for: primary, secondary, tertiary, background, surface, error colors
- Variants for each theme (Blue, Red, Green, Orange, Purple)
- Colors used in styles.xml

**Night Mode Themes:** `/home/user/Apps/PocketBoy/src/android/app/src/main/res/values-night/themes.xml`

### 6.4 Application ID
**File:** `/home/user/Apps/PocketBoy/src/android/app/build.gradle.kts`
```kotlin
applicationId = "io.github.lime3ds.android"
```
- Changes with build type:
  - Release: `io.github.lime3ds.android`
  - Debug: `io.github.lime3ds.android.debug`
  - RelWithDebInfo: `io.github.lime3ds.android.debug`

### 6.5 Qt Desktop Themes (for reference)
**Location:** `/home/user/Apps/PocketBoy/dist/qt_themes/`
- Contains desktop UI themes (not used in Android app)
- Includes: default, colorful, qdarkstyle_midnight_blue

---

## 7. BUILD SYSTEM ORGANIZATION

### 7.1 Gradle Build System (Android)
**Files:**
- **Main build file:** `/home/user/Apps/PocketBoy/src/android/app/build.gradle.kts` (282 lines)
  - Plugin usage: Android Application, Kotlin, ProGuard, SafeArgs navigation
  - Compilation: Java 17, Kotlin
  - Build variants: debug, release, relWithDebInfo
  - Signing configuration with environment variables
  - CMake integration for native code
  - Vulkan Validation Layers download task

- **Gradle wrapper:** `/home/user/Apps/PocketBoy/src/android/gradle/wrapper/`

**Build Configuration Details:**
- **Compilation SDK:** android-35
- **NDK Version:** 27.1.12297006
- **Min SDK:** 29
- **Target SDK:** 35
- **ABI Filters:** arm64-v8a, x86_64
- **Version Code:** Auto-generated from system time
- **Version Name:** From git tags/commits

### 7.2 CMake Build System (Native Code)
**Files:**
- **Root CMakeLists.txt:** `/home/user/Apps/PocketBoy/CMakeLists.txt` (20KB)
- **Android app CMakeLists referenced from:** gradle.kts
  - Location: `../../../CMakeLists.txt` (relative to build.gradle.kts)
- **CMake modules:** `/home/user/Apps/PocketBoy/CMakeModules/`

**CMake Configuration:**
- Qt disabled: `-DENABLE_QT=0`
- SDL2 disabled: `-DENABLE_SDL2=0`
- NEON support: `-DANDROID_ARM_NEON=true`
- Flexible page size support: `-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON`

### 7.3 Dependencies
**Gradle Dependencies (gradle.kts):**
- androidx (activity, appcompat, core, fragment, lifecycle, navigation, preference, recyclerview, work)
- Material Design 3
- Coil (image loading)
- ini4j (INI file parsing)
- kotlinx-serialization
- Various others

**External Libraries (externals/):**
- GLAD (OpenGL loader)
- SDL2, cryptopp, libuv, libusb, and 40+ others

### 7.4 Build Outputs
**Output Locations:**
- APKs: `layout.buildDirectory.dir("bundle")`
- Native libraries: Downloaded from Vulkan SDK
- ProGuard rules: `/home/user/Apps/PocketBoy/src/android/app/proguard-rules.pro`

### 7.5 Version Management
**Git-based versioning:**
```kotlin
versionCode = (((System.currentTimeMillis() / 1000) - 1451606400) / 10).toInt()
// Uses seconds since Jan 1, 2016, divided by 10
```

**Functions in gradle.kts:**
- `getGitVersion()` - Gets version from git tags
- `getGitHash()` - Gets short commit hash
- `getBranch()` - Gets current branch name
- `runGitCommand()` - Generic git command runner

---

## MODIFICATION TARGETS FOR REBRANDING

### Critical Files to Modify:

1. **App Branding:**
   - `/home/user/Apps/PocketBoy/src/android/app/src/main/res/values/strings.xml` - Change `app_name`
   - `/home/user/Apps/PocketBoy/src/android/app/src/main/res/values/strings.xml` - Update all "Azahar" references
   - `/home/user/Apps/PocketBoy/src/android/app/build.gradle.kts` - Change `applicationId`
   - `/home/user/Apps/PocketBoy/src/android/app/src/main/AndroidManifest.xml` - Update if needed

2. **Icons & Visual Assets:**
   - `/home/user/Apps/PocketBoy/src/android/app/src/main/res/drawable/ic_citra*.xml` - Replace with new logos
   - `/home/user/Apps/PocketBoy/src/android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` - Adaptive icon config
   - Bitmap icons in drawable-hdpi, drawable-xhdpi, etc.

3. **Color Themes:**
   - `/home/user/Apps/PocketBoy/src/android/app/src/main/res/values/citra_colors.xml` - Update color palette
   - `/home/user/Apps/PocketBoy/src/android/app/src/main/res/values/themes.xml` - Modify theme definitions

4. **Settings & Feature Customization:**
   - `/home/user/Apps/PocketBoy/src/android/app/src/main/java/org/citra/citra_emu/features/` - Modify features as needed
   - INI file handling via `SettingsFile.kt`
   - Setting sections in `Settings.kt`

---

## DIRECTORY SUMMARY

| Component | Location | Key Files |
|-----------|----------|-----------|
| **Main App Code** | `src/android/app/src/main/java/org/citra/citra_emu/` | CitraApplication.kt, NativeLibrary.kt |
| **Settings/Prefs** | `features/settings/` | Settings.kt, SettingsFile.kt, SettingsActivity.kt |
| **Cheats** | `features/cheats/` | Cheat.kt, CheatEngine.kt, CheatsActivity.kt |
| **Hotkeys** | `features/hotkeys/` | Hotkey.kt, HotkeyUtility.kt |
| **UI Resources** | `src/main/res/` | strings.xml, themes.xml, citra_colors.xml |
| **Layouts** | `src/main/res/layout/` | 26+ XML layout files |
| **Drawable Assets** | `src/main/res/drawable*/` | 62 vector icons + bitmap variants |
| **Build Config** | `app/build.gradle.kts` | App ID, versions, build types |
| **Native Code** | `src/core/`, `src/video_core/`, etc. | C++ emulation engine |
| **Cheats Engine** | `src/core/cheats/` | C++ cheat support |

