# Gaia - Technical Requirements & Implementation

## Overview
This document provides detailed technical requirements for the Gaia offline maps navigation application.

## 1. Feature Specifications

### 1.1 Complete Offline Maps

**Description**: Users can download entire city maps for offline navigation.

**Technical Implementation**:
- **Map Tiles**: OpenStreetMap via OSMDroid library
- **Storage**: Local file system using Android's external files directory
- **Tile Provider**: TileSourceFactory.MAPNIK for standard street maps
- **Zoom Levels**: Support levels 1-19 for detailed navigation
- **Format**: Standard OSM tile format (PNG, 256x256 pixels)

**Connection Points**:
- Integrates with DownloadMapsActivity for user selection
- WorkManager handles background downloads
- Room database tracks downloaded regions
- Navigation engine uses tiles for route visualization

**Code Location**: 
- `GaiaApplication.kt` - OSMDroid configuration
- `DownloadMapsActivity.kt` - Download UI
- `MainActivity.kt` - Map display

---

### 1.2 Customizable Navigation Preferences

**Description**: Users personalize navigation with route preferences.

**Technical Implementation**:
- **Storage**: DataStore for persistent preferences
- **Data Model**: `NavigationPreferences` data class
- **Options**:
  - Route Type: FASTEST, SHORTEST, SCENIC
  - Road Preferences: Interstate, Residential
  - Avoidance: Highways, Tolls
  - Special Mode: Sensitive Mode

**Connection Points**:
- SettingsActivity provides UI for configuration
- Routing engine applies preferences during calculation
- Saved preferences persist across app sessions

**Code Location**:
- `data/NavigationPreferences.kt` - Data model
- `SettingsActivity.kt` - Settings UI

---

### 1.3 Draw Your Route Feature

**Description**: Users can draw custom routes on the map.

**Technical Implementation**:
- **Drawing**: OSMDroid Polyline overlay with touch events
- **Path Processing**: Convert drawn points to road-snapped waypoints
- **Route Calculation**: GraphHopper processes waypoints
- **Visual Feedback**: Real-time polyline rendering

**Connection Points**:
- MainActivity enables draw mode via FAB
- Custom overlay captures touch events
- Route data stored in Route data class
- Navigation uses drawn route for guidance

**Code Location**:
- `MainActivity.kt` - Draw mode toggle
- `data/Route.kt` - Route data model

---

### 1.4 Sensitive Mode

**Description**: Avoids construction zones and police activity.

**Technical Implementation**:
- **Incident Storage**: Room database for local incidents
- **Data Structure**: `IncidentReport` with location and type
- **Route Calculation**: GraphHopper custom weighting to avoid areas
- **Expiration**: Time-based incident expiry (e.g., 24 hours)

**Connection Points**:
- User reports stored locally
- Routing engine queries incidents near route
- Dynamic route recalculation when incidents detected
- Settings toggle enables/disables feature

**Code Location**:
- `data/IncidentReport.kt` - Incident data model
- `SettingsActivity.kt` - Sensitive mode toggle

---

### 1.5 Distraction-Free Navigation Mode

**Description**: Blocks notifications during active navigation.

**Technical Implementation**:
- **API**: Android Notification Manager API
- **Filter Mode**: INTERRUPTION_FILTER_PRIORITY (Do Not Disturb)
- **Permission**: ACCESS_NOTIFICATION_POLICY required
- **Auto-Toggle**: Enables with navigation start, disables on end

**Connection Points**:
- NavigationActivity manages notification policy
- NavigationService maintains state during background
- Settings allows user to enable/disable feature
- Automatic restoration when navigation ends

**Code Location**:
- `NavigationActivity.kt` - DND implementation
- `service/NavigationService.kt` - Background service

---

### 1.6 User-Submitted Data

**Description**: Community-driven incident reporting system.

**Technical Implementation**:
- **Storage**: Room database (local-first)
- **Data Model**: IncidentReport with type, location, timestamp
- **Types**: 
  - TRAFFIC_ACCIDENT
  - CONSTRUCTION
  - POLICE_ACTIVITY
  - WEATHER_HAZARD
  - ROAD_CLOSURE
- **Privacy**: Optional anonymous sharing (future)

**Connection Points**:
- Report button in MainActivity and NavigationActivity
- Current location automatically captured
- Incidents influence Sensitive Mode routing
- Time-based expiration for data freshness

**Code Location**:
- `data/IncidentReport.kt` - Data model

