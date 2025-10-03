NotePad (Android Compose App)
================================

Modern minimalist note taking application with an integrated triple‑tap triggered private (incognito) browser. Built using Jetpack Compose + Room.

DISCLAIMER
---------
The full feature parity with iOS Notes (handwriting, document scanning, shared collaboration, rich attachments, tagging, locking, folders, etc.) is beyond this initial implementation's scope. This project provides a solid, extensible core: CRUD notes, pinning, search (client side), and a hidden privacy browser that leaves no persistent history.

Key Features
------------
1. Create, edit, delete notes.
2. Pin / unpin (long‑press a note card or long-press logic simplified via long click mapping).
3. Real‑time search filtering.
4. Triple‑tap anywhere on the main screen (including blank space or search bar) to open an in‑app private browser.
5. Private Browser: No cookies, no cache, history cleared and WebView destroyed on exit.
6. Modern Material 3 UI (Compose) with adaptive theme stub (ready for dynamic or dark theming).

Architecture Overview
---------------------
Layered simple approach:
Data: Room (Entity Note, Dao NoteDao, Database NoteDatabase, Repository NoteRepository)
Presentation: ViewModels (AndroidViewModel for quick access to Application context), StateFlows for reactive UI.
UI: Jetpack Compose screens and dialogs; single-activity pattern for notes + separate PrivateBrowserActivity for stronger isolation and lifecycle-managed purge.

Triple Tap Gesture Logic
------------------------
Collect timestamps of taps (registerTap) and check if three taps occur within 600ms window. If yes, launch PrivateBrowserActivity.

Incognito Browser Details
-------------------------
WebView configured with:
 - JavaScript enabled (can be toggled for stricter privacy if desired)
 - DOM storage disabled
 - Cache disabled
 - Cookies disabled
On destroy: clearHistory(), clearCache(true), remove WebView from hierarchy, destroy(), removeAllCookies().
Address bar accepts either full URLs or search queries (DuckDuckGo). Simple heuristic: if contains a dot and no spaces treat as domain else run search.

Building / Running
------------------
Requirements: Android Studio Giraffe+ (AGP 8.3.0, Kotlin 1.9.22, compileSdk 34)

Steps:
1. Ensure you have an installed Android SDK (API 34) and JDK 17.
2. Open the project in Android Studio (Open existing project).
3. Let Gradle sync finish.
4. Run the app on an emulator or physical device (minSdk 24).

Gradle Wrapper
--------------
For brevity the wrapper JAR is not checked in (add via: gradlew wrapper --gradle-version 8.5). Then use: ./gradlew assembleDebug

Extending Toward iOS Notes Parity
---------------------------------
Suggested roadmap:
 - Folder organization & tags
 - Rich text / Markdown support
 - Image & file attachments (use SAF + Coil)
 - Checklists with interactive items
 - Biometric lock per note (BiometricPrompt + encrypted storage)
 - Version history (maintain revisions table)
 - Sync (e.g., with backend/Firebase) and multi-device real-time collaboration
 - Note sharing via OS share sheet
 - Widgets & Quick capture shortcuts
 - Handwriting / stylus support (Compose Canvas + ML Kit for text recognition)

Privacy & Security Hardening Ideas
----------------------------------
 - Add option to auto-lock after inactivity
 - In-memory encryption for note content (SQLCipher or Tink) when at rest
 - Screenshot blocking for private browser activity (FLAG_SECURE)
 - Tor / proxy integration for advanced privacy

Testing Strategy (Not yet added)
--------------------------------
Unit tests for Repository & Dao; UI tests with Robolectric / Compose UI tests for note CRUD flow; instrumentation test verifying WebView data cleared.

License
-------
Add your chosen license here (e.g., MIT, Apache 2.0).

Enjoy building on this foundation!
NotePad 

This application is a fully operable notepad app for android 16 to be used on android devices. 
This application should have all implimentation of notepad on ios.
It should feel modern, sharp, and have user experience at its forefront.

