# PocketBoy Emulator Codebase Structure - Comprehensive Analysis

## 1. ALL VIEWMODELS

### GamesViewModel
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/viewmodel/GamesViewModel.kt`
**Status**: COMPLETE

**StateFlow Properties**:
- `games`: StateFlow<List<Game>> - All loaded games
- `searchedGames`: StateFlow<List<Game>> - Search results
- `isReloading`: StateFlow<Boolean> - Reload indicator
- `shouldSwapData`: StateFlow<Boolean> - Data swap flag
- `shouldScrollToTop`: StateFlow<Boolean> - Scroll control
- `searchFocused`: StateFlow<Boolean> - Search focus state

**Functions**:
- `setGames(games: List<Game>)` - Set and sort games
- `setSearchedGames(games: List<Game>)` - Set search results
- `setShouldSwapData(shouldSwap: Boolean)` - Update swap state
- `setShouldScrollToTop(shouldScroll: Boolean)` - Update scroll state
- `setSearchFocused(searchFocused: Boolean)` - Update search focus
- `reloadGames(directoryChanged: Boolean)` - Reload game list from disk

**What it connects to**: Used by GamesFragment and SearchFragment for displaying game lists

---

### EmulationViewModel
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/viewmodel/EmulationViewModel.kt`
**Status**: COMPLETE

**StateFlow Properties**:
- `emulationStarted`: StateFlow<Boolean> - Emulation running state
- `shaderProgress`: StateFlow<Int> - Shader compilation progress
- `totalShaders`: StateFlow<Int> - Total shader count
- `shaderMessage`: StateFlow<String> - Shader status message

**Functions**:
- `setEmulationStarted(started: Boolean)` - Update emulation state
- `setShaderProgress(progress: Int)` - Update progress
- `setTotalShaders(max: Int)` - Set total shader count
- `setShaderMessage(msg: String)` - Set status message
- `updateProgress(msg: String, progress: Int, max: Int)` - Update all progress data

**What it connects to**: Used by EmulationFragment for shader loading and emulation status

---

### HomeViewModel
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/viewmodel/HomeViewModel.kt`
**Status**: COMPLETE

**StateFlow Properties**:
- `navigationVisible`: StateFlow<Pair<Boolean, Boolean>> - Nav visibility & animation
- `statusBarShadeVisible`: StateFlow<Boolean> - Status bar visibility
- `isPickingUserDir`: StateFlow<Boolean> - Directory picker state
- `userDir`: StateFlow<String> - User directory path
- `gamesDir`: StateFlow<String> - Games directory path
- `dirProgress`: StateFlow<Int> - Directory progress
- `maxDirProgress`: StateFlow<Int> - Max directory progress
- `messageText`: StateFlow<String> - Progress message
- `copyComplete`: StateFlow<Boolean> - Copy completion flag

**Mutable Properties**:
- `copyInProgress: Boolean` - Copy operation state
- `navigatedToSetup: Boolean` - Setup navigation flag

**Functions**:
- `setNavigationVisibility(visible: Boolean, animated: Boolean)`
- `setStatusBarShadeVisibility(visible: Boolean)`
- `setPickingUserDir(picking: Boolean)`
- `setUserDir(activity: FragmentActivity, dir: String)` - Updates user dir and reloads games
- `setGamesDir(activity: FragmentActivity, dir: String)` - Updates games dir and reloads games
- `clearCopyInfo()`
- `onUpdateSearchProgress(resources: Resources, directoryName: String)`
- `onUpdateCopyProgress(resources: Resources, filename: String, progress: Int, max: Int)`
- `setCopyComplete(complete: Boolean)`

**What it connects to**: MainActivity, GamesFragment, HomeSettingsFragment for navigation control

---

### TaskViewModel
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/viewmodel/TaskViewModel.kt`
**Status**: COMPLETE (Generic task runner)

**StateFlow Properties**:
- `result`: StateFlow<Any> - Task result
- `isComplete`: StateFlow<Boolean> - Completion flag
- `isRunning`: StateFlow<Boolean> - Running state
- `cancelled`: StateFlow<Boolean> - Cancellation flag

**Functions**:
- `runTask()` - Execute task on IO dispatcher
- `clear()` - Reset state
- `setCancelled(value: Boolean)` - Cancel task

