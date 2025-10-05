# Gaia - Architecture Overview

## System Architecture

This document visualizes the Gaia app architecture and component relationships.

---

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Gaia Application                      │
│                    (Privacy-First Design)                    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│                   (Jetpack Compose UI)                       │
├─────────────────────────────────────────────────────────────┤
│  MainActivity  │  SettingsActivity  │  NavigationActivity   │
│  DownloadMaps  │                                             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Business Logic                          │
│                    (Domain Layer)                            │
├─────────────────────────────────────────────────────────────┤
│  Navigation     │  Route          │  Incident               │
│  Preferences    │  Calculation    │  Management             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                             │
│                 (Local Storage Only)                         │
├─────────────────────────────────────────────────────────────┤
│  Room Database  │  DataStore      │  File System            │
│  (Incidents)    │  (Preferences)  │  (Map Tiles)            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    External Services                         │
│                   (Privacy-Aware)                            │
├─────────────────────────────────────────────────────────────┤
│  OSMDroid       │  GraphHopper    │  Android Location       │
│  (Map Display)  │  (Routing)      │  (GPS)                  │
└─────────────────────────────────────────────────────────────┘
```

---

## Component Interaction Flow

### 1. App Initialization
```
User Launches App
       │
       ▼
GaiaApplication.onCreate()
       │
       ├──> Configure OSMDroid
       │    - Set tile cache path
       │    - Configure user agent
       │
       └──> Create Notification Channels
            - Navigation channel
            - Download channel
```

### 2. Map Display Flow
```
MainActivity Loads
       │
       ▼
Check Location Permissions
       │
       ├──> Granted? ──> Show Map + Location Overlay
       │                       │
       │                       ▼
       │                 Load OSM Tiles from Cache
       │                       │
       │                       ├──> Cache Hit? ──> Display
       │                       │
       │                       └──> Cache Miss? ──> Download from OSM
       │
       └──> Not Granted? ──> Show Permission Request
```

### 3. Navigation Flow
```
User Searches Destination
       │
       ▼
Select Route Preferences
       │
       ▼
GraphHopper Calculates Route
       │
       ├──> Apply User Preferences
       │    - Route type (fastest/shortest)
       │    - Road preferences
       │    - Avoidances
       │
       ├──> Check Sensitive Mode
       │    - Query local incidents
       │    - Avoid problem areas
       │
       └──> Generate Route
            │
            ▼
Display Route on Map
       │
       ▼
Start Navigation Service
       │
       ├──> Enable Distraction-Free Mode?
       │    - Block notifications
       │    - Set DND mode
       │
       └──> Show Turn-by-Turn Directions
```

### 4. Download Maps Flow
```
User Opens Download Maps
       │
       ▼
Display Available Cities
       │
       ▼
User Selects City
       │
       ▼
WorkManager Schedules Download
       │
       ▼
Download OSM Tiles for Region
       │
       ├──> Show Progress Notification
       │
       ├──> Save to File System
       │
       └──> Update Room Database
            │
            ▼
Mark City as Downloaded
```

### 5. Incident Reporting Flow
```
User Taps "Report Incident"
       │
       ▼
Show Incident Type Selector
       │
       ▼
User Selects Type
       │
       ▼
Capture Current Location
       │
       ▼
Create IncidentReport
       │
       ├──> Set location (GeoPoint)
       │
       ├──> Set timestamp
       │
       └──> Set expiration (24h)
            │
            ▼
Save to Room Database
       │
       ▼
Update Map Overlay (Future)
```

---

## Data Flow Diagram

### Privacy-First Data Flow
```
┌─────────────┐
│    User     │
└──────┬──────┘
       │
       │ (Interactions)
       │
       ▼
┌─────────────────────────────────────┐
│         User Interface              │
│  (MainActivity, SettingsActivity)   │
└─────────────────────────────────────┘
       │
       │ (User Actions)
       │
       ▼
┌─────────────────────────────────────┐
│      Application Logic              │
│   (Route Calc, Preferences)         │
└─────────────────────────────────────┘
       │
       │ (Store Locally)
       │
       ▼
┌─────────────────────────────────────┐
│       Local Storage                 │
│  ┌─────────┐  ┌─────────┐          │
│  │  Room   │  │DataStore│          │
│  │Database │  │Prefs    │          │
│  └─────────┘  └─────────┘          │
│                                     │
│  ┌──────────────────────┐          │
│  │   File System        │          │
│  │   (Map Tiles)        │          │
│  └──────────────────────┘          │
└─────────────────────────────────────┘
       │
       │ (Read from Local)
       │
       ▼
┌─────────────────────────────────────┐
│      Map & Navigation               │
│   OSMDroid + GraphHopper            │
└─────────────────────────────────────┘
       │
       │ (Display)
       │
       ▼
┌─────────────┐
│    User     │
└─────────────┘

NOTE: No data leaves device! ✓
      No external servers! ✓
      No tracking! ✓
```

---

## Activity Lifecycle

### MainActivity Lifecycle
```
onCreate()
  │
  ├──> Initialize MapView
  │
  ├──> Check Permissions
  │
  └──> Load User Preferences
  
onResume()
  │
  └──> mapView.onResume()
       - Refresh location
       - Update map display

onPause()
  │
  └──> mapView.onPause()
       - Pause GPS updates
       - Save state

onDestroy()
  │
  └──> Clean up resources
```

### NavigationActivity Lifecycle
```
onCreate()
  │
  ├──> Initialize Navigation
  │
  ├──> Load Route
  │
  └──> Start Foreground Service
  
