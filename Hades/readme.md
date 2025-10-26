create a folder called Hades. make this app. I want two views one that's the folder navigation and the second the command line so you can interact with server. App should default open to folder view. # SSH Server Android App - Development Document

## Project Overview

**App Name:** SSH Server File Manager  
**Platform:** Android (API Level 24+)  
**Purpose:** Provide SSH server functionality on Android devices with a native file system GUI that mimics the Android Files app interface

### Core Concept
Users can SSH into their Android device and browse the file system through a familiar, native Android Files app-style interface rather than a traditional terminal. The app acts as both an SSH server and a file manager GUI.

---

## Architecture Overview

### High-Level Components

```
┌─────────────────────────────────────────┐
│         User Interface Layer            │
│  (Material Design 3 File Browser UI)    │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Application Logic Layer            │
│  • File Operations Manager              │
│  • Permission Handler                   │
│  • Path Navigation                      │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         SSH Server Layer                │
│  • Apache MINA SSHD                     │
│  • Authentication                       │
│  • Session Management                   │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      File System Access Layer           │
│  • Storage Access Framework             │
│  • Media Store API                      │
│  • DocumentFile API                     │
└─────────────────────────────────────────┘
```

---

## Technical Stack

### Core Libraries

1. **SSH Server Implementation**
   - **Apache MINA SSHD** (version 2.10.0+)
     - Industry-standard SSH server library
     - Supports SSH protocol v2
     - Built-in SFTP subsystem support

2. **UI Framework**
   - **Jetpack Compose** (Material 3)
     - Modern declarative UI
     - Material Design 3 components
     - Native file browser components

3. **File System Access**
   - **Storage Access Framework (SAF)**
   - **MediaStore API** (Android 10+)
   - **DocumentFile** for unified file access
   - **File Provider** for secure file sharing

4. **Background Services**
   - **Foreground Service** for SSH server
   - **WorkManager** for periodic maintenance
   - **JobScheduler** for background tasks

### Additional Dependencies

```gradle
dependencies {
    // SSH Server
    implementation 'org.apache.sshd:sshd-core:2.10.0'
    implementation 'org.apache.sshd:sshd-sftp:2.10.0'
    
    // Jetpack Compose
    implementation 'androidx.compose.ui:ui:1.5.0'
    implementation 'androidx.compose.material3:material3:1.1.0'
    implementation 'androidx.compose.ui:ui-tooling-preview:1.5.0'
    implementation 'androidx.activity:activity-compose:1.7.0'
    
    // Navigation
    implementation 'androidx.navigation:navigation-compose:2.7.0'
    
    // ViewModel and LiveData
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.6.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-compose:2.6.0'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.0'
    
    // Storage
    implementation 'androidx.documentfile:documentfile:1.0.1'
    
    // Security
    implementation 'androidx.security:security-crypto:1.1.0-alpha06'
    
    // Icons
    implementation 'androidx.compose.material:material-icons-extended:1.5.0'
}
```

---

## Feature Requirements

### 1. SSH Server Core Features

#### Server Configuration
- Configurable SSH port (default: 2222)
- Configurable bind address (default: 0.0.0.0)
- Auto-start on boot (optional)
- Connection timeout settings
- Maximum concurrent connections limit

#### Authentication Methods
- **Password Authentication**
  - User-defined username/password
  - Secure credential storage (EncryptedSharedPreferences)
  - Password strength validation

- **Public Key Authentication**
  - RSA, ECDSA, ED25519 support
  - Import authorized_keys file
  - Key management UI
  - Key fingerprint display

#### Connection Management
- Real-time connection status display
- Active sessions list
- Session details (IP, username, connection time)
- Ability to disconnect specific sessions
- Connection logging

### 2. File System GUI Features

#### File Browser Interface (Android Files App Style)

**Main Screen Components:**