---

### 1.7 Privacy-Centric Design

**Description**: Zero external tracking, all processing local.

**Technical Implementation**:
- **No Analytics**: No Firebase Analytics, Google Analytics, etc.
- **No Accounts**: No user authentication or cloud sync
- **Local Processing**: All routing via GraphHopper on-device
- **No Network**: Navigation works 100% offline
- **Encrypted Storage**: User data encrypted using AndroidX Security

**Architecture Principles**:
- No external API calls during navigation
- Map tiles downloaded once, cached locally
- Incident reports stored locally only
- Location never transmitted to servers

**Code Location**:
- All activities and services - local processing
- No network permission used during navigation
- AndroidManifest.xml - minimal permissions

---

## 2. Languages and Frameworks

### 2.1 Primary Language
- **Kotlin**: Modern Android development
- **Version**: 1.9.22
- **Target JVM**: 17

### 2.2 UI Framework
- **Jetpack Compose**: Modern declarative UI
- **Version**: 2024.02.01 (BOM)
- **Material 3**: Latest Material Design components

### 2.3 Backend/Services
- **Android Services**: Foreground service for navigation
- **WorkManager**: Background map downloads
- **Room**: Local database persistence

---

## 3. APIs and SDKs

### 3.1 Map Provider
- **OSMDroid**: OpenStreetMap Android library
- **Version**: 6.1.18
- **Purpose**: Offline map rendering and display
- **License**: Apache 2.0

### 3.2 Routing Engine
- **GraphHopper**: Open-source routing engine
- **Version**: 8.0
- **Purpose**: Calculate routes with custom preferences
- **Features**: Offline routing, custom weighting
- **License**: Apache 2.0

### 3.3 Location Services
- **Google Play Services Location**: 21.1.0
- **Purpose**: GPS location for navigation
- **Fallback**: Android LocationManager

### 3.4 Notification Management
- **Android NotificationManager**: System API
- **Purpose**: Control notification policy for distraction-free mode
- **Requires**: ACCESS_NOTIFICATION_POLICY permission (API 23+)

---

## 4. Additional Tools and Services

### 4.1 File Storage
- **Android External Storage**: For map tiles
- **Path**: `context.getExternalFilesDir("osmdroid/tiles")`
- **Scoped Storage**: Compatible with Android 10+ restrictions

### 4.2 Background Processing
- **WorkManager**: For map downloads
- **Type**: OneTimeWorkRequest with constraints
- **Constraints**: Network available, battery not low

### 4.3 Local Database
- **Room**: SQLite abstraction
- **Entities**: DownloadedMap, IncidentReport
- **DAO**: Type-safe database access

### 4.4 Preferences
- **DataStore**: Modern SharedPreferences replacement
- **Storage**: Navigation preferences, user settings
- **Type-safe**: Proto DataStore for complex objects

---

## 5. User Interface Screens

### 5.1 Home Screen (MainActivity)
**Purpose**: Main map view and search

**Components**:
- MapView (OSMDroid) - Full-screen map
- Search bar - Location search
- FAB - My Location button
- Extended FAB - Draw Route button
- Top bar - Download maps, settings, menu
- Bottom card - Privacy notice

**Interactions**:
- Pan/zoom map
- Search locations
- Center on current location
- Navigate to other screens

---

### 5.2 Download Maps Screen
**Purpose**: Select and download city maps

**Components**:
- List of cities with search
- Download progress indicators
- Downloaded maps with delete option
- Storage usage information

**Interactions**:
- Search cities
- Tap to download
- Delete downloaded maps
- View download progress

---

### 5.3 Settings Screen
**Purpose**: Configure app preferences

**Components**:
- Route type selector (Radio buttons)
- Road preference switches
- Special modes toggles
- Privacy information card

**Interactions**:
- Select route preferences
- Enable/disable modes
- View privacy guarantees

---

### 5.4 Navigation Screen
**Purpose**: Active turn-by-turn navigation

**Components**:
- Full-screen map with route
- Top card - Next instruction, distance, ETA
- Bottom card - Controls and status
- Distraction-free toggle
- Report/End buttons

**Interactions**:
- View route and current position
- Toggle distraction-free mode
- Report incidents
- End navigation

---

## 6. Data Models

