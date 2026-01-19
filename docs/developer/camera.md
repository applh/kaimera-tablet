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

## Features & Implementation Details

### 1. Camera Preview
The preview is rendered using a `PreviewView` inside an `AndroidView`. It is bound to the lifecycle of the composable's `vLifecycleOwner`.

### 2. Zoom Control
Zoom is managed via `CameraControl.setZoomRatio(float)`. A slider in the UI updates a state variable which triggers the camera control update.
- **Range**: 1x to 4x (hardcoded for safety, can be dynamic based on `CameraInfo.getZoomState()`).

### 3. Image Capture
Configuration:
- **Format**: JPEG
- **Storage**: `Pictures/Kaimera` directory using `MediaStore`.
- **Naming**: Timestamp-based (`yyyy-MM-dd-HH-mm-ss-SSS`).

### 4. Settings Integration
Camera settings are isolated in `CameraSettings.kt` but accessed via the main `SettingsScreen`.
- **Current Settings**:
    - **Grid Overlay**: Helper lines for composition.
    - **Timer**: 3s, 10s delay (Implementation pending in `CameraScreen`, UI present).

## Troubleshooting
- **Permission Denied**: The app requests `CAMERA` permission on first launch. If denied, a slate is shown. Clear app data to reset.
- **Preview Black**: Ensure the device is not being used by another camera app (adb).
- **Emulator**: CameraX works on standard Android emulators (Camera2 API supported).
