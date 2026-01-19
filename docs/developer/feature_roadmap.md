# Camera Applet Feature Roadmap

This document outlines the planned feature expansion for the Kaimera Tablet Camera Applet. The goal is to evolve the current MVP into a "Dynamic and Options-Rich" professional tool.

We follow an **Agile/MVP** methodology, delivering value in 2-week sprint-sized increments (Phases).

## Phase 1: Foundation & Usability (Completed v0.0.7+)
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

## Phase 3: Pro & "Options Rich" Features
*Goal: Appeal to power users.*

### 3.1 Pro Control Panel
- **Exposure Compensation**: Slider to adjust +/- EV.
- **White Balance**: Presets (Cloudy, Sunny, Incandescent, Fluorescent, Auto).
- **ISO/Shutter Priority**: (If supported by hardware/Camera2Interop) Monitor and display current values.

### 3.2 Advanced Modes
- **QR Code Scanner**: Dedicated mode or auto-detection overlay using ML Kit or ZXing.
- **Timelapse**: Record video with frame intervals.
- **Burst Mode**: Hold shutter for rapid capture.

## Phase 4: Intelligence & Polishing
- **AI Scene Detection**: Highlight text, faces, or objects.
- **Gallery "Film Strip"**: Horizontal film strip of recent photos at the bottom of the camera view (Dynamic).

---

## Proposed MVP Sprint (Completed)

**Objective**: Complete Phase 1 to establish a solid base.

1.  **Refactor Architecture**: Introduce `UserPreferencesRepository` (DataStore). [Done]
2.  **Implement Flash & Torch**: Add UI and CameraX bindings. [Done]
3.  **Implement Tap-to-Focus**: Add touch listener and focus logic. [Done]
4.  **Connect Settings**: Verify Grid draws lines and Timer delays capture. [Done]
5.  **Video Recording**: Implement MediaStore logic and UI. [Done]
