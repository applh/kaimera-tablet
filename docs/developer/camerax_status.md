# CameraX Implementation Status & Roadmap

## 1. Current Implementation Status

### Core Architecture
- **Wrapper**: `CameraManager` wraps all CameraX interactions.
- **Lifecycle**: Bound to `LifecycleOwner` (Fragment/Activity).
- **Executor**: Dedicated single-thread executor for camera operations.

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
    - `FlashMode` integration (Auto/On/Off).
    - `JpegQuality` integration (1-100).
- **Output**: `MediaStore` (Pictures/Kaimera).

#### C. VideoCapture
- **Status**: ✅ Implemented
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
- **ImageAnalysis**: ❌ Not implemented.
    - *Potential Uses*: QR Code scanning, Real-time ML, Histogram generation.
- **Extensions**: ❌ Not implemented.
    - *Potential Uses*: Auto-Night Mode, HDR, Face Retouch (Vendor dependent).

### Advanced Controls
- **Exposure Compensation**: ❌ `CameraControl.setExposureCompensationIndex`.
- **Torch (Flashlight)**: ❌ `CameraControl.enableTorch` (distinct from Flash Mode).
- **Auto-Focus Modes**: ❌ Continuous AF vs Auto vs Locked (currently just tap-to-meter).
- **Concurrent Cameras**: ❌ Multi-camera API (e.g., streaming front/back simultaneously).

## 3. Implementation Plan for Missing Features

### Priority 1: Video Quality Improvements
**Goal**: Higher fidelity video.
1.  ✅ Implement `VideoCapabilities` check.
2.  ✅ Allow fps configuration (30 vs 60 fps).
3.  ✅ Pause/Resume recording support.

### Priority 2: Image Analysis (QR/ML)
**Goal**: Enable "Smart" features.
1.  Add `ImageAnalysis` UseCase to `bindPreview`.
2.  Create `Analyzer` interface in `CameraManager`.
3.  Implement ZXing/MLKit analyzer for QR codes.

### Priority 2: Exposure Control
**Goal**: Pro-level control.
1.  Read `ExposureState` from `CameraInfo`.
2.  Expose `exposureRange` (min/max/step) via StateFlow.
3.  Add `setExposure(index: Int)` to `CameraManager`.
4.  UI: Add explicit exposure slider in Pro mode.

### Priority 4: Vendor Extensions
**Goal**: Better low-light/HDR performance.
1.  Add `camera-extensions` dependency (already present).
2.  Check availability: `ExtensionsManager.getInstance(context)`.
3.  Wrap binding logic with `extensionsManager.getExtensionEnabledCameraSelector`.
