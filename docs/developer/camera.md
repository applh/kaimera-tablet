# Camera Applet Features & CameraX Implementation

## Overview
The Camera Applet is a high-performance camera interface designed for the Kaimera Tablet. It leverages **CameraX** for robust device compatibility and **Jetpack Compose** for a modern, reactive UI.

## Architecture
The implementation follows a modular MVVM-like pattern:

- **`CameraManager.kt`**: Encapsulates all CameraX interaction (UseCases, Binding, Capture, State). It exposes `StateFlows` for Zoom, Recording, and Flash status.
- **`CameraScreen.kt`**: The UI layer. It collects state from `CameraManager` and delegates user actions.
- **`CameraOverlays.kt`**: Dedicated Composable for drawing complex overlays (Grid, Level, Focus, Crosshairs).
- **`UserPreferencesRepository`**: Persistent DataStore for settings (Grid size, Resolution, Flash preference).

## Implemented CameraX Features

### 1. Camera Preview
- **Implementation**: `Preview` UseCase bound to `PreviewView`.
- **Dynamic Rebinding**: Preview is automatically re-bound when settings (resolution, lens) change.
- **Auto-Focus**: Tap-to-focus implemented using `FocusMeteringAction`.

### 2. Image Capture
- **UseCase**: `ImageCapture`
- **Output**: JPEG files saved to `Pictures/Kaimera`.
- **Features**: Configurable JPEG quality and resolution tiers.


### 3. Video Recording
- **UseCase**: `VideoCapture` with `Recorder`.
- **Output**: MP4 files saved to `Movies/Kaimera`.
- **Features**: 
    - Audio recording support.
    - Real-time duration counter.
    - **Configurable Frame Rate**: Supports 30 FPS and 60 FPS (if supported by hardware) via `Camera2Interop` AE Target FPS Range.
    - Pause/Resume support.

### 4. Hardware Controls
- **Zoom**: Vertical slider mapping to `CameraControl.setZoomRatio`. Supports pinch-to-zoom via `PreviewView` (if enabled) or manual slider input.
- **Flash**: Supports Off (Default), On, and Auto modes.
- **Lens Switch**: Seamless toggle between Front (Selfie) and Back (Main) cameras.

### 5. Vendor Extensions
- **Implementation**: Uses `ExtensionsManager` to query and apply vendor-specific modes.
- **Modes**: Supports HDR, Night, Bokeh, Face Retouch, and Auto (Availability depends on device manufacturer).
- **UI**: Displayed as a stars icon ✨ in the sidebar only when supported by the active lens.

### 6. Overlays & Composition
- **Grid Overlay**: Configurable horizontal/vertical lines for the "Rule of Thirds" or custom layouts.
- **Level Indicator**: Sensor-based (Gravity/Accelerometer) crosshairs that rotate to stay horizontal, helping users keep the tablet level.
- **Timer**: 3s, 10s, or custom delays for both photo and video capture.

### 7. Settings Integration
- **Resolution Tiers**: HD (720p), FHD (1080p - Default), and Max (Highest available).
- **Quality**: Adjustable JPEG compression level.
- **Circle Overlay**: Adjustable center circle radius for specific framing needs.

## Dependencies
```kotlin
implementation("androidx.camera:camera-core:1.4.0-alpha03")
implementation("androidx.camera:camera-camera2:1.4.0-alpha03")
implementation("androidx.camera:camera-lifecycle:1.4.0-alpha03")
implementation("androidx.camera:camera-video:1.4.0-alpha03")
implementation("androidx.camera:camera-view:1.4.0-alpha03")
```

## Future Roadmap
- [ ] Multi-camera concurrent preview (if hardware supports).
- [ ] RAW image capture.
- [ ] Manual exposure and ISO controls.