1. **Top App Bar**
   - Current path breadcrumb navigation
   - Search functionality
   - View mode toggle (grid/list)
   - Sort options (name, date, size, type)
   - More options menu

2. **Navigation Drawer**
   - Quick access locations:
     - Internal Storage
     - SD Card (if available)
     - Downloads
     - Documents
     - Pictures
     - Videos
     - Audio
     - Recent Files
     - Favorites
   - SSH Server status indicator
   - Settings shortcut

3. **Main Content Area**
   - File/folder list with icons
   - File metadata (size, date modified)
   - Selection mode (long-press)
   - Swipe gestures for actions
   - Pull-to-refresh

4. **Bottom Navigation / FAB**
   - New folder button
   - Upload file button (for SSH context)
   - Paste button (when clipboard has content)

#### File Operations
- View (open with appropriate app)
- Copy/Cut/Paste
- Rename
- Delete
- Move
- Share
- File details/properties
- Create new folder
- Compress/Extract (ZIP)
- Search with filters

#### File Type Support
- Preview for common formats:
  - Images (JPEG, PNG, GIF, WebP)
  - Text files (TXT, JSON, XML, etc.)
  - Documents (PDF) via external viewer
  - Code files with syntax highlighting (optional)
- Open with system default app
- Thumbnails for images and videos

### 3. Network & Security Features

#### Network Management
- Wi-Fi connection detection
- Local IP address display
- QR code for connection details
- Network status monitoring
- Port forwarding instructions

#### Security Features
- Encrypted credential storage
- SSH host key generation and persistence
- Failed authentication attempt logging
- IP-based access control (whitelist/blacklist)
- Two-factor authentication (optional, advanced)
- Automatic session timeout

### 4. Storage Permissions & Scoped Storage

#### Android 10+ Scoped Storage Handling
- Request MANAGE_EXTERNAL_STORAGE permission (for root-level access)
- Use Storage Access Framework for user-selected directories
- MediaStore API for media collections
- App-specific storage for temporary files
- Clear permission explanations in UI

#### Permission Flow
1. Runtime permission request dialogs
2. Explanation of why each permission is needed
3. Graceful degradation if permissions denied
4. Link to app settings for permission management

---

## Data Models

### Configuration Data

```kotlin
data class ServerConfig(
    val port: Int = 2222,
    val bindAddress: String = "0.0.0.0",
    val autoStartOnBoot: Boolean = false,
    val maxConnections: Int = 5,
    val sessionTimeout: Int = 300, // seconds
    val allowPasswordAuth: Boolean = true,
    val allowPublicKeyAuth: Boolean = true,
    val enableLogging: Boolean = true
)

data class UserCredentials(
    val username: String,
    val passwordHash: String, // Never store plain text
    val authorizedKeys: List<AuthorizedKey> = emptyList()
)

data class AuthorizedKey(
    val keyType: String, // "rsa", "ecdsa", "ed25519"
    val publicKey: String,
    val fingerprint: String,
    val comment: String = ""
)
```

### File System Models

```kotlin
data class FileItem(
    val name: String,
    val path: String,
    val uri: Uri,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val mimeType: String?,
    val isHidden: Boolean = false,
    val canRead: Boolean = true,
    val canWrite: Boolean = true
)

data class StorageVolume(
    val name: String,
    val path: String,
    val totalSpace: Long,
    val freeSpace: Long,
    val isRemovable: Boolean,
    val isPrimary: Boolean
)

data class NavigationLocation(
    val name: String,
    val path: String,
    val icon: ImageVector,
    val type: LocationType
)

enum class LocationType {
    ROOT, INTERNAL_STORAGE, SD_CARD, DOWNLOADS, 
    DOCUMENTS, PICTURES, VIDEOS, AUDIO, RECENT, FAVORITES
}
```

### Session Management