**What it connects to**: Generic utility for background tasks

---

### DriverViewModel
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/viewmodel/DriverViewModel.kt`
**Status**: COMPLETE

**StateFlow Properties**:
- `areDriversLoading`: StateFlow<Boolean> - Driver loading state
- `isDriverReady`: StateFlow<Boolean> - Driver ready state
- `isDeletingDrivers`: StateFlow<Boolean> - Deletion state
- `driverList`: StateFlow<MutableList<Pair<Uri, GpuDriverMetadata>>> - Available drivers
- `selectedDriverMetadata`: StateFlow<String> - Selected driver name
- `newDriverInstalled`: StateFlow<Boolean> - New driver flag

**Mutable Properties**:
- `previouslySelectedDriver: Int` - Previous selection
- `selectedDriver: Int` - Current selection
- `driversToDelete: MutableList<Uri>` - Drivers marked for deletion

**Functions**:
- `setSelectedDriverIndex(value: Int)` - Update selected driver
- `setNewDriverInstalled(value: Boolean)` - Update driver installed flag
- `addDriver(driverData: Pair<Uri, GpuDriverMetadata>)` - Add new driver
- `removeDriver(driverData: Pair<Uri, GpuDriverMetadata>)` - Remove driver
- `onCloseDriverManager()` - Finalize driver changes

**What it connects to**: DriverManagerFragment for GPU driver management

---

### SettingsViewModel
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/features/settings/model/SettingsViewModel.kt`
**Status**: PARTIAL (Just holds Settings object)

**Properties**:
- `settings: Settings` - Settings container

**What it connects to**: MainActivity, EmulationActivity for settings access

---

### CheatsViewModel
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/features/cheats/model/CheatsViewModel.kt`
**Status**: COMPLETE

**StateFlow Properties**:
- `selectedCheat`: StateFlow<Cheat?> - Currently selected cheat
- `isAdding`: StateFlow<Boolean> - Adding new cheat
- `isEditing`: StateFlow<Boolean> - Editing state
- `cheatAddedEvent`: StateFlow<Int?> - Cheat added position
- `cheatChangedEvent`: StateFlow<Int?> - Cheat changed position
- `cheatDeletedEvent`: StateFlow<Int?> - Cheat deleted position
- `openDetailsViewEvent`: StateFlow<Boolean> - Open details view event
- `closeDetailsViewEvent`: StateFlow<Boolean> - Close details view event
- `listViewFocusChange`: StateFlow<Boolean> - List focus change event
- `detailsViewFocusChange`: StateFlow<Boolean> - Details focus change event

**Mutable Properties**:
- `cheats: Array<Cheat>` - All cheats for current game
- `cheatsNeedSaving: Boolean` - Unsaved changes flag

**Functions**:
- `initialize(titleId_: Long)` - Load cheats for game
- `load()` - Load cheats from engine
- `saveIfNeeded()` - Save cheats if modified
- `setSelectedCheat(cheat: Cheat?, position: Int)`
- `setIsEditing(value: Boolean)`
- `startAddingCheat()`
- `finishAddingCheat(cheat: Cheat?)`
- `updateSelectedCheat(newCheat: Cheat?)`
- `deleteSelectedCheat()`
- `openDetailsView()`
- `closeDetailsView()`
- `onListViewFocusChanged(changed: Boolean)`
- `onDetailsViewFocusChanged(changed: Boolean)`

**What it connects to**: CheatsFragment, CheatListFragment, CheatDetailsFragment

---

## 2. ALL ACTIVITIES

### MainActivity
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/ui/main/MainActivity.kt`
**Status**: COMPLETE (Main app entry point)

**Hosts Fragments**:
- GamesFragment (games list)
- SearchFragment (game search)
- HomeSettingsFragment (home settings/menu)
- SetupFragment (first time setup)
- AboutFragment (about page)
- LicensesFragment (licenses)
- SystemFilesFragment (system file management)
- DriverManagerFragment (GPU driver management)
- CheatsFragment (cheats management)

**ViewModels**: HomeViewModel, GamesViewModel, SettingsViewModel

**Key Features**:
- Navigation bar with bottom menu
- Window insets handling
- Navigation animations (fade through, shared axis)
- Permission checking
- CIA file installation worker
- PocketBoy directory management

**What it needs to connect to**: DualScreenActivity for 3DS dual-screen mode

