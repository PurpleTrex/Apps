# Gaia - Offline Maps Navigation App

An innovative offline maps navigation application with privacy-centric design and unique features for enhanced user experience.

## 🌟 Key Features

### 1. Complete Offline Maps
- Download entire city maps for offline use
- No internet connection required after download
- Comprehensive coverage of major cities
- Local storage of map tiles using OpenStreetMap data

### 2. Customizable Navigation Preferences
- **Route Types**: Choose between fastest, shortest, or scenic routes
- **Road Preferences**: 
  - Prefer Interstate roads
  - Prefer Residential roads
  - Avoid highways
  - Avoid toll roads
- Preferences saved locally and applied to all navigation

### 3. Draw Your Route Feature
- Visually draw your desired path on the map
- App converts drawn paths into navigable directions
- Full control over your route selection
- Integration with offline maps for accurate routing

### 4. Sensitive Mode
- Avoid construction zones and police activity
- Uses community-submitted incident reports
- Dynamically adjusts routes based on real-time data
- Enhanced comfort for users with sensory sensitivities

### 5. Distraction-Free Navigation Mode
- Blocks all phone notifications during navigation
- Prevents distractions while driving
- Automatically enables/disables with navigation
- Promotes safer driving experience

### 6. User-Submitted Data
- Report incidents in real-time:
  - Traffic accidents
  - Construction zones
  - Police activity
  - Weather hazards
  - Road closures
- Community-driven map accuracy
- Local data storage for privacy

### 7. Privacy-Centric Design
- ✓ No external tracking of user location
- ✓ No metadata collection
- ✓ All navigation processed locally on device
- ✓ Your location never leaves your device
- ✓ No account required
- ✓ No internet connection required for navigation

## 🛠️ Technical Stack

### Frontend Development
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

### Backend/Services
- **Map Provider**: OpenStreetMap (OSM) via OSMDroid library
- **Routing Engine**: GraphHopper for offline route calculation
- **Location Services**: Android Location Services API
- **Notification Management**: Android Notification Manager API

### Data Storage
- **Map Tiles**: Local file system storage
- **User Data**: Room Database (SQLite)
- **Preferences**: DataStore (SharedPreferences successor)

### Key Dependencies
- OSMDroid 6.1.18 - Offline map rendering
- GraphHopper 8.0 - Routing engine
- Jetpack Compose - Modern UI toolkit
- Room - Local database
- WorkManager - Background downloads

## 📱 Application Structure

### Main Screens

#### 1. Home Screen (MainActivity)
- Interactive map view
- Search functionality for locations
- Quick access to download maps
- Settings and incident reporting
- Privacy notice display
- Location permission handling

#### 2. Download Maps Screen
- Browse available cities
- Search for specific cities
- Download maps for offline use
- Manage downloaded maps
- View download progress and storage info

#### 3. Settings Screen
- Configure navigation preferences
- Set route type preferences
- Enable/disable special modes:
  - Sensitive Mode
  - Distraction-Free Mode
- View privacy information

#### 4. Navigation Screen
- Turn-by-turn navigation display
- Real-time route visualization
- Distance and ETA information
- Distraction-free mode toggle
- Incident reporting during navigation
- Route recalculation

## 🔐 Privacy & Security

### No Tracking Guarantee
- Location data never transmitted to external servers
- No analytics or tracking SDKs
- No user accounts or authentication
- All processing happens on-device

### Data Storage
- Map tiles stored in app-specific directory
- User preferences encrypted locally
- Incident reports stored locally (optional sharing)
- No cloud synchronization

### Permissions
- **Location**: Required for navigation and current position
- **Storage**: Required for offline map storage
- **Notification Policy**: Optional for distraction-free mode

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24 or higher
- 2GB+ free storage for map downloads

### Building the App

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Build and run on device or emulator

```bash
./gradlew assembleDebug
```

### First-Time Setup
1. Grant location permissions when prompted
2. Download maps for your area from Download Maps screen
3. Configure navigation preferences in Settings
4. Start navigating!

## 📋 User Workflow

### Downloading Maps
1. Open app and tap "Download Maps"
2. Search for your city
3. Tap on city to download
4. Wait for download to complete
5. Maps are now available offline

### Starting Navigation
1. Search for destination in home screen
2. Review route options
3. Optional: Draw custom route
4. Tap "Start Navigation"
5. Enable distraction-free mode if desired
6. Follow turn-by-turn directions

### Reporting Incidents
1. During navigation, tap "Report" button
2. Select incident type
3. Incident location automatically captured
4. Submit to help community

## 🎨 UI Design Philosophy

### Material Design 3
- Modern, clean interface
- Consistent design language
- Accessibility-focused
- Dark mode support (planned)

### Color Scheme
- Primary: Blue (#2196F3) - Trust and reliability
- Secondary: Orange (#FF9800) - Action and energy
- Success: Green (#4CAF50) - Positive feedback
- Error: Red (#F44336) - Warnings and alerts

## 🔄 Future Enhancements

- [ ] Dark mode support
- [ ] Voice-guided navigation
- [ ] Multi-language support
- [ ] Bicycle and walking routes
- [ ] Public transit integration
- [ ] Parking information
- [ ] Speed limit warnings
- [ ] Alternative route suggestions
- [ ] Route sharing (privacy-preserving)
- [ ] Weather overlay

## 📄 License

This project is part of the PurpleTrex/Apps repository.

## 🤝 Contributing

Contributions welcome! Please ensure:
- Privacy-first approach maintained
- No tracking or analytics added
- Code follows Kotlin coding standards
- UI follows Material Design guidelines

## 📞 Support

For issues or questions, please open an issue in the repository.

---

**Remember**: Gaia respects your privacy. Your location and navigation data never leave your device.