```kotlin
data class SshSession(
    val sessionId: String,
    val username: String,
    val clientIp: String,
    val connectedAt: Long,
    val lastActivity: Long,
    val isActive: Boolean = true
)

data class ConnectionLog(
    val timestamp: Long,
    val event: ConnectionEvent,
    val username: String?,
    val clientIp: String,
    val details: String
)

enum class ConnectionEvent {
    CONNECT_ATTEMPT, AUTH_SUCCESS, AUTH_FAILURE, 
    DISCONNECT, FILE_ACCESS, ERROR
}
```

---

## Key Components Implementation

### 1. SSH Server Service

```kotlin
class SshServerService : Service() {
    private var sshServer: SshServer? = null
    private val binder = SshServerBinder()
    private lateinit var notificationManager: NotificationManager
    
    inner class SshServerBinder : Binder() {
        fun getService(): SshServerService = this@SshServerService
    }
    
    override fun onBind(intent: Intent): IBinder = binder
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVER -> startSshServer()
            ACTION_STOP_SERVER -> stopSshServer()
        }
        return START_STICKY
    }
    
    private fun startSshServer() {
        val config = loadServerConfig()
        
        sshServer = SshServer.setUpDefaultServer().apply {
            port = config.port
            host = config.bindAddress
            
            // Set up authentication
            passwordAuthenticator = createPasswordAuthenticator()
            publickeyAuthenticator = createPublicKeyAuthenticator()
            
            // Set up file system provider
            fileSystemFactory = createFileSystemFactory()
            
            // Configure SFTP subsystem
            subsystemFactories = listOf(
                SftpSubsystemFactory.Builder()
                    .build()
            )
            
            // Set up session listeners
            addSessionListener(SshSessionListener())
            
            start()
        }
        
        startForeground(NOTIFICATION_ID, createNotification())
        broadcastServerStatus(ServerStatus.RUNNING)
    }
    
    private fun stopSshServer() {
        sshServer?.stop()
        sshServer = null
        stopForeground(true)
        stopSelf()
        broadcastServerStatus(ServerStatus.STOPPED)
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SSH Server Running")
            .setContentText("Port: ${sshServer?.port}, Connections: ${getActiveConnections()}")
            .setSmallIcon(R.drawable.ic_server)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
    
    companion object {
        const val ACTION_START_SERVER = "com.example.sshserver.START_SERVER"
        const val ACTION_STOP_SERVER = "com.example.sshserver.STOP_SERVER"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "ssh_server_channel"
    }
}
```

### 2. File System Provider

```kotlin
class AndroidFileSystemProvider(
    private val context: Context
) : FileSystemProvider() {
    
    override fun newFileSystem(uri: URI, env: Map<String, *>): FileSystem {
        return AndroidFileSystem(context, uri)
    }
    
    override fun getFileSystem(uri: URI): FileSystem {
        return AndroidFileSystem(context, uri)
    }
}

class AndroidFileSystem(
    private val context: Context,
    private val rootUri: URI
) : FileSystem() {
    
    private val documentFileCache = mutableMapOf<String, DocumentFile>()
    
    override fun provider(): FileSystemProvider = AndroidFileSystemProvider(context)
    
    override fun getRootDirectories(): Iterable<Path> {
        return listOf(
            AndroidPath(this, "/"),
            AndroidPath(this, "/storage/emulated/0"),
            // Add external storage if available
        )
    }
    
    fun getDocumentFile(path: String): DocumentFile? {
        if (documentFileCache.containsKey(path)) {
            return documentFileCache[path]
        }
        
        // Convert path to Uri and get DocumentFile
        val uri = pathToUri(path)
        val documentFile = DocumentFile.fromTreeUri(context, uri)
        
        documentFileCache[path] = documentFile
        return documentFile
    }
    
    private fun pathToUri(path: String): Uri {
        // Convert file system path to content:// Uri
        // Handle different storage locations
        return when {
            path.startsWith("/storage/emulated/0") -> {
                // Primary storage
                Uri.parse("content://com.android.externalstorage.documents/tree/primary")
            }
            else -> {
                // App-specific storage or other locations
                Uri.fromFile(File(path))
            }
        }
    }
}
```