---

### DualScreenActivity
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/ui/activities/DualScreenActivity.kt`
**Status**: PARTIAL (Basic structure, incomplete wiring)

**Hosts Fragments**:
- ThreeDSTopScreenFragment (top screen - games)
- ThreeDSBottomScreenFragment (bottom screen - settings)

**Key Features**:
- Dual screen container management
- Screen switching with animations
- Back button handling
- Screen indicator and toggle buttons

**Issues**:
- GamesViewModel is created fresh in ThreeDSTopScreenFragment instead of using shared instance
- No SettingsViewModel connection
- Bottom screen fragment loading happens on resume (lazy loading)
- Missing integration with existing navigation system

---

### EmulationActivity
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/activities/EmulationActivity.kt`
**Status**: COMPLETE

**Hosts Fragments**:
- EmulationFragment (game rendering)

**ViewModels**: EmulationViewModel, SettingsViewModel

**Key Features**:
- Fullscreen immersive mode
- Game input handling (keyboard, gamepad, motion)
- Settings management
- Secondary display support
- Camera and microphone permissions
- Screenshot/cheat loading
- Play time tracking

**What it connects to**: EmulationFragment for game rendering

---

### SettingsActivity
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/features/settings/ui/SettingsActivity.kt`
**Status**: COMPLETE

**Hosts Fragments**:
- SettingsFragment (settings UI)

**Key Features**:
- Settings configuration UI
- Per-game settings support
- Settings file management

---

### CheatsActivity
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/features/cheats/ui/CheatsActivity.kt`
**Status**: COMPLETE

**Hosts Fragments**:
- CheatsFragment (cheats management)

---

## 3. ALL FRAGMENTS

### GamesFragment
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/fragments/GamesFragment.kt`
**Status**: COMPLETE (Regular single-screen version)

**Dependencies**:
- GamesViewModel (shared from activity)
- HomeViewModel (shared from activity)
- GameAdapter

**Functionality**:
- Display games in grid layout
- Pull-to-refresh to reload games
- Game launch capability
- Long-press game options
- Search file browser

**Data Flow**:
1. Observes `gamesViewModel.games` StateFlow
2. Observes `isReloading` for refresh state
3. Updates nav/status bar visibility via homeViewModel
4. Calls `gamesViewModel.reloadGames()` on refresh

---

### SearchFragment
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/fragments/SearchFragment.kt`
**Status**: COMPLETE

**Dependencies**:
- GamesViewModel
- HomeViewModel
- GameAdapter
- String similarity libraries (Jaccard, JaroWinkler)

**Functionality**:
- Text search on games
- Filter by: Recently played, Recently added, Favorites (BROKEN), Downloads
- Real-time search results
- Game launch from search results

**Data Flow**:
1. Listens to text input with `doOnTextChanged`
2. Filters games based on chips selection
3. Updates `gamesViewModel.searchedGames` StateFlow
4. Displays filtered results

---

### HomeSettingsFragment
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/fragments/HomeSettingsFragment.kt`
**Status**: COMPLETE (Main menu)

**Dependencies**:
- HomeViewModel
- DriverViewModel
- HomeSettingAdapter

**Functionality**:
- Core settings
- System files management
- Driver manager
- GPU driver installation
- About section
- License viewing
- Update/download manager

**Data Flow**:
1. Shows menu options via HomeSettingAdapter
2. Navigates to appropriate fragments/activities
3. Uses HomeViewModel for navigation state

---

### ThreeDSTopScreenFragment
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/fragments/ThreeDSTopScreenFragment.kt`
**Status**: PARTIAL (Has wiring gaps)

**Dependencies**:
- GamesViewModel (creates new instance - NOT SHARED)
- GameTileView custom view

