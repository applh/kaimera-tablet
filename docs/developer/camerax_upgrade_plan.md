# CameraX 1.5+ Upgrade & Implementation Plan

This document outlines the strategy for leveraging modern features introduced in CameraX 1.4.0 and 1.5.x.

## 1. Version Upgrade (Completed)

*   **Status**: ✅ Updated to `1.5.2` in `build.gradle.kts`.
*   **Action**: Verify `./gradlew assembleDebug` passes.

## 2. Adoption of `camera-compose` (Completed)

CameraX 1.5.0+ stabilizes the `camera-compose` artifact, allowing us to remove the legacy `AndroidView` wrapping `PreviewView`.

*   **Status**: ✅ Migrated to `CameraXViewfinder` in v0.0.25.
*   **Goal**: Pure Compose UI without legacy View system bridging per [Android 15 guidance](https://developer.android.com/media/camera/camerax/compose).
*   **Steps**:
    1.  Add `implementation("androidx.camera:camera-compose:1.5.2")`.
    2.  Replace `AndroidView(factory = { PreviewView(...) })` with the new `Viewfinder` composable (or `Preview` if using the high-level API).
    3.  Connect the `SurfaceProvider` from `Viewfinder` to the `Preview` use case.
    4.  Verify rotation and aspect ratio handling (often improved in native Compose).

## 3. Known Issues & Blockers

### ⚠️ Preview Stabilization Conflict
*   **Issue**: Enabling `Preview.Builder.setPreviewStabilizationEnabled(true)` causes a **blank viewfinder** (black screen) on startup (Cold Start).
*   **Detection**: Verify by reverting the line: if viewfinder returns, this is the cause.
*   **Root Cause**: Device driver incompatibility or conflict with `Camera2Interop` settings (FPS ranges).
*   **Status**: Feature is **DEFERRED** until stability is proven. Do *not* enable blindly.

## 6. Phase C: Ultra HDR (Completed v0.0.28)

*   **Status**: ✅ Implemented 10-bit HDR checks and configuration in `CameraManager`.
*   **Goal**: Capture "Professional" quality images/video in 10-bit HDR (HLG10/JPEG_R).
*   **Steps**:
    1.  Check support: `querySupportedDynamicRanges()`.
    2.  Configure `ImageCapture` and `VideoCapture` with `DynamicRange.HDR_UNSPECIFIED_10_BIT`.
    3.  Fallback to SDR (8-bit) if unsupported.
    4.  Update file output formats if necessary (usually standard JPG/JPEG_R).

## 4. Advanced Video Features (Completed)

CameraX 1.5.0 formalized High Dynamic Range and High Frame Rate video.

*   **Status**: ✅ Implemented Native 60 FPS support via `setTargetFrameRate`.
*   **Goal**: Native 60fps/120fps recording without fragile workarounds.
*   **Steps**:
    1.  Resolve `Camera2CameraInfo` from `CameraInfo`.
    2.  Check `CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES`.
    3.  Only apply `CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE` if the device explicitly confirms support.

## 5. Stabilization (Completed)

*   **Status**: ✅ Enabled Preview and Video stabilization in v0.0.25+.
*   **Goal**: Reduce shakiness in handheld tablet video.
*   **Steps**:
    1.  Enable `Preview.Builder.setPreviewStabilizationEnabled(true)` **(BLOCKED: See Known Issues)**.
    2.  Enable `VideoCapture.Builder.setVideoStabilizationEnabled(true)`.
    3.  Check `CameraInfo.isPreviewStabilizationSupported`.

## Implementation Order

1.  **Phase A (Architecture)**: Migrate to `camera-compose` (High Refactor Risk, High Payoff).
2.  **Phase B (Visuals)**: Enable Preview Stabilization (Low Risk, "Free" Quality improvement).
3.  **Phase C (Formats)**: Implement Ultra HDR (Device Dependent).
