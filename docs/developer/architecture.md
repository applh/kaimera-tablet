# Architecture & Design Patterns

## Overview
The Kaimera Tablet application follows a modern, modular **Model-View-ViewModel (MVVM)** architecture, adapted for Jetpack Compose. It emphasizes separation of concerns, unidirectional data flow, and reactive state management.

## Architectural Layers

### 1. Feature Layer (Feature-Based Packaging)
The app is organized into high-level features to ensure scalability. Each feature encapsulates its own UI, Logic, and specific Data requirements.

- **`com.kaimera.tablet.features.camera`**: Core photography/videography functionality.
- **`com.kaimera.tablet.features.launcher`**: Central dashboard and applet navigation.
- **`com.kaimera.tablet.features.files`**: Media management and gallery viewing.
- **`com.kaimera.tablet.features.[browser|notes|downloads]`**: Planned expansion applets.

### 2. Core Layer (Infrastructure)
Shared components and base logic accessible by all features.

- **`com.kaimera.tablet.core.ui`**: Standard UI components, themes, and shared composables.
- **`com.kaimera.tablet.core.data`**: Shared repositories (e.g., `UserPreferencesRepository`) and DI modules.
- **`com.kaimera.tablet.core.di`**: Hilt modules defining dependency providers.

### 3. Structural Breakdown

- **Technology**: Jetpack Compose, Hilt (DI), Kotlin Flow
- **Pattern**: MVVM with Unidirectional Data Flow (UDF)
- **Key Components**:
  - `MainActivity.kt`: NavHost entry point.
  - `FeatureViewModel.kt`: Feature-scoped business logic and state management.
  - `FeatureScreen.kt`: Composable UI entry point for a feature.
  - `Repository.kt`: Data access abstraction.

## Design Patterns

### Dependency Injection (Hilt)
We use **Dagger Hilt** for dependency injection. This simplifies the provision of singleton resources (like `UserPreferencesRepository`) and makes ViewModels easier to test by injecting their dependencies.
- **`@HiltAndroidApp`**: Required on the `Application` class.
- **`@AndroidEntryPoint`**: Required on `MainActivity`.
- **`@HiltViewModel`**: Used for all ViewModels to enable constructor injection.

### Unidirectional Data Flow (UDF)
State flows down from the ViewModel to the UI. Events (user actions) flow up from the UI to the ViewModel.
- **State**: `ViewModel` -> `UI` (`StateFlow.collectAsStateWithLifecycle()`)
- **Events**: `UI` -> `ViewModel` (via function calls)

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
├── KaimeraApplication.kt  // Hilt @HiltAndroidApp
├── MainActivity.kt        // Entry point & Navigation
├── core/                  // Shared infrastructure
│   ├── data/              // Shared Repositories & DataSources
│   ├── di/                // Hilt Modules
│   └── ui/                // Shared Composables & Theme
└── features/              // Feature-specific code
    ├── camera/            // Camera feature (UI + Manager)
    ├── launcher/          // Launcher feature
    ├── files/             // Media gallery feature
    └── browser/           // Web browsing feature
```