**Functionality**:
- Display games in 3-column grid
- Favorites filter (BROKEN - isFavorite doesn't exist)
- Recent games filter
- Game launch
- Game selection highlighting
- Animated entrance

**Data Flow**:
1. Observes `gamesViewModel.games`
2. Applies filter (favorites/recent/all)
3. Displays game tiles dynamically
4. Launches EmulationActivity on click

**WIRING GAPS**:
- Creates its own GamesViewModel instead of using shared instance
- Uses `it.isFavorite` but Game class doesn't have this property
- "Recent" just takes first 6 games (not sorted by play time)
- Search button not implemented (TODO)
- Game options menu not implemented (TODO)

---

### ThreeDSBottomScreenFragment
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/fragments/ThreeDSBottomScreenFragment.kt`
**Status**: PARTIAL (Stub implementations)

**Dependencies**:
- None (direct implementation)

**Functionality**:
- System settings button
- Install CIA button
- Select games folder
- GPU driver management
- About screen
- Share log

**Data Flow**:
- Direct intent launches to activities
- File pickers for folder/CIA selection
- Toast notifications for feedback

**WIRING GAPS**:
- CIA install calls TODO native method
- GPU driver management shows toast instead of navigating
- About navigation hardcoded instead of using navigation system
- No connection to GamesViewModel or other shared state
- No connection to settings system

---

### EmulationFragment
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/fragments/EmulationFragment.kt`
**Status**: COMPLETE

**Dependencies**:
- EmulationViewModel
- SettingsViewModel
- Game model (from intent/nav args)

**Functionality**:
- Game rendering surface
- Input handling (keyboard, gamepad, touch)
- Pause/resume
- Settings overlay menu
- Screenshot
- Performance monitoring
- Save/load states

**Data Flow**:
1. Receives Game object from activity intent
2. Initializes emulation via NativeLibrary
3. Handles all input events
4. Updates shader progress via EmulationViewModel

---

### CheatsFragment
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/features/cheats/ui/CheatsFragment.kt`
**Status**: COMPLETE

**Dependencies**:
- CheatsViewModel
- HomeViewModel
- CheatListFragment
- CheatDetailsFragment
- SlidingPaneLayout

**Functionality**:
- Two-pane layout (list + details)
- Cheat list display
- Cheat details editing
- Add/edit/delete cheats
- Cheat enabling/disabling

**Data Flow**:
1. Receives titleId from navigation args
2. Initializes CheatsViewModel with titleId
3. Loads cheats from CheatEngine
4. Displays in list and details panes
5. Saves on fragment close

---

### CheatListFragment
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/features/cheats/ui/CheatListFragment.kt`
**Status**: COMPLETE

**Dependencies**:
- CheatsViewModel
- CheatsAdapter

**Functionality**:
- List all cheats
- Select cheat
- Add new cheat button
- Enable/disable toggle

---

### CheatDetailsFragment
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/features/cheats/ui/CheatDetailsFragment.kt`
**Status**: COMPLETE

**Dependencies**:
- CheatsViewModel

**Functionality**:
- Edit cheat name, notes, code
- Code validation
- Save/delete buttons
- Cheat enabling

---

### SettingsFragment
**File**: `/home/user/Apps/PocketBoy/src/android/app/src/main/java/com/pocketboy/emulator/features/settings/ui/SettingsFragment.kt`
**Status**: COMPLETE

**Dependencies**:
- SettingsAdapter
- Settings system
- SettingsFragmentPresenter

**Functionality**:
- Settings tree/menu
- Per-setting UI widgets
- Settings loading/saving

---

### SetupFragment / FirstTimeSetupFragment
**Status**: COMPLETE
- First time setup wizard
- Directory selection

---

### SystemFilesFragment
**Status**: COMPLETE
- System file browser/manager

---

### DriverManagerFragment
**Status**: COMPLETE
- GPU driver list
- Driver selection
- Driver installation

---

### AboutFragment, LicensesFragment
**Status**: COMPLETE
- About information
- License display

---

## 4. SETTINGS SYSTEM

**Files**:
- Settings.kt - Main settings container
- BooleanSetting.kt - Boolean settings enum
- IntSetting.kt - Integer settings enum
- StringSetting.kt - String settings enum
- FloatSetting.kt - Float settings enum
- Abstract setting classes - Base interfaces

**Storage Mechanism**:
- SharedPreferences (Android standard)
- INI files via SettingsFile utility
- Per-game settings support

**Sections**:
- Core, System, Camera, Controls, Renderer, Layout, Utility, Audio, Debug, Theme, Storage
- Custom Landscape/Portrait layouts
- Performance Overlay

**Status**: COMPLETE - Fully functional settings system

**Integration Points**:
- SettingsActivity for UI
- SettingsViewModel holds Settings object
- BooleanSetting, IntSetting, StringSetting used throughout
- EmulationActivity, EmulationFragment access settings
- Settings changes affect emulation immediately or on restart (depends on isRuntimeEditable)

---

## 5. NAVIGATION PATTERN

**Navigation Files**:
- home_navigation.xml - MainActivity navigation
- emulation_navigation.xml - EmulationActivity navigation
- cheats_navigation.xml - CheatsActivity navigation

**Navigation System**:
- Uses AndroidX Navigation component
- Fragment destinations with arguments
- Global actions for shared transitions

**Data Flow Between Screens**:
1. **MainActivity** (hub):
   - Hosts GamesFragment, SearchFragment, HomeSettingsFragment, SetupFragment
   - Shares ViewModels: HomeViewModel, GamesViewModel, SettingsViewModel
   - Bottom nav switches between fragments

2. **Game Launch Flow**:
   - GamesFragment/SearchFragment → EmulationActivity intent
   - Game object passed via intent extra
   - EmulationActivity → EmulationFragment (receives game)

3. **Settings Flow**:
   - HomeSettingsFragment → SettingsActivity (separate activity)
   - SettingsActivity hosts SettingsFragment
   - Settings loaded on MainActivity.onCreate()

4. **Cheats Flow**:
   - EmulationFragment menu → CheatsActivity
   - CheatsActivity → CheatsFragment with titleId
   - CheatsFragment → CheatListFragment + CheatDetailsFragment (two-pane)

5. **Driver Flow**:
   - HomeSettingsFragment → DriverManagerFragment
   - DriverManagerFragment uses DriverViewModel

**DualScreenActivity Integration**:
- Separate from main navigation
- Hosts ThreeDSTopScreenFragment and ThreeDSBottomScreenFragment
- Needs to integrate with MainActivity's GamesViewModel, SettingsViewModel
- Screen switching via button clicks (not navigation system)

---

## 6. EXISTING FEATURES

### Complete Features:
1. **Game Library Management**
   - Game discovery from filesystem
   - Game list caching
   - Game sorting by title
   - Game filtering (system titles)
   - Game icon extraction and display

2. **Game Launching**
   - Intent-based game launch
   - Direct file launch
   - Installed app launch
   - Play time tracking

3. **Game Search**
   - Full-text search
   - String similarity matching (Jaccard, JaroWinkler)
   - Filter by: Recently added, Recently played, Downloads
   - Real-time search results

4. **Emulation**
   - Game rendering
   - Full input handling (keyboard, gamepad, motion)
   - Pause/resume
   - Screen swap
   - Frame limiting
   - Shader compilation progress
   - V-sync support

5. **Settings Management**
   - Core settings (CPU JIT, GPU driver, etc.)
   - System settings (New 3DS, LLE applets, etc.)
   - Camera settings
   - Control settings (button mapping)
   - Renderer settings (shader modes, filtering, etc.)
   - Layout settings (screen orientation, custom layouts)
   - Audio settings (stretching, realtime)
   - Debug settings
   - Per-game overrides

6. **GPU Driver Management**
   - List available drivers
   - Select active driver
   - Install custom drivers
   - Delete drivers
   - System GPU driver fallback

7. **Cheats System**
   - Load cheats for games
   - Create new cheats
   - Edit existing cheats
   - Enable/disable cheats
   - Code validation (Gateway format)
   - Save cheat changes

8. **System Files**
   - System file browser
   - NAND/system content management

9. **CIA Installation**
   - Select CIA files
   - Install to system
   - Progress tracking

10. **UI/UX**
    - Material Design 3
    - System insets handling
    - Dark/Light theme support
    - Animations (Material transitions, fade through)
    - Navigation bar with bottom menu

### Partial/Stub Features (Referenced but not fully implemented):
1. **Favorites System**
   - ThreeDSTopScreenFragment references `it.isFavorite` but property doesn't exist in Game class
   - SearchFragment references favorites filter but no backend

2. **Recent Games**
   - Implementation just takes first 6 games (not sorted by play time)
   - No LastPlayedTime tracking in current Game model

3. **Achievements**
   - No evidence of achievement system in codebase

---

## 7. WIRING GAPS

### CRITICAL GAPS:

1. **Game Model Missing Properties** (Game.kt)
   - ❌ `isFavorite: Boolean` - Referenced in ThreeDSTopScreenFragment, SearchFragment
   - ❌ `isRecent: Boolean` - Recent filtering implementation incomplete
   - ❌ Method to track/retrieve last played time
   - ❌ Method to mark/check favorites
   - **Impact**: Favorites and recent filters are broken

2. **ThreeDSTopScreenFragment ViewModel Isolation**
   - ❌ Creates own GamesViewModel instead of using shared instance
   - ❌ No access to HomeViewModel for navigation control
   - **Impact**: Data not synchronized with MainActivity, nav state inconsistent

3. **ThreeDSBottomScreenFragment Missing Wiring**
   - ❌ No SettingsViewModel access
   - ❌ No GamesViewModel access
   - ❌ CIA install calls TODO native method
   - ❌ GPU driver button shows toast instead of navigating
   - ❌ About button hardcoded navigation logic
   - **Impact**: Settings changes not reflected, navigation inconsistent

4. **DualScreenActivity Integration Issues**
   - ❌ Not connected to MainActivity's navigation system
   - ❌ Not sharing ViewModels with MainActivity
   - ❌ Separate game loading in ThreeDSTopScreenFragment
   - ❌ No way to return to MainActivity
   - **Impact**: DualScreenActivity is isolated from main app

5. **SharedPreferences vs Settings System Inconsistency**
   - ❌ Some settings use SharedPreferences directly (GameHelper.KEY_GAME_PATH)
   - ❌ Some use Settings class
   - ❌ SettingsViewModel not used for loading settings in DualScreenActivity
   - **Impact**: Settings changes might not propagate correctly

6. **Search Fragment Filter Issues**
   - ❌ "Favorites" filter references non-existent Game.isFavorite
   - ❌ "Recently played" doesn't actually sort by play time
   - ❌ "Downloads" filter has no implementation
   - **Impact**: Search filters don't work as expected

### MODERATE GAPS:

1. **EmulationFragment Not Connected to Cheats in DualScreen**
   - ❌ No cheats button/menu in dual-screen UI
   - ❌ No way to access cheats from game
   - **Impact**: Can't use cheats in dual-screen mode

2. **No Dark/Light Theme Toggle in DualScreen**
   - ❌ ThreeDSBottomScreenFragment has no theme settings
   - ❌ No reference to ThemeUtil
   - **Impact**: Can't change theme in dual-screen mode

3. **CIA Installation Not Wired**
   - ❌ ThreeDSBottomScreenFragment.installCiaFile() has TODO comment
   - ❌ No actual CIA installation method called
   - **Impact**: Can't install CIA files in dual-screen UI

4. **GPU Driver Management Not Wired**
   - ❌ ThreeDSBottomScreenFragment shows toast instead of navigating
   - ❌ No DriverViewModel access
   - **Impact**: Can't manage drivers in dual-screen UI

5. **Performance Overlay Settings**
   - ❌ No UI for performance overlay in dual-screen
   - ❌ No toggle in ThreeDSBottomScreenFragment
   - **Impact**: Can't enable performance monitoring in dual-screen

6. **No Search Feature in DualScreen**
   - ❌ ThreeDSTopScreenFragment.btnSearch is TODO
   - ❌ No SearchFragment in dual-screen layout
   - **Impact**: Can't search for games in dual-screen mode

7. **Play Time Tracking Not Stored**
   - ❌ Game model has keyLastPlayedTime but not used
   - ❌ No property to retrieve last played time
   - ❌ No sorting by play time for "Recent" games
   - **Impact**: Recent games just shows first 6 games

### ARCHITECTURAL GAPS:

1. **Dual-Screen Architecture Conflicts**
   - MainActivity uses navigation component + bottom nav
   - DualScreenActivity uses fragment containers + manual screen switching
   - No unified navigation system
   - **Impact**: Two different navigation patterns, hard to maintain

2. **Shared State Management Issues**
   - Each fragment can create its own ViewModels
   - ThreeDSTopScreenFragment creates new GamesViewModel
   - No central state holder for app-wide state
   - **Impact**: Data synchronization problems

3. **Settings Not Integrated with DualScreen**
   - SettingsActivity is separate
   - DualScreenActivity has no settings management UI
   - No way to save/load settings in dual-screen mode
   - **Impact**: Settings changes not accessible in dual-screen

4. **No Achievement System**
   - No achievement tracking in models
   - No UI for achievements
   - No integration with native library
   - **Impact**: Achievements feature missing entirely

---

## 8. CONNECTION REQUIREMENTS FOR DUAL-SCREEN UI

### ThreeDSTopScreenFragment Needs:
1. **Shared GamesViewModel** from activity instead of creating own
   - Use `activityViewModels()` instead of `viewModels()`
   - Access to games, searchedGames, isReloading, etc.

2. **Shared HomeViewModel**
   - Control navigation visibility
   - Update status bar visibility
   - Access to directory picker state

3. **Game Model Enhancement**
   - Add `isFavorite: Boolean` property
   - Add play time tracking properties
   - Methods to query favorites and recent

4. **SearchFragment Integration**
   - Open SearchFragment instead of TODO
   - Navigate through MainActivity's navigation system

5. **Settings Access**
   - Access SettingsViewModel
   - Read theme settings
   - Read layout settings

### ThreeDSBottomScreenFragment Needs:
1. **SettingsViewModel Connection**
   - Access Settings object
   - Load/save settings
   - Display current settings

2. **GamesViewModel Connection**
   - Reload games when folder changed
   - Update game list

3. **DriverViewModel Connection**
   - Navigate to driver manager
   - Access driver list and management

4. **Navigation System Integration**
   - Use proper navigation instead of TODO
   - Connect to MainActivity's navigation

5. **Native Library Wiring**
   - Call NativeLibrary methods for CIA installation
   - Proper error handling

6. **CheatEngine Integration**
   - Access cheats from current game
   - Display cheat UI

### DualScreenActivity Needs:
1. **ViewModel Sharing**
   - Access MainActivity's GamesViewModel, HomeViewModel, SettingsViewModel
   - Or create ActivityScope ViewModels

2. **Navigation Integration**
   - Route game launches to proper Activity
   - Handle intents correctly
   - Track back stack

3. **Intent Handling**
   - Receive game selections
   - Pass data to fragments properly

4. **Lifecycle Management**
   - Proper onCreate, onResume, onDestroy
   - State saving/restoration

---

## SUMMARY TABLE

| Component | Status | File Location | Issues |
|-----------|--------|----------------|--------|
| **ViewModels** |
| GamesViewModel | COMPLETE | viewmodel/GamesViewModel.kt | None |
| EmulationViewModel | COMPLETE | viewmodel/EmulationViewModel.kt | None |
| HomeViewModel | COMPLETE | viewmodel/HomeViewModel.kt | None |
| TaskViewModel | COMPLETE | viewmodel/TaskViewModel.kt | Generic utility |
| DriverViewModel | COMPLETE | viewmodel/DriverViewModel.kt | None |
| SettingsViewModel | PARTIAL | features/settings/model/SettingsViewModel.kt | Just container |
| CheatsViewModel | COMPLETE | features/cheats/model/CheatsViewModel.kt | None |
| **Activities** |
| MainActivity | COMPLETE | ui/main/MainActivity.kt | None |
| EmulationActivity | COMPLETE | activities/EmulationActivity.kt | None |
| SettingsActivity | COMPLETE | features/settings/ui/SettingsActivity.kt | None |
| CheatsActivity | COMPLETE | features/cheats/ui/CheatsActivity.kt | None |
| DualScreenActivity | PARTIAL | ui/activities/DualScreenActivity.kt | Many wiring gaps |
| **Fragments** |
| GamesFragment | COMPLETE | fragments/GamesFragment.kt | None |
| SearchFragment | PARTIAL | fragments/SearchFragment.kt | Filters broken |
| HomeSettingsFragment | COMPLETE | fragments/HomeSettingsFragment.kt | None |
| ThreeDSTopScreenFragment | PARTIAL | fragments/ThreeDSTopScreenFragment.kt | isFavorite missing |
| ThreeDSBottomScreenFragment | PARTIAL | fragments/ThreeDSBottomScreenFragment.kt | Many TODOs |
| EmulationFragment | COMPLETE | fragments/EmulationFragment.kt | None |
| CheatsFragment | COMPLETE | features/cheats/ui/CheatsFragment.kt | None |
| **Settings** | COMPLETE | features/settings/model/ | Fully implemented |
| **Navigation** | MIXED | res/navigation/ | DualScreen isolated |