### 6.1 NavigationPreferences
```kotlin
data class NavigationPreferences(
    val routeType: RouteType,
    val avoidHighways: Boolean,
    val avoidTolls: Boolean,
    val preferResidential: Boolean,
    val preferInterstate: Boolean,
    val sensitiveMode: Boolean
)
```

### 6.2 IncidentReport
```kotlin
data class IncidentReport(
    val id: String,
    val type: IncidentType,
    val location: GeoPoint,
    val description: String,
    val timestamp: Date,
    val expiresAt: Date?
)
```

### 6.3 Route
```kotlin
data class Route(
    val waypoints: List<GeoPoint>,
    val distance: Double,
    val duration: Long,
    val instructions: List<RouteInstruction>,
    val isUserDrawn: Boolean
)
```

### 6.4 DownloadedMap
```kotlin
data class DownloadedMap(
    val id: String,
    val name: String,
    val boundingBox: BoundingBox,
    val downloadDate: Long,
    val sizeBytes: Long,
    val isComplete: Boolean
)
```

---

## 7. Permissions

### Required Permissions
- `ACCESS_FINE_LOCATION`: GPS navigation
- `ACCESS_COARSE_LOCATION`: Network location
- `INTERNET`: Map downloads (not for navigation)
- `ACCESS_NETWORK_STATE`: Check network for downloads
- `FOREGROUND_SERVICE`: Navigation service

### Optional Permissions
- `ACCESS_NOTIFICATION_POLICY`: Distraction-free mode
- `POST_NOTIFICATIONS`: Navigation notifications (Android 13+)
- `WRITE_EXTERNAL_STORAGE`: Map storage (Android 9 and below)

---

## 8. Architecture

### 8.1 MVVM Pattern (Planned)
- **Model**: Data classes and Room entities
- **ViewModel**: State management with StateFlow
- **View**: Jetpack Compose UI

### 8.2 Repository Pattern
- Local data source (Room, DataStore)
- No remote data sources (privacy-first)
- Single source of truth

### 8.3 Dependency Injection (Future)
- Hilt/Dagger for DI
- Service locator pattern currently

---

## 9. Build Configuration

### 9.1 Gradle Configuration
- Android Gradle Plugin: 8.3.0
- Kotlin Gradle Plugin: 1.9.22
- Compose Compiler: 1.5.8

### 9.2 SDK Versions
- Min SDK: 24 (Android 7.0, Nougat)
- Target SDK: 34 (Android 14)
- Compile SDK: 34

### 9.3 Build Types
- **Debug**: Development builds
- **Release**: Production with ProGuard

---

## 10. Testing Strategy (Planned)

### 10.1 Unit Tests
- Data models
- Route calculation logic
- Preference management

### 10.2 Integration Tests
- Database operations
- Navigation service
- Map download

### 10.3 UI Tests
- Compose UI testing
- Screen navigation
- User interactions

---

## 11. Performance Considerations

### 11.1 Map Rendering
- Tile caching for smooth scrolling
- Hardware acceleration enabled
- Memory management for large maps

### 11.2 Routing
- Offline graph processing
- Optimized pathfinding algorithms
- Background thread for calculations

### 11.3 Battery Optimization
- GPS mode selection (high accuracy when needed)
- Background service optimizations
- Wake lock management

---

## 12. Security Considerations

### 12.1 Data Encryption
- AndroidX Security Crypto for sensitive data
- Encrypted SharedPreferences for settings
- No plaintext storage of location history

### 12.2 Privacy
- No external analytics
- No crash reporting to external services
- Local-only processing

### 12.3 Permissions
- Runtime permission requests
- Clear permission explanations
- Minimal permission usage

---

## 13. Deployment

### 13.1 App Distribution
- Google Play Store (primary)
- F-Droid (privacy-focused users)
- Direct APK download

### 13.2 Version Management
- Semantic versioning (MAJOR.MINOR.PATCH)
- Changelog maintenance
- Migration strategies for updates

---

## 14. Future Roadmap

### Phase 1 (Current)
- ✓ Basic offline maps
- ✓ Navigation preferences
- ✓ Privacy-first design
- ✓ Distraction-free mode

### Phase 2 (Planned)
- [ ] Voice navigation
- [ ] Draw route implementation
- [ ] Incident reporting UI
- [ ] Route calculation with preferences

### Phase 3 (Future)
- [ ] Multi-language support
- [ ] Dark mode
- [ ] Bicycle/walking routes
- [ ] Public transit integration

---

This technical specification provides the complete framework for implementing all features described in the product requirements document.
