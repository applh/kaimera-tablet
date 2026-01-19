# Camera Applet Developer Guide

## Overview
The Camera Applet is a feature-rich camera interface built using **CameraX** and **Jetpack Compose**. It provides standard camera functionalities like preview, capture, zoom, and lens switching, along with a "Pro" style overlay.

## Architecture
The camera implementation is split into:
- **UI Layer**: `CameraScreen.kt` (Compose UI, controls, overlay).
- **Settings Layer**: `CameraSettings.kt` (Configuration for Grid, Timer, etc.).
- **Logic Layer**: Embedded directly in `CameraScreen` using `AndroidView` for the `PreviewView`.

## Key Dependencies
- **CameraX Core**: `androidx.camera:camera-core`
- **CameraX Camera2**: `androidx.camera:camera-camera2`
- **CameraX Lifecycle**: `androidx.camera:camera-lifecycle`
- **CameraX View**: `androidx.camera:camera-view`
- **CameraX Video**: `androidx.camera:camera-video`

## Features & Implementation Details

### 1. Camera Preview
The preview is rendered using a `PreviewView` inside an `AndroidView`. It is bound to the lifecycle of the composable's `vLifecycleOwner`.

### 2. Zoom Control
Zoom is managed via `CameraControl.setZoomRatio(float)`. 
- **Vertical Slider**: Located on the right side, height is dynamically set to 30% of screen height.
- **Range**: 1x to Device Max (dynamically retrieved via `CameraInfo.getZoomState().maxZoomRatio`).
- **Feedback**: Current zoom level displayed numerically above the slider.

### 3. Image Capture & Gallery
- **Capture**: Saves JPEGs to `Pictures/Kaimera` with timestamp naming.
- **Gallery Integration**: 
    - A circular thumbnail of the last captured image is displayed.
    - Clicking the thumbnail navigates to the **Files Applet** (Gallery View).
    - Image loading is handled by **Coil**.

### 4. Video Recording
- **UseCase**: `VideoCapture` with `Recorder` output.
- **Storage**: Saves MP4s to `Movies/Kaimera`.
- **Logic**:
    - **Mode Switching**: Toggles between Photo/Video states.
    - **Audio Permission**: Request `RECORD_AUDIO` at runtime.
    - **Recording**: Uses `FileOutputOptions` (or `MediaStoreOutputOptions` for Scoped Storage) to save video.

### 5. Settings Integration
Camera settings are isolated in `CameraSettings.kt` but accessed via the main `SettingsScreen`.
- **Current Settings**:
    - **Grid Overlay**: Helper lines for composition.
    - **Timer**: 3s, 10s delay (Implementation pending in `CameraScreen`, UI present).

## Troubleshooting
- **Permission Denied**: The app requests `CAMERA` permission on first launch. If denied, a slate is shown. Clear app data to reset.
- **Preview Black**: Ensure the device is not being used by another camera app (adb).
- **Emulator**: CameraX works on standard Android emulators (Camera2 API supported).
