# Architecture & Design Patterns

## Overview
The Kaimera Tablet application follows a modern, modular **Model-View-ViewModel (MVVM)** architecture, adapted for Jetpack Compose. It emphasizes separation of concerns, unidirectional data flow, and reactive state management.

## Architectural Layers

### 1. UI Layer (View)
- **Technology**: Jetpack Compose
- **Responsibility**: Renders the UI and reacts to state changes. It does *not* contain business logic.
- **Key Components**:
  - `MainActivity.kt`: The single Activity entry point hosting the Navigation graph.
  - `CameraScreen.kt`: Composable handling the Camera UI layout.
  - `CameraScreen.kt`: Composable handling the Camera UI layout.
  - `CameraLayouts.kt`: Implementation of the "Quad-Border Cockpit" (Top, Bottom, Side bars).
  - `CameraXViewfinder`: Native Compose viewfinder (replaced legacy PreviewView).
  - `CameraOverlays.kt`: Stateless composables for drawing visual aids (Grid, Level).
  - `LauncherScreen.kt`: The main dashboard.

### 2. Logic Layer (ViewModel / Manager)
- **Technology**: Kotlin Flow, Coroutines
- **Responsibility**: Manages state, handles business logic, and interacts with Repositories.
- **Key Components**:
  - `CameraManager.kt`: Acts as a ViewModel/Controller for CameraX. It encapsulates camera binding, capture logic, and device control, exposing `StateFlows` to the UI.
  - `*ViewModel.kt`: Standard ViewModels for other features (planned).

### 3. Data Layer (Model / Repository)
- **Technology**: DataStore, Room (future)
- **Responsibility**: Abstraction of data sources. Provides a clean API for data access.
- **Key Components**:
  - `UserPreferencesRepository.kt`: Handles persistent storage of user settings (Grid size, Resolution, etc.) using Jetpack DataStore.

## Design Patterns

### Repository Pattern
Used to abstract data sources. For example, `UserPreferencesRepository` hides the details of `DataStore` interaction, providing simple `Flow<T>` properties and `suspend` functions to the rest of the app.

### Observer Pattern
Heavily used via **Kotlin Flows** and **StateFlows**. The UI observes state changes (e.g., `cameraManager.zoomState.collectAsState()`) and reacts automatically, ensuring the UI always reflects the current data.

### Unidirectional Data Flow (UDF)
State flows down from the Logic Layer to the UI Layer. Events (user actions) flow up from the UI Layer to the Logic Layer.
- **State**: `ViewModel` -> `UI`
- **Events**: `UI` -> `ViewModel`

### Delegation
The `CameraScreen` delegates all complex camera operations to `CameraManager`. This keeps the UI code clean ("dumb UI") and focused solely on layout and rendering.

## Technology Stack & Dependencies

### Core
- **Kotlin**: 1.8+
- **Android SDK**: Compile/Target SDK 35

### Libraries

| Category | Library | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **UI** | **Jetpack Compose** | BOM 2024.06.00 | Reactive UI toolkit |
| | Material 3 | (via BOM) | Material Design components |
| | Activity Compose | 1.9.0 | Activity integration |
| | Navigation Compose | 2.7.7 | Navigation graph |
| | Icons Extended | 1.6.8 | Full material icon set |
| **Camera** | **CameraX Core** | 1.5.2 | Core camera logic |
| | CameraX Video | 1.5.2 | Video capture & Recorder |
| | CameraX Compose | 1.5.2 | Native Compose Viewfinder |
| | CameraX Extensions | 1.5.2 | Vendor extensions (HDR, Night) |
| **Image/Video** | **Coil** | 2.6.0 | Image loading (AsyncImage) |
| | Coil Video | 2.6.0 | Video thumbnail loading |
| **Storage** | **DataStore** | 1.0.0 | Type-safe preferences storage |
| **Permissions** | **Accompanist** | 0.34.0 | Permission handling in Compose |
| **Lifecycle** | Lifecycle Runtime KTX | 2.8.2 | Lifecycle-aware coroutines |

## Directory Structure
```
app/src/main/java/com/kaimera/tablet/
├── MainActivity.kt        // Entry point
├── camera/                // Camera logic
│   └── CameraManager.kt
├── data/                  // Data layer
│   └── UserPreferencesRepository.kt
├── ui/                    // UI Composable screens
│   ├── CameraScreen.kt
│   ├── CameraLayouts.kt
│   ├── CameraOverlays.kt
│   └── LauncherScreen.kt
└── theme/                 // App theme & styling
```
