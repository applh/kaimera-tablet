# Camera Applet Feature Roadmap

This document outlines the planned feature expansion for the Kaimera Tablet Camera Applet. The goal is to evolve the current MVP into a "Dynamic and Options-Rich" professional tool.

We follow an **Agile/MVP** methodology, delivering value in 2-week sprint-sized increments (Phases).

## Phase 1 & 2: Core Foundation & Dynamic Visuals (Completed v0.0.12+)
*Goal: Polish the core experience and ensure basic features expected of any camera app work flawlessly.*

### 1.1 Persistent Settings Architecture (Completed)
- **Problem**: Current settings (Grid, Timer) are lost when navigating away from the Settings screen.
- **Solution**: Implement `DataStore` or `SharedPreferences` repository to persist `CameraSettings`.
- **Integration**:
    - **Grid**: Customizable 0-10 lines for both horizontal and vertical axes (Slider).
    - **Timer**: Precise control via slider (0s to 100s).

### 1.2 Essential Controls (Completed)
- **Flash/Torch Toggle**:
    - UI: Add Flash icon (Auto/On/Off) to the top bar.
    - Logic: Bind to CameraX `ImageCapture.setFlashMode` and `CameraControl.enableTorch`.
- **Tap-to-Focus**:
    - UI: Visual indicator (circle) on touch.
    - Logic: Use `CameraControl.startFocusAndMetering` on tap coordinates.

### 1.3 Video Recording (New - v0.0.7)
- **Feature**: Start/Stop video recording with duration timer.
- **UI**: Compact toggle between Photo/Video modes.
- **Storage**: Saves to MediaStore (Movies/Kaimera).
- **Architecture**: Refactored into `CameraManager` and `CameraOverlays`.

## Phase 2: Dynamic Visuals & Experience
*Goal: Make the app feel alive and premium ("Dynamic").*

### 2.1 Creative Filters
- **Real-time Filters**: Apply color grading (B&W, Sepia, Cyberpunk, Vivid).
- **Implementation**: Use CameraX `CameraEffect` (if available on API level) or OpenGL-based `PreviewView` overlay.

### 2.2 Fluid UI Transitions
- **Orientation**: Smooth rotation of icons when tablet is rotated, instead of full screen rotation.
- **Mode Switching**: Animated transition between "Photo" and "Video" labels (sliding selector).

### 2.3 Hardware Integration
- **Volume Button Shutter**: Allow capturing photo/video using physical volume keys.
- **Haptic Feedback**: Subtle vibration on capture start/stop.

## Phase 3: Pro & "Options Rich" Features (In Progress)
*Goal: Appeal to power users.*

### 3.1 Pro Control Panel
- **Exposure Compensation (Completed)**: Slider to adjust +/- EV.
- **White Balance (Completed)**: Presets including Cloudy, Sunny, Incandescent, Fluorescent, and Auto.
- **Torch Control (Completed)**: Dedicated flashlight toggle for photos/video.

### 3.2 Advanced Modes
- **QR Code Scanner (Completed)**: Dedicated toggle in sidebar with real-time ML Kit detection.
- **Timelapse (Completed)**: Record video with frame intervals (v0.0.24).
- **Burst Mode (Completed)**: Gestures implemented for rapid capture.

### 3.3 Video Enhancements (Completed)
- **Frame Rate (Completed)**: Configurable 30/60 FPS (Native CameraX 1.5 check).
- **Stabilization (Completed)**: Preview and Video stabilization enabled.
- **Resolution**: Toggle between HD, FHD, 4K.

### 3.4 Vendor Extensions (Completed)
- **Feature**: Initial support for Vendor Extensions (HDR, Night, Bokeh, Face Retouch, Auto).
- **Implementation**: `ExtensionsManager` integration with dynamic availability check.

## Phase 4: Intelligence & Polishing
- **AI Scene Detection (Completed)**: Real-time subject labeling (Food, Nature, Portrait, etc.) with UI badge.
- **Gallery "Film Strip"**: Horizontal film strip of recent photos at the bottom of the camera view (Dynamic).

---

## Proposed MVP Sprint (Completed)

**Objective**: Complete Phase 1 to establish a solid base.

1.  **Refactor Architecture**: Introduce `UserPreferencesRepository` (DataStore). [Done]
2.  **Implement Flash & Torch**: Add UI and CameraX bindings. [Done]
3.  **Implement Tap-to-Focus**: Add touch listener and focus logic. [Done]
4.  **Connect Settings**: Verify Grid draws lines and Timer delays capture. [Done]
5.  **Video Recording**: Implement MediaStore logic and UI. [Done]
6.  **Modular Refactor**: Split logic into `CameraManager` and state-driven UI. [Done]
