# AI Scene Detection - Feature Details

AI Scene Detection leverages machine learning to enhance the camera's photography and productivity capabilities by understanding what is in the frame.

## 1. Objectives
- **Productivity**: Identify text, documents, and QR codes for immediate action.
- **Photography**: Optimize camera parameters based on the subject (e.g., Food, Landscape, Night).
- **User Guidance**: Provide real-time hints (e.g., "Too dark", "Move closer").

## 2. Technical Stack
- **Engine**: [Google ML Kit - Image Labeling](https://developers.google.com/ml-kit/vision/image-labeling) & Object Detection.
- **Pipeline**:
    - Uses CameraX `ImageAnalysis` UseCase.
    - Runs asynchronously on a dedicated thread (`cameraExecutor`).
    - Leverages NNAPI/GPU acceleration if available.
- **Architecture**:
    - `SceneAnalyzer`: An implementation of `ImageAnalysis.Analyzer`.
    - `StateFlow`: Results streamed to `CameraScreen` as a list of detected labels and confidence scores.

## 3. Planned "Scenes" & Behaviors
| Scene | Detection Logic | App Action |
|-------|-----------------|------------|
| **Document** | Edge detection / Text density | Show "Scan Document" button. |
| **Food** | Object classification (Food/Drink) | Increase saturation/warmth hints. |
| **Landscape** | Horizon detection / Sky ratio | Suggest "Panorama" or "Grid line" alignment. |
| **Low Light** | Average luma calculation | Prompt to toggle "Night Mode" extension. |
| **Portrait** | Face detection & depth estimation | Suggest "Bokeh" extension. |

## 4. UI/UX
- **Dynamic Hints**: A small, semi-transparent pill at the bottom or top center displaying the dominant scene (e.g., "🍽️ Food").
- **Optional Toggle**: A setting in the Camera Settings menu to enable/disable AI detection to save battery.
- **Prioritization**: When multiple scenes are detected (e.g., a Person eating Food), the system prioritizes the most actionable one (Person/Portrait).

## 5. Implementation Roadmap
1.  **Research**: Compare ML Kit Image Labeling vs custom TFLite models.
2.  **Prototype**: Implement a `SceneAnalyzer` that logs detected labels to console.
3.  **UI**: Build a "Scene Hint" Composable for the camera overlay.
4.  **Integration**: Link scene detection to auto-toggle Vendor Extensions (if supported).
