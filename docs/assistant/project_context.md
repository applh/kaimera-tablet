# Assistant Context & Overview

## Project Identity
- **Name**: Kaimera Tablet
- **Target Device**: Xiaomi Pad 2 Pro
- **Current Version**: v0.1.1 (The World Update)
- **Tech Stack**: Kotlin, Jetpack Compose, CameraX 1.5.2, osmdroid 6.1.18, Room, Android SDK 35

## Architecture
- **Single Activity**: `MainActivity` hosts the `NavHost`.
- **Navigation**: `androidx.navigation.compose`.
- **UI Structure**:
    - `LauncherScreen`: Entry point, grid of icons.
    - `CameraScreen`: CameraX-based capture interface with Pro features.
    - `SettingsScreen`: App-wide and applet-specific settings with hierarchical **Tree Panel**.
    - `FilesScreen`: Media management with hierarchical **Tree Panel**.
    - `NotesScreen`: Distraction-free editor with hierarchical **Tree Panel**.
    - `TreePanel`: Reusable hierarchical navigation sidebar.
    - `ProjectsScreen`: Hierarchical Project Management with Room Persistence.
    - `MapsScreen`: OpenStreetMap-based interactive map using `osmdroid`.

## Key Files
- `app/src/main/java/com/kaimera/tablet/MainActivity.kt`: Navigation entry.
- `app/src/main/java/com/kaimera/tablet/features/launcher/LauncherScreen.kt`: Main menu.
- `app/src/main/java/com/kaimera/tablet/features/projects/ProjectsScreen.kt`: Projects UI.
- `app/src/main/java/com/kaimera/tablet/data/local/ProjectsDatabase.kt`: Room Database definition.
- `app/src/main/java/com/kaimera/tablet/data/repository/ProjectRepository.kt`: Data access layer.
- `app/src/main/java/com/kaimera/tablet/features/maps/data/MapsDatabase.kt`: Maps Room Database.
- `app/src/main/java/com/kaimera/tablet/features/maps/data/MapsRepository.kt`: Maps data access layer.
- `app/src/main/java/com/kaimera/tablet/features/maps/MapsScreen.kt`: OpenStreetMap view.
- `app/src/main/java/com/kaimera/tablet/core/ui/components/TreePanel.kt`: Reusable tree component.
- `app/build.gradle.kts`: Dependency management (includes CameraX, osmdroid, Coil, Room).

## Environment Quirks
- **JDK 17 STRICT**: System may have JDK 25 (incompatible). Always explicitly set `JAVA_HOME` to JDK 17 path.
- **Missing Resources**: If `res/` errors occur, check if assets were restored from `git`.

## Scripts
- `.agent/skills/AndroidDevelopment/scripts/build_debug.sh`: Safe build script.