onResume()
  │
  ├──> Enable Distraction-Free Mode
  │
  └──> Resume GPS tracking

onPause()
  │
  └──> Navigation continues in background

onDestroy()
  │
  ├──> Disable Distraction-Free Mode
  │
  └──> Stop Foreground Service
```

---

## Service Architecture

### NavigationService (Foreground)
```
┌────────────────────────────────────┐
│     NavigationService              │
├────────────────────────────────────┤
│                                    │
│  START_NAVIGATION                  │
│         │                          │
│         ▼                          │
│  Create Notification               │
│         │                          │
│         ▼                          │
│  Start Foreground                  │
│         │                          │
│         ▼                          │
│  Track Location                    │
│         │                          │
│         ├──> Update ETA            │
│         │                          │
│         ├──> Check Next Turn       │
│         │                          │
│         └──> Update Notification   │
│                                    │
│  STOP_NAVIGATION                   │
│         │                          │
│         ▼                          │
│  Stop Foreground                   │
│         │                          │
│         ▼                          │
│  Clean Up                          │
│                                    │
└────────────────────────────────────┘
```

---

## Data Models Relationships

```
NavigationPreferences
  ├──> routeType: RouteType
  ├──> avoidHighways: Boolean
  ├──> preferInterstate: Boolean
  └──> sensitiveMode: Boolean
       │
       └──> influences ──> Route Calculation

IncidentReport
  ├──> location: GeoPoint
  ├──> type: IncidentType
  └──> timestamp: Date
       │
       └──> influences ──> Sensitive Mode Routing

Route
  ├──> waypoints: List<GeoPoint>
  ├──> distance: Double
  ├──> duration: Long
  └──> instructions: List<RouteInstruction>
       │
       └──> used by ──> Navigation Display

DownloadedMap
  ├──> name: String
  ├──> boundingBox: BoundingBox
  └──> isComplete: Boolean
       │
       └──> enables ──> Offline Navigation
```

---

## Permission Flow

```
App Starts
    │
    ▼
Check Location Permission
    │
    ├──> Granted?
    │    │
    │    ├──> Yes ──> Enable GPS ──> Show Location on Map
    │    │
    │    └──> No ──> Show Permission Request
    │              │
    │              ├──> User Grants ──> Enable GPS
    │              │
    │              └──> User Denies ──> Show Limited Functionality
    │
    ▼
Check Notification Permission (Android 13+)
    │
    ├──> For Navigation Alerts
    │
    └──> Optional, not required

During Navigation
    │
    ▼
Check Do Not Disturb Permission
    │
    ├──> For Distraction-Free Mode
    │
    ├──> User Grants ──> Can block notifications
    │
    └──> User Denies ──> Show warning, continue without blocking
```

---

## Thread Model

```
Main Thread (UI)
  ├──> Jetpack Compose rendering
  ├──> User interaction handling
  └──> MapView updates

Background Threads
  ├──> GraphHopper route calculation
  ├──> Room database operations
  ├──> OSM tile loading
  └──> GPS location updates

Coroutines
  ├──> ViewModel operations (Future)
  ├──> DataStore reads/writes
  └──> Network operations (map downloads)
```

---

## Security Architecture

```
┌────────────────────────────────────┐
│         User Data                  │
├────────────────────────────────────┤
│  Location History                  │
│  Navigation Preferences            │
│  Incident Reports                  │
│  Downloaded Maps                   │
└────────────────────────────────────┘
         │
         ▼
┌────────────────────────────────────┐
│    Encryption Layer                │
│  (AndroidX Security Crypto)        │
└────────────────────────────────────┘
         │
         ▼
┌────────────────────────────────────┐
│    Local Storage Only              │
│  - App-specific directory          │
│  - No cloud sync                   │
│  - No external access              │
└────────────────────────────────────┘

External Network Access:
  - Map tile downloads ONLY
  - No user data transmission
  - Optional (for initial download)
  - Not required for navigation
```

---

## Build Architecture

```
Root build.gradle.kts
  │
  ├──> Android Application Plugin 8.3.0
  │
  └──> Kotlin Plugin 1.9.22

app/build.gradle.kts
  │
  ├──> Dependencies
  │    ├──> OSMDroid
  │    ├──> GraphHopper
  │    ├──> Jetpack Compose
  │    ├──> Room
  │    └──> DataStore
  │
  ├──> Build Types
  │    ├──> Debug
  │    └──> Release (with ProGuard)
  │
  └──> Build Features
       ├──> Compose
       └──> ViewBinding

Gradle Configuration
  │
  ├──> Compile SDK: 34
  ├──> Min SDK: 24
  ├──> Target SDK: 34
  └──> JVM Target: 17
```

---

## Testing Architecture (Future)

```
Unit Tests
  ├──> Data models
  ├──> Route calculation
  └──> Preferences logic

Integration Tests
  ├──> Room database
  ├──> DataStore operations
  └──> Service lifecycle

UI Tests
  ├──> Compose UI testing
  ├──> Screen navigation
  └──> User interactions

E2E Tests
  ├──> Full navigation flow
  ├──> Map download
  └──> Permission handling
```

---

## Summary

The Gaia architecture follows these principles:

1. **Privacy-First**: No data leaves the device
2. **Offline-First**: All features work without internet
3. **Modern Android**: Uses latest Jetpack libraries
4. **Clean Architecture**: Separation of concerns
5. **User-Centric**: Designed for safety and comfort

All components work together to provide a seamless, private, offline navigation experience.