### 3. File Browser ViewModel

```kotlin
class FileBrowserViewModel(
    private val fileRepository: FileRepository
) : ViewModel() {
    
    private val _currentPath = MutableStateFlow<String>("/storage/emulated/0")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()
    
    private val _fileList = MutableStateFlow<List<FileItem>>(emptyList())
    val fileList: StateFlow<List<FileItem>> = _fileList.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _selectedFiles = MutableStateFlow<Set<FileItem>>(emptySet())
    val selectedFiles: StateFlow<Set<FileItem>> = _selectedFiles.asStateFlow()
    
    private val _sortOrder = MutableStateFlow(SortOrder.NAME_ASC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()
    
    init {
        loadFiles()
    }
    
    fun loadFiles(path: String = _currentPath.value) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val files = fileRepository.getFilesInDirectory(path)
                _fileList.value = sortFiles(files)
                _currentPath.value = path
            } catch (e: Exception) {
                // Handle error
                Log.e("FileBrowser", "Error loading files", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun navigateUp() {
        val parent = File(_currentPath.value).parent
        if (parent != null) {
            loadFiles(parent)
        }
    }
    
    fun navigateTo(item: FileItem) {
        if (item.isDirectory) {
            loadFiles(item.path)
        } else {
            openFile(item)
        }
    }
    
    fun toggleFileSelection(item: FileItem) {
        _selectedFiles.value = if (item in _selectedFiles.value) {
            _selectedFiles.value - item
        } else {
            _selectedFiles.value + item
        }
    }
    
    fun deleteSelectedFiles() {
        viewModelScope.launch {
            _selectedFiles.value.forEach { file ->
                fileRepository.deleteFile(file)
            }
            _selectedFiles.value = emptySet()
            loadFiles()
        }
    }
    
    fun copySelectedFiles(destinationPath: String) {
        viewModelScope.launch {
            _selectedFiles.value.forEach { file ->
                fileRepository.copyFile(file, destinationPath)
            }
            _selectedFiles.value = emptySet()
            loadFiles()
        }
    }
    
    fun createFolder(name: String) {
        viewModelScope.launch {
            fileRepository.createDirectory(_currentPath.value, name)
            loadFiles()
        }
    }
    
    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        _fileList.value = sortFiles(_fileList.value)
    }
    
    private fun sortFiles(files: List<FileItem>): List<FileItem> {
        return when (_sortOrder.value) {
            SortOrder.NAME_ASC -> files.sortedBy { it.name.lowercase() }
            SortOrder.NAME_DESC -> files.sortedByDescending { it.name.lowercase() }
            SortOrder.DATE_ASC -> files.sortedBy { it.lastModified }
            SortOrder.DATE_DESC -> files.sortedByDescending { it.lastModified }
            SortOrder.SIZE_ASC -> files.sortedBy { it.size }
            SortOrder.SIZE_DESC -> files.sortedByDescending { it.size }
            SortOrder.TYPE -> files.sortedBy { it.mimeType ?: "" }
        }
    }
    
    private fun openFile(item: FileItem) {
        // Open file with appropriate app
    }
}

enum class SortOrder {
    NAME_ASC, NAME_DESC, DATE_ASC, DATE_DESC, 
    SIZE_ASC, SIZE_DESC, TYPE
}
```

### 4. File Browser UI (Jetpack Compose)

