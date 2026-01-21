# Lessons Learned & Troubleshooting

This document tracks non-obvious problems, technical constraints, and ergonomic decisions made during the development of the Kaimera Tablet Camera. It serves as "long-term memory" for future development.

## 1. Video Recording & Timelapse (CameraX)

### 1.1 Pulse Recording Strategy
*   **Problem**: Standard `VideoCapture` doesn't have a native "timelapse" mode that preserves high-frame-rate smoothness with slow playback.
*   **Solution**: Implemented a "Pulse" strategy using `resume()` and `pause()`.
*   **Critical Constraint**: The "on" pulse must be at least **300ms**. Shorter pulses (e.g., 50ms-100ms) often result in the video encoder failing to commit frames, leading to videos that only play the first few frames.
*   **Audio**: Disable audio for timelapse. Audio sync logic adds significant overhead and latent complexity to rapid pause/resume cycles.

### 1.2 Pulse Loop Reliability
*   **Safety**: Always check `isActive` within the coroutine loop to ensure the pulse job stops immediately when the recording is stopped.
*   **Dynamic Intervals**: Reading the interval within the loop (rather than passing it once) allows users to change the timelapse speed mid-recording.

## 2. UI & Ergonomics

### 2.1 Slider Rotation & Sizing (Compose)
*   **Problem**: Rotated sliders using `.graphicsLayer { rotationZ = -90f }` do not report their vertical height correctly to parent containers, often appearing "small" or compressed.
*   **Solution**: Use a custom `.layout` modifier to swap width and height constraints.
    ```kotlin
    .layout { measurable, constraints ->
        val placeable = measurable.measure(
            Constraints(
                minWidth = constraints.minHeight,
                maxWidth = constraints.maxHeight,
                minHeight = constraints.minWidth,
                maxHeight = constraints.maxWidth
            )
        )
        layout(placeable.height, placeable.width) {
            placeable.place(
                x = -(placeable.width - placeable.height) / 2,
                y = (placeable.width - placeable.height) / 2
            )
        }
    }
    ```

### 2.2 Tablet Accessibility
*   **30% Rule**: On large tablet screens (like Pad 2 Pro), controls positioned at the top or bottom of a sidebar can be outside a human hand's comfortable "reach zone." 
*   **Constraint**: The Zoom component is capped at **30% of the minimum window dimension** to ensure it stays within easy thumb reach.

## 3. Computer Vision (ML Kit)

### 3.1 Multiplexing Analyzers
*   **Problem**: CameraX's `setAnalyzer` takes a single analyzer. Running multiple features (QR and AI Scene Detection) requires coordination.
*   **Solution**: Implement a "Multiplexing Analyzer" in `CameraManager` that receives a single frame and passes it to multiple specialized analyzer classes (`QrCodeAnalyzer`, `SceneAnalyzer`) before closing the `ImageProxy`.
