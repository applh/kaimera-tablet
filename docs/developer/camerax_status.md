# CameraX Implementation Status & Roadmap

## 1. Current Implementation Status

### Core Architecture
- **Wrapper**: `CameraManager` wraps all CameraX interactions.
- **Lifecycle**: Bound to `LifecycleOwner` (Fragment/Activity).
- **Executor**: Dedicated single-thread executor for camera operations.
- **Upgrade Plan**: See [CameraX 1.5+ Upgrade Plan](camerax_upgrade_plan.md).


### UseCases Implemented

#### A. Preview
- **Status**: ✅ Implemented
- **Configuration**:
    - `ResolutionSelector` with `ResolutionStrategy` (HD, FHD, Highest).
    - `SurfaceProvider` attached to `PreviewView`.
- **Key Logic**: Re-bound dynamically when resolution settings change.

#### B. ImageCapture
- **Status**: ✅ Implemented
- **Configuration**:
    - `CAPTURE_MODE_MINIMIZE_LATENCY` for speed.
  - [x] Timelapse Mode (using Pulse Recording - v0.0.24) <!-- id: 50 -->
- [ ] Slow Motion (High Frame Rate) <!-- id: 51 -->
    - `FlashMode` integration (Auto/On/Off).
    - `JpegQuality` integration (1-100).
- **Output**: `MediaStore` (Pictures/Kaimera).

#### C. VideoCapture
- **Status**: ✅ Implemented
- **AI Scene Detection**: ✅ Implemented.
    - *Features*: High-confidence labeling (Food, Nature, Portrait, etc.) with UI badge.
- **Configuration**:
    - `Recorder` with `QualitySelector` (HD, FHD, UHD).
    - `MediaStoreOutputOptions` for saving.
- **Features**:
    - Build-in duration tracking via `VideoRecordEvent.Status`.
    - MP4 Output.

### Camera Control & State
- **Zoom**: ✅ `CameraControl.setZoomRatio`. `zoomState` and `maxZoomState` exposed as flows.
- **Focus**: ✅ `FocusMeteringAction` initiated via tap-to-focus.
- **Flash**: ✅ `ImageCapture.setFlashMode` (binding time) and dynamic update.
- **Lens**: ✅ Front/Back switching supported.

## 2. Missing CameraX Features

### UseCases
- **ImageAnalysis**: ✅ Implemented.
  - [x] Timelapse (v0.0.24) <!-- id: 68 -->
- [ ] Panorama <!-- id: 69 -->
    - *Features*: QR Code scanning via ML Kit.
- **Extensions**: ✅ Implemented.
    - *Features*: Auto, HDR, Night, Bokeh, Face Retouch (Availability depends on device vendor).

### Advanced Controls
- **Exposure Compensation**: ✅ Implemented.
    - *Features*: Manual offset (+/- EV) via slider in Pro mode.
- **Torch (Flashlight)**: ✅ Implemented.
    - *Features*: Constant light toggle in sidebar.
- **Auto-Focus Modes**: ❌ Continuous AF vs Auto vs Locked (currently just tap-to-meter).
- **Concurrent Cameras**: ❌ Multi-camera API (e.g., streaming front/back simultaneously).

## 3. Implementation Plan for Missing Features

### Priority 1: Video Quality Improvements
**Goal**: Higher fidelity video.
1.  ✅ Implement `VideoCapabilities` check.
2.  ✅ Allow fps configuration (30 vs 60 fps).
3.  ✅ Pause/Resume recording support.


### Priority 2: Exposure Control
**Goal**: Pro-level control.
1.  ✅ Read `ExposureState` from `CameraInfo`.
2.  ✅ Expose `exposureRange` (min/max/step) via StateFlow.
3.  ✅ Add `setExposure(index: Int)` to `CameraManager`.
4.  ✅ UI: Add explicit exposure slider in Pro mode.

### Priority 3: Vendor Extensions
**Goal**: Better low-light/HDR performance.
1.  ✅ Add `camera-extensions` dependency (already present).
2.  ✅ Check availability: `ExtensionsManager.getInstance(context)`.
3.  ✅ Wrap binding logic with `extensionsManager.getExtensionEnabledCameraSelector`.

### Priority 4: Image Analysis (QR/ML)
**Goal**: Enable "Smart" features.
1.  ✅ Add ML Kit dependency.
2.  ✅ Implement `ImageAnalysis` UseCase.
3.  ✅ Research/Add QR scanning logic.
