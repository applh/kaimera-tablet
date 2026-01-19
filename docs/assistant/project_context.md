# Assistant Context & Overview

## Project Identity
- **Name**: Kaimera Tablet
- **Target Device**: Xiaomi Pad 2 Pro
- **Tech Stack**: Kotlin, Jetpack Compose, Android SDK 34 (UpsideDownCake)

## Architecture
- **Single Activity**: `MainActivity` hosts the `NavHost`.
- **Navigation**: `androidx.navigation.compose`.
- **UI Structure**:
    - `LauncherScreen`: Entry point, grid of icons.
    - `CameraScreen`: Placeholder for camera function.
    - `SettingsScreen`: Placeholder for settings.
  
## Key Files
- `app/src/main/java/com/kaimera/tablet/MainActivity.kt`: Navigation entry.
- `app/src/main/java/com/kaimera/tablet/ui/LauncherScreen.kt`: Main menu.
- `app/build.gradle.kts`: Dependency management.

## Environment Quirks
- **JDK 17 STRICT**: System may have JDK 25 (incompatible). Always explicitly set `JAVA_HOME` to JDK 17 path.
- **Missing Resources**: If `res/` errors occur, check if assets were restored from `git`.

## Scripts
- `.agent/skills/AndroidDevelopment/scripts/build_debug.sh`: Safe build script.