```kotlin
@Composable
fun FileBrowserScreen(
    viewModel: FileBrowserViewModel = viewModel()
) {
    val currentPath by viewModel.currentPath.collectAsState()
    val fileList by viewModel.fileList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                onLocationSelected = { location ->
                    viewModel.loadFiles(location.path)
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                FileBrowserTopBar(
                    currentPath = currentPath,
                    onNavigateUp = { viewModel.navigateUp() },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onSearchClick = { /* Handle search */ },
                    selectedCount = selectedFiles.size,
                    onClearSelection = { /* Clear selection */ }
                )
            },
            floatingActionButton = {
                if (selectedFiles.isEmpty()) {
                    FloatingActionButton(onClick = { /* Show new folder dialog */ }) {
                        Icon(Icons.Default.CreateNewFolder, "New Folder")
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn {
                        items(fileList) { file ->
                            FileListItem(
                                file = file,
                                isSelected = file in selectedFiles,
                                onClick = { viewModel.navigateTo(file) },
                                onLongClick = { viewModel.toggleFileSelection(file) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FileListItem(
    file: FileItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File icon
            Icon(
                imageVector = getFileIcon(file),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = getFileIconColor(file)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // File info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row {
                    if (!file.isDirectory) {
                        Text(
                            text = formatFileSize(file.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = formatDate(file.lastModified),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun NavigationDrawerContent(
    onLocationSelected: (NavigationLocation) -> Unit
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SSH File Manager",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Divider()
            
            // Quick access locations
            val locations = remember {
                listOf(
                    NavigationLocation(
                        "Internal Storage",
                        "/storage/emulated/0",
                        Icons.Default.Smartphone,
                        LocationType.INTERNAL_STORAGE
                    ),
                    NavigationLocation(
                        "Downloads",
                        "/storage/emulated/0/Download",
                        Icons.Default.Download,
                        LocationType.DOWNLOADS
                    ),
                    NavigationLocation(
                        "Documents",
                        "/storage/emulated/0/Documents",
                        Icons.Default.Description,
                        LocationType.DOCUMENTS
                    ),
                    NavigationLocation(
                        "Pictures",
                        "/storage/emulated/0/Pictures",
                        Icons.Default.Image,
                        LocationType.PICTURES
                    ),
                    NavigationLocation(
                        "Videos",
                        "/storage/emulated/0/Movies",
                        Icons.Default.VideoLibrary,
                        LocationType.VIDEOS
                    )
                )
            }
            
            locations.forEach { location ->
                NavigationDrawerItem(
                    label = { Text(location.name) },
                    icon = { Icon(location.icon, contentDescription = null) },
                    selected = false,
                    onClick = { onLocationSelected(location) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            NavigationDrawerItem(
                label = { Text("Settings") },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                selected = false,
                onClick = { /* Navigate to settings */ }
            )
        }
    }
}
```

---

## Security Considerations

### 1. Authentication Security
- Use bcrypt or Argon2 for password hashing
- Implement rate limiting for failed login attempts
- Log all authentication attempts
- Support for strong key types (ED25519 preferred)
- Secure storage of private keys and credentials

### 2. Network Security
- Bind to specific interfaces when possible
- Consider VPN-only access option
- Display clear warnings about security implications
- Support for IP whitelisting
- Optional: mTLS for enhanced security

### 3. File System Security
- Enforce Android permission model
- Prevent path traversal attacks
- Validate all file operations
- Implement file access logging
- Respect app sandbox boundaries
- Handle symbolic links carefully

### 4. Data Protection
- Use EncryptedSharedPreferences for sensitive data
- Secure key storage using Android Keystore
- Clear sensitive data from memory after use
- Implement secure deletion for sensitive files
- No plain text passwords in logs or storage

---

## Permissions Required

### Android Manifest Permissions

```xml
<manifest>
    <!-- Network -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    
    <!-- Storage (Android 10+) -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="29" />
    <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"
        tools:ignore="ScopedStorage" />
    
    <!-- Android 13+ granular media permissions -->
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
    <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
    
    <!-- Foreground Service -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
    
    <!-- Boot -->
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    
    <!-- Notifications -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
</manifest>
```

### Runtime Permission Handling

Implement proper permission request flows:
1. Check if permission is granted
2. Show rationale if needed (shouldShowRequestPermissionRationale)
3. Request permission
4. Handle grant/deny result
5. Provide fallback functionality
6. Guide user to settings if permanently denied

