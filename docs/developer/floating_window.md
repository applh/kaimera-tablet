# Android Floating Window & Resizing Guide

## Overview
Android supports multi-window modes, including split-screen and freeform (floating window) modes. On devices like tablets and ChromeOS (or Android Desktop mode), users can freely resize application windows.

By default, the Kaimera Tablet app is resizeable (targeting SDK 35). However, resizing the window triggers a **configuration change**, which by default **destroys and recreates the Activity**. This can lead to:
-   Flickering or black screens during resize.
-   Camera preview interruptions.
-   Loss of transient UI state (if not saved).
-   "Weird" or jumpy behavior as the layout engine tries to keep up with rapid recreations.

## Smart Aspect Ratio & Resolution Strategy

To provide a high-quality "What You See Is What You Get" (WYSIWYG) experience, Kaimera Tablet dynamically adapts the camera configuration based on the **Window's Aspect Ratio**, not just the device's physical orientation.

### The Challenge: Black Bars vs. Cropping
If a user resizes the app window to a **Portrait** shape (e.g., 634x800, aspect ratio ~0.79) on a **Landscape** Tablet:
1.  **Forcing 16:9**: The camera would produce a wide image. To fit inside the tall window, it would require massive black bars (letterboxing).
2.  **Forcing 4:3**: A standard 4:3 image (aspect ratio 0.75) fits the 0.79 window almost perfectly.

### The Solution: Dynamic Aspect Ratio Selection
The `CameraManager` calculates the aspect ratio of the current window and selects the best matching strategy from the camera sensor:

| Window Shape | Window Ratio | Selected Strategy | Capture Resolution (Example) |
| :--- | :--- | :--- | :--- |
| **Fullscreen** (Landscape) | ~1.77 (16:9) | **16:9** | **3264x1836** (6MP) |
| **Split / Float** (Portrait) | ~0.79 (~3:4) | **4:3** | **3264x2448** (8MP) |

**Result**:
-   **Fullscreen**: Cinematic wide view.
-   **Portrait Window**: Full sensor readout. This fills the window and provides *higher* resolution (8MP vs 6MP) because it utilizes the full height of the sensor which is normally cropped out for 16:9 videos.

### Note on Resolution Numbers
You might notice debug logs or overlays reporting resolutions like **3264x2448** (Width > Height) even when you are in a "Portrait" window.
-   **Sensor Orientation**: Tablet camera sensors are mounted in Landscape. Their "Native" buffer is 3264(W) x 2448(H).
-   **Output File**: The image file is saved with the full buffer, but with a **Rotation Metadata** tag (e.g., 90°).
-   **Gallery Display**: The gallery reads this tag and displays the image vertically (2448x3264).

**This is NOT a bug.** It confirms that we are capturing the **raw, uncropped full sensor** data.

## Enabling/Disabling Multi-Window
Multi-window support is enabled by default for apps targeting Android 7.0 (API level 24) or higher.
To explicitly control this, use the `android:resizeableActivity` attribute in `AndroidManifest.xml`:

```xml
<activity
    android:name=".MainActivity"
    android:resizeableActivity="true" ... >
```

## Handling Resizing Smoothly

### The Problem: Activity Recreation
When a user resizes a freeform window, the system triggers multiple configuration changes (screen size, smallest screen width, etc.). If the app lets the system handle this, the Activity is torn down and restarted multiple times. This is the primary cause of "weird" resizing behavior.

### The Solution: Handling Configuration Changes Manually
To prevent Activity recreation and handle resizing dynamically (which Jetpack Compose does excellently), add `android:configChanges` to your Activity in `AndroidManifest.xml`:

```xml
<activity
    android:name=".MainActivity"
    android:configChanges="orientation|screenLayout|screenSize|smallestScreenSize|uiMode"
    ... >
```

**What this does:**
-   Tells Android: "I will handle these changes myself."
-   The Activity is **NOT** destroyed.
-   The Compose content automatically recomposes with the new window dimensions.
-   The resizing experience becomes smooth and fluid.

## Best Practices for Kaimera Tablet

### 1. Jetpack Compose & Responsive UI
Since this app uses Jetpack Compose, the UI is inherently reactive. Ensure your layouts do not rely on hardcoded screen dimensions.
-   Use `Modifier.fillMaxSize()`, `Modifier.weight()`, and `BoxWithConstraints` to adapt to available space.
-   Avoid assumptions about `Portrait` vs `Landscape` based solely on device rotation; check the window aspect ratio instead.

### 2. CameraX Considerations
The Camera preview is sensitive to window resizing.
-   **PreviewView**: If using `PreviewView`, it generally handles resizing well, but aspect ratio mismatches can occur.
-   **Aspect Ratio**: When the window aspect ratio changes (e.g., from 16:9 to 1:1), the camera use cases might need to be unbound and rebound with a new target aspect ratio if you strictly enforce it.
-   **Rotation**: Window rotation might not match device rotation in freeform mode. Always check `display.rotation` when configuring CameraX.

### 3. Testing
-   **Split Screen**: Long-press the app icon and select "Split screen".
-   **Freeform**: Enable "Enable freeform windows" in Developer Options, then launch the app in a floating window.
-   **Desktop Mode**: Connect a mouse/keyboard or use a foldable device to test dynamic resizing.

## Troubleshooting "Weird" Resizing
If the app flickers, restarts, or loses state when dragging the window corner:
1.  **Check Manifest**: Ensure `android:configChanges` includes `screenSize`, `smallestScreenSize`, `screenLayout`, and `orientation`.
2.  **Check ViewModels**: Ensure state is held in `ViewModel` or `rememberSaveable` so it persists if recreation *does* happen.
3.  **Camera Lag**: If the camera preview lags during resize, it's often because the surface is being destroyed. Handling config changes manually keeps the surface alive longer, though the preview might stretch momentarily before layout updates.
