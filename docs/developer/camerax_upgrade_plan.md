# CameraX 1.5+ Upgrade & Implementation Plan

This document outlines the strategy for leveraging modern features introduced in CameraX 1.4.0 and 1.5.x.

## 1. Version Upgrade (Completed)

*   **Status**: ✅ Updated to `1.5.2` in `build.gradle.kts`.
*   **Action**: Verify `./gradlew assembleDebug` passes.

## 2. Adoption of `camera-compose`

CameraX 1.5.0+ stabilizes the `camera-compose` artifact, allowing us to remove the legacy `AndroidView` wrapping `PreviewView`.

*   **Goal**: Pure Compose UI without legacy View system bridging per [Android 15 guidance](https://developer.android.com/media/camera/camerax/compose).
*   **Steps**:
    1.  Add `implementation("androidx.camera:camera-compose:1.5.2")`.
    2.  Replace `AndroidView(factory = { PreviewView(...) })` with the new `Viewfinder` composable (or `Preview` if using the high-level API).
    3.  Connect the `SurfaceProvider` from `Viewfinder` to the `Preview` use case.
    4.  Verify rotation and aspect ratio handling (often improved in native Compose).

## 3. Ultra HDR & 10-bit Capture

Android 14/15 introduced Ultra HDR (JPEG_R), which CameraX 1.4+ supports natively.

*   **Goal**: Capture "Professional" quality images that shine on HDR displays (like Pad 2 Pro).
*   **Steps**:
    1.  Check support: `ImageCapture.Builder.setDynamicRange(DynamicRange.HDR_UNSPECIFIED_10_BIT)`.
    2.  Query `CameraInfo.querySession10BitDynamicRangeAvailability()`.
    3.  Fallback to SDR (8-bit) if unsupported.
    4.  Update file output formats if necessary (usually standard JPG/JPEG_R).

## 4. Advanced Video Features (Slow Motion / High FPS)

CameraX 1.5.0 formalized High Dynamic Range and High Frame Rate video.

*   **Goal**: Native 60fps/120fps recording without fragile workarounds.
*   **Steps**:
    1.  Use `QualitySelector.getCommonQualities(CameraInfo)` combined with `DynamicRange` checks.
    2.  Configure `Recorder` with a specific `FrameRate` range if hardware supports it.

## 5. Stabilization

*   **Goal**: Reduce shakiness in handheld tablet video.
*   **Steps**:
    1.  Enable `Preview.Builder.setPreviewStabilizationEnabled(true)`.
    2.  Enable `VideoCapture.Builder.setVideoStabilizationEnabled(true)`.
    3.  Check `CameraInfo.isPreviewStabilizationSupported`.

## Implementation Order

1.  **Phase A (Architecture)**: Migrate to `camera-compose` (High Refactor Risk, High Payoff).
2.  **Phase B (Visuals)**: Enable Preview Stabilization (Low Risk, "Free" Quality improvement).
3.  **Phase C (Formats)**: Implement Ultra HDR (Device Dependent).
