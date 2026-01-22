# Assistant Context & Overview

## Project Identity
- **Name**: Kaimera Tablet
- **Target Device**: Xiaomi Pad 2 Pro
- **Tech Stack**: Kotlin, Jetpack Compose, CameraX 1.5.2, Android SDK 34 (UpsideDownCake)

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
  
## Key Files
- `app/src/main/java/com/kaimera/tablet/MainActivity.kt`: Navigation entry.
- `app/src/main/java/com/kaimera/tablet/features/launcher/LauncherScreen.kt`: Main menu.
- `app/src/main/java/com/kaimera/tablet/features/camera/CameraScreen.kt`: Main Camera UI.
- `app/src/main/java/com/kaimera/tablet/features/camera/CameraLayouts.kt`: Adaptive layout strategies.
- `app/src/main/java/com/kaimera/tablet/features/camera/CameraManager.kt`: CameraX logic controller.
- `app/src/main/java/com/kaimera/tablet/features/files/FilesScreen.kt`: Files applet with sidebar.
- `app/src/main/java/com/kaimera/tablet/features/notes/NotesScreen.kt`: Notes applet with sidebar.
- `app/src/main/java/com/kaimera/tablet/core/ui/components/TreePanel.kt`: Reusable tree component.
- `app/build.gradle.kts`: Dependency management (includes CameraX, Coil).

## Environment Quirks
- **JDK 17 STRICT**: System may have JDK 25 (incompatible). Always explicitly set `JAVA_HOME` to JDK 17 path.
- **Missing Resources**: If `res/` errors occur, check if assets were restored from `git`.

## Scripts
- `.agent/skills/AndroidDevelopment/scripts/build_debug.sh`: Safe build script.