---

## User Interface Design

### Material Design 3 Theme

```kotlin
@Composable
fun SshServerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF6750A4),
            secondary = Color(0xFF625B71),
            tertiary = Color(0xFF7D5260),
            background = Color(0xFF1C1B1F),
            surface = Color(0xFF1C1B1F)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF6750A4),
            secondary = Color(0xFF625B71),
            tertiary = Color(0xFF7D5260),
            background = Color(0xFFFFFBFE),
            surface = Color(0xFFFFFBFE)
        )
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

### Screen Layouts

1. **Main Screen (Files Browser)**
   - Full-screen file list
   - FAB for new folder
   - Navigation drawer for quick access
   - Bottom sheet for file operations

2. **Server Settings Screen**
   - Server configuration options
   - Start/stop toggle
   - Connection information display
   - Active sessions list

3. **Security Settings Screen**
   - Authentication method selection
   - Credential management
   - Authorized keys management
   - Access control rules

4. **Logs Screen**
   - Connection history
   - File access logs
   - Error logs
   - Export logs functionality

---

## Testing Strategy

### Unit Tests
- File operations logic
- Path parsing and validation
- Authentication logic
- Configuration management
- Permission checking

### Integration Tests
- SSH server start/stop
- File system access through SSH
- Authentication flows
- Session management
- Storage Access Framework integration

### UI Tests
- Navigation flows
- File selection and operations
- Settings changes
- Permission dialogs
- Error handling

### Security Tests
- Authentication bypass attempts
- Path traversal attacks
- Permission escalation attempts
- Brute force protection
- Secure credential storage

### Performance Tests
- Large directory listings
- Multiple concurrent connections
- Large file transfers
- Memory usage under load
- Battery consumption

---

## Deployment Considerations

### Build Variants

```gradle
android {
    buildTypes {
        debug {
            applicationIdSuffix ".debug"
            debuggable true
        }
        
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'),
                         'proguard-rules.pro'
        }
    }
    
    flavorDimensions "version"
    productFlavors {
        free {
            dimension "version"
            applicationIdSuffix ".free"
        }
        pro {
            dimension "version"
            applicationIdSuffix ".pro"
        }
    }
}
```

### ProGuard Rules

```proguard
# Apache SSHD
-keep class org.apache.sshd.** { *; }
-dontwarn org.apache.sshd.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# DocumentFile
-keep class androidx.documentfile.** { *; }
```

### App Distribution
- Google Play Store (primary)
- F-Droid (open source variant)
- Direct APK download from website
- Beta testing channel

---

## Monitoring & Analytics

### Metrics to Track
- Server uptime
- Number of active connections
- Average session duration
- File operations performed
- Authentication success/failure rates
- Crash reports
- ANR (Application Not Responding) events

### Privacy-Respecting Analytics
- No personally identifiable information
- Opt-in analytics
- Local-only metrics option
- Clear data retention policy

---

## Documentation Requirements

### User Documentation
1. **Quick Start Guide**
   - Installation steps
   - Initial setup wizard
   - First SSH connection

2. **Feature Guide**
   - File operations
   - Authentication setup
   - Network configuration
   - Troubleshooting

3. **Security Best Practices**
   - Strong password guidelines
   - Public key authentication setup
   - Network security recommendations
   - Access control configuration

### Developer Documentation
1. **Architecture Overview**
2. **API Documentation**
3. **Contributing Guidelines**
4. **Code Style Guide**
5. **Build Instructions**

---

## Future Enhancements

### Phase 2 Features
- SFTP client mode (connect to other servers)
- WebDAV server option
- Cloud storage integration (Google Drive, Dropbox)
- File encryption/decryption
- Batch operations
- Advanced search with regex
- File compression levels
- Scheduled backups

### Phase 3 Features
- Remote management API
- Multi-user support
- Group-based permissions
- Audit logging export
- Integration with Tasker/automation apps
- Widget for quick server toggle
- Wear OS companion app
- Desktop client application

---

## Known Limitations

### Android Platform Limitations
1. **Scoped Storage (Android 10+)**
   - Limited access to some system directories
   - Requires MANAGE_EXTERNAL_STORAGE for full access
   - May require SAF for certain folders

2. **Background Execution**
   - Foreground service required for reliable operation
   - Battery optimization may affect server uptime
   - Doze mode can interrupt connections

3. **Network Restrictions**
   - May not work on some enterprise networks
   - Port forwarding required for external access
   - Some carriers block incoming connections

4. **File System Access**
   - Cannot access other apps' private data
   - Some system directories are protected
   - External SD card access limited on some devices

---

## Development Roadmap

### Milestone 1: Core Functionality (8 weeks)
- Week 1-2: Project setup, basic UI framework
- Week 3-4: SSH server integration
- Week 5-6: File system browser implementation
- Week 7-8: Basic file operations

### Milestone 2: Enhanced Features (6 weeks)
- Week 9-10: Authentication system
- Week 11-12: Permission management
- Week 13-14: UI polish and Material Design 3 refinement

### Milestone 3: Testing & Release (4 weeks)
- Week 15-16: Comprehensive testing
- Week 17: Beta release and feedback
- Week 18: Release preparation and launch

---

## Resources

### Documentation
- [Apache MINA SSHD](https://mina.apache.org/sshd-project/)
- [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Android Scoped Storage](https://developer.android.com/training/data-storage)

### Sample Code
- [SSHD Android Example](https://github.com/apache/mina-sshd/tree/master/sshd-core)
- [DocumentFile Examples](https://developer.android.com/training/data-storage/shared/documents-files)
- [Compose File Browser](https://github.com/topics/file-manager-android)

### Tools
- Android Studio (latest stable)
- ADB for testing
- Wireshark for network debugging
- SSH clients (OpenSSH, PuTTY, etc.)

---

## Contact & Support

### Development Team Roles
- **Lead Developer**: SSH server integration, core architecture
- **UI/UX Developer**: Compose UI, Material Design implementation
- **QA Engineer**: Testing, security audit
- **DevOps**: Build automation, CI/CD

### Support Channels
- GitHub Issues (bug reports)
- Discussion Forum (feature requests)
- Email Support (security issues)
- Wiki (documentation)

---

## Version History

### v1.0.0 (Target Release)
- Initial release
- SSH server with password and public key auth
- Material Design 3 file browser
- Basic file operations
- Android 10+ support

---

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

---

## Appendix

### A. SSH Command Examples

```bash
# Connect to the server
ssh username@192.168.1.100 -p 2222

# SFTP connection
sftp -P 2222 username@192.168.1.100

# Copy files to server
scp -P 2222 file.txt username@192.168.1.100:/storage/emulated/0/Download/

# Copy files from server
scp -P 2222 username@192.168.1.100:/storage/emulated/0/file.txt ./

# Mount via SSHFS (Linux/Mac)
sshfs username@192.168.1.100:/storage/emulated/0 ~/android-mount -p 2222
```

### B. Troubleshooting Common Issues

1. **Cannot connect to server**
   - Verify server is running
   - Check firewall settings
   - Confirm IP address and port
   - Ensure devices on same network

2. **Permission denied errors**
   - Check MANAGE_EXTERNAL_STORAGE permission
   - Verify file/folder permissions
   - Enable Storage Access Framework picker

3. **Server stops after device sleep**
   - Disable battery optimization for app
   - Ensure foreground service is active
   - Check Doze mode settings

4. **Slow file transfers**
   - Check network quality
   - Reduce max connections
   - Consider encryption overhead

---

*Document Version: 1.0*  
*Last Updated: October 2025*  
*Author: Development Team*
