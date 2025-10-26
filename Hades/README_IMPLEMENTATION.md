# Hades SSH Server - Android Application

## Overview

Hades is an Android SSH Server application with a native file browser GUI, built according to the specifications in `readme.md`. The app allows users to run an SSH server on their Android device and browse the file system through a Material Design 3 interface.

## Project Structure

```
Hades/
├── app/
│   ├── src/main/
│   │   ├── java/com/hades/sshserver/
│   │   │   ├── data/              # Data models
│   │   │   │   ├── FileModels.kt      # File, Storage, Navigation models
│   │   │   │   └── ServerModels.kt     # Server, Session, Auth models
│   │   │   ├── repository/        # Data access layer
│   │   │   │   └── FileRepository.kt   # File operations
│   │   │   ├── service/           # Background services
│   │   │   │   └── SshServerService.kt # SSH server implementation
│   │   │   ├── receiver/          # Broadcast receivers
│   │   │   │   └── BootReceiver.kt     # Auto-start on boot
│   │   │   ├── ui/                # User interface
│   │   │   │   ├── MainActivity.kt      # Main activity
│   │   │   │   ├── theme/
│   │   │   │   │   └── Theme.kt         # Material Design 3 theme
│   │   │   │   ├── screens/
│   │   │   │   │   ├── FileBrowserScreen.kt  # File browser UI
│   │   │   │   │   └── ServerStatusScreen.kt # Server control UI
│   │   │   │   ├── components/
│   │   │   │   │   └── FileListItem.kt       # File list item component
│   │   │   │   └── viewmodel/
│   │   │   │       ├── FileBrowserViewModel.kt
│   │   │   │       └── ServerViewModel.kt
│   │   │   └── util/              # Utility classes
│   │   │       ├── FileUtils.kt        # File formatting utilities
│   │   │       └── NetworkUtils.kt     # Network utilities
│   │   ├── res/                   # Android resources
│   │   │   ├── values/
│   │   │   │   ├── strings.xml    # String resources
│   │   │   │   ├── colors.xml     # Color definitions
│   │   │   │   └── themes.xml     # Material theme
│   │   │   └── xml/
│   │   │       └── file_paths.xml # FileProvider paths
│   │   └── AndroidManifest.xml    # App manifest with permissions
│   └── build.gradle.kts           # App-level build configuration
├── build.gradle.kts               # Project-level build configuration
├── settings.gradle.kts            # Gradle settings
└── gradle.properties              # Gradle properties

```

## Implemented Features

### ✅ Core Architecture
- **Android Project Setup**: Complete Gradle-based Android project structure
- **Dependencies**: Apache MINA SSHD, Jetpack Compose, Material 3
- **Permissions**: All required permissions configured in AndroidManifest

### ✅ SSH Server
- **Service Implementation**: Foreground service with notification
- **Basic Authentication**: Simple username/password authentication (admin/admin)
- **SFTP Support**: SFTP subsystem configured
- **Start/Stop Control**: UI controls for server management
- **Status Monitoring**: Real-time server status display

### ✅ File Browser
- **Material Design 3 UI**: Modern, native Android UI using Jetpack Compose
- **File List View**: Displays files and folders with icons, sizes, and dates
- **Navigation**: Breadcrumb navigation and back button support
- **File Operations**:
  - Create new folders
  - Delete files/folders
  - File selection (single and multiple)
  - Copy/Move operations
  - Rename files
- **Sort Options**: Multiple sort orders (name, date, size, type)
- **Permission Handling**: Runtime permission requests for storage access

### ✅ ViewModels & State Management
- **FileBrowserViewModel**: Manages file list, navigation, and operations
- **ServerViewModel**: Manages server status and configuration
- **StateFlow**: Reactive state management using Kotlin Flow

### ✅ Network Features
- **IP Address Display**: Shows local IP address for connection
- **WiFi Detection**: Monitors WiFi connection status
- **Connection Info**: Displays SSH connection command

### ✅ UI/UX
- **Bottom Navigation**: Tab navigation between File Browser and Server screens
- **Dialogs**: Create folder, delete confirmation dialogs
- **Error Handling**: User-friendly error messages
- **Loading States**: Loading indicators during operations
- **Empty States**: Helpful messages for empty folders

## Technologies Used

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **SSH Library**: Apache MINA SSHD 2.10.0
- **Architecture**: MVVM (Model-View-ViewModel)
- **Concurrency**: Kotlin Coroutines & Flow
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)

## Building the Project

### Prerequisites
- Android Studio (latest version recommended)
- Android SDK 34
- JDK 17 or higher
- Gradle 8.5+ (wrapper included)

### Build Steps

1. Open the project in Android Studio
2. Sync Gradle files
3. Build the project:
   ```bash
   ./gradlew build
   ```
4. Run on device/emulator:
   ```bash
   ./gradlew installDebug
   ```

### Building from Command Line

```bash
cd Hades
./gradlew assembleDebug    # Build debug APK
./gradlew assembleRelease  # Build release APK
```

## Usage

1. **Grant Permissions**: On first launch, grant storage and notification permissions
2. **Start Server**: Navigate to the "Server" tab and tap "Start Server"
3. **Connect via SSH**: Use the displayed connection command from another device
4. **Browse Files**: Use the "Files" tab to navigate your device's file system
5. **Manage Files**: Long-press to select files, use dialogs for operations

### Default Credentials
- **Username**: admin
- **Password**: admin

⚠️ **Security Warning**: Change these credentials before using in production!

## Configuration

### Server Settings (in `ServerModels.kt`)
- **Port**: 2222 (default)
- **Bind Address**: 0.0.0.0 (all interfaces)
- **Max Connections**: 5
- **Session Timeout**: 300 seconds

## Future Enhancements

The following features from the specification are planned for future releases:

- [ ] Public key authentication
- [ ] Advanced session management with active session list
- [ ] Connection logging and history
- [ ] Navigation drawer with quick access locations
- [ ] Search functionality
- [ ] File previews (images, text files)
- [ ] Zip/Unzip operations
- [ ] Settings screen for server configuration
- [ ] Auto-start on boot (currently disabled)
- [ ] IP whitelisting/blacklisting
- [ ] Custom credentials management
- [ ] QR code for connection details

## Security Considerations

- ⚠️ This is a development/demo implementation
- Default credentials should be changed
- Consider using public key authentication
- Be cautious when exposing SSH server on public networks
- Review and restrict file system access permissions

## License

This project follows the Apache License 2.0 as specified in the main readme.md

## Contributing

Contributions are welcome! Please ensure:
- Code follows Kotlin coding conventions
- Material Design 3 guidelines are followed
- Proper error handling is implemented
- Documentation is updated

## Notes

- The app requires Android 7.0 (API 24) or higher
- Storage permissions are required for file access
- WiFi connection recommended for SSH access
- Foreground service notification cannot be dismissed while server is running
