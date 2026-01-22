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

## Phase 4: Adaptive & Responsive UI (New)
*Goal: Ensure the camera experience is flawless across all tablet postures, split-screen modes, and floating windows.*

### 4.1 Liquid Layouts (AdaptiveCameraLayout)
- **Problem**: Current UI assumes a landscape-oriented rectangular window. In constrained floating windows (e.g., square or tall), controls overlap or become unusable.
- **Solution**: Implement a responsive state-machine that switches layout strategies based on window Aspect Ratio, not just Device Orientation.
    - **Landscape Window (> 1.2 AR)**: Standard "Sidebar" layout (Controls on Right).
    - **Portrait Window (< 0.8 AR)**: "Bottom Bar" layout (Controls at Bottom).
    - **Square/Compact Window**: specific "Compact" variation with collapsed menus.

### 4.2 Smart Viewfinder Scaling
- **Problem**: When resizing a window freely, the CameraX Preview surface may stretch or crop aggressively to fill usage, potentially misleading the user about the actual capture framing.
- **Feature**: Dynamic `ScaleType` switching.
    - **Default**: `FILL_CENTER` (Immersive, but potential cropping).
    - **Framing Priority**: When enabled (or in small windows), switch to `FIT_CENTER` (Letterboxing) to show the *exact* sensor output, ensuring 100% framing accuracy.

### 4.3 Dynamic Sizing
- **Refinement**: Replace fixed `dp` sizes for buttons and sliders with `weight` based or `%` based sizing in compact views to ensure all controls remain accessible even in 300x300dp micro-windows.

---

# UX Assessment: Floating Windows & Responsiveness

## 1. Current State Analysis
- **Layout Rigidity**: The current `CameraScreen.kt` heavily favors a landscape tablet experience. The sidebars are hard-coded to the right.
- **Window Resizing**: In split-screen or freeform mode, if the window becomes too narrow, the UI elements overlap the viewfinder, or the viewfinder crops awkwardly.
- **Aspect Ratio Mismatch**: Capturing a 4:3 photo in a 16:9 window (or vice-versa) relies on `FILL_CENTER`, which hides significant portions of the scene.

## 2. Identified Pain Points
1.  **"Squashed" Controls**: In 50/50 split screen (portrait orientation), the sidebar consumes ~20-30% of horizontal space, leaving a tall, thin strip for the camera preview.
2.  **Unreachable UI**: In multi-window mode, if the window is small, touch targets (sliders) may become too small or too close together.
3.  **Orientation Confusion**: Rotating the device while in a fixed-orientation floating window can cause icon rotation logic to detach from the actual window orientation.

## 3. Recommended Improvements (Prioritized)

### Priority A: Implement `AdaptiveCameraLayout` logic
**Effect**: High
**Effort**: Medium
Refactor `CameraScreen` to choose between `Row` (Landscape) and `Column` (Portrait) top-level containers based on `BoxWithConstraints` measurements. This single change fixes 80% of split-screen usability issues.

### Priority B: "Compact Mode" for Controls
**Effect**: Medium
**Effort**: Low
When `min(width, height) < 400dp`, automatically hide auxiliary controls (Focus Peaking, Grid toggles) behind a "More" fab or menu to declutter the viewfinder.

### Priority C: Viewfinder Letterboxing Option
**Effect**: High (for Pro users)
**Effort**: Low
Allow double-tapping the viewfinder to toggle between "Fill" (Immersive) and "Fit" (Accurate Framing). This is crucial for floating windows where the window aspect ratio rarely matches the sensor aspect ratio (4:3).

---

## Phase 5: Multi-Utility Expansion (v0.1.0 - In Progress)
*Goal: Expand the tablet's capabilities with essential productivity and utility applets.*

### 5.1 Browser Applet (v0.0.36 - Enhanced MVP)
- **MVP (Completed)**: Address bar, WebView integration, basic navigation controls (Back/Forward/Refresh).
- **Premium Polish (Completed)**: Progress bar, Home button, Persistent URL storage, and dedicated ViewModel.
- **Context Awareness (Completed)**: Context menu for Links, Images, and Videos (Copy, Download, Open).

### 5.2 Downloads Applet (v0.0.35 - MVP Completed)
- **MVP (Completed)**: List of downloaded files with "Open" and "Delete" actions.
- **Manual Download (v0.0.37)**: Added ability to manually trigger downloads via URL input.
- **Visuals (Completed)**: Storage usage indicator and file type icons.

### 5.3 Notes Applet (v0.0.29 - MVP Stub)
- **MVP (Completed)**: Simple text-based notes with auto-save and local search.
- **UI**: Minimalist, typography-focused "distraction-free" editor.

## Phase 6: System Synergy (v0.2.0+)
*Goal: Inter-applet communication and advanced features.*

### 6.1 Unified Search
- Launcher-level search that queries across Browser history, Downloads, and Notes.

### 6.2 Advanced Productivity
- **Files (v0.0.37)**: Robust "Rename" feature with scoped storage permission handling.
- **Tree Panel (v0.0.42)**: New hierarchical navigation component implemented for **Files**, **Notes**, and **Settings** applets to manage deep structures.
- **Core (v0.0.38)**: 
    - **Launcher (v0.0.47)**: Implemented "HoneyComb" Hexagonal Grid layout for main applet screen.
    - **APK Optimization**: Reduced debug size by 50% (131MB -> 66MB) via ABI filtering and Play Services ML Kit.
    - **AI Scene Fix**: Resolved async frame lifecycle bugs and enabled auto-model downloading.
- **Notes**: Markdown support and media attachments.
- **Browser**: Multi-tab support and Bookmarks.
- **Downloads**: Search and category-based filtering.

## Phase 7: Productivity Powerhouse (v0.3.0+)
*Goal: Deep project management and time tracking.*

### 7.1 Calendar (v0.0.50)
- **Visuals**: Modern, fluid calendar views (Month/Week).
- **Navigation**: **Tree Panel** for managing multiple calendar sources.
- **Interoperability**: Connect with Projects to show task deadlines.

### 7.2 Projects (v0.0.60 - MVP Completed)
- **Hierarchy (Completed)**: Space -> Project -> Task management via **Tree Panel**.
- **Execution (Completed)**: Kanban boards and task lists.
- **Synergy (Completed)**: Link Files and Notes directly to specific project nodes.
- **Visuals (Completed)**: Dashboards, filters, and team assignment simulation.

## Future Phases
- **Cloud Sync**: Integrate with Kaimera Cloud for multi-device support.
- **Notification Center**: System-wide alerts for deadlines and assignments.
