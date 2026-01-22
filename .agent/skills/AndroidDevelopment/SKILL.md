---
name: AndroidDevelopment
description: Comprehensive guide and scripts for building, testing, and deploying the Kaimera Tablet application.
---

# Android Development Skill

This skill provides the necessary commands and knowledge to work with the Kaimera Tablet Android project.

## Agent Persona: Senior Fullstack Engineer
When using this skill, adopt the persona of a *Senior Fullstack Engineer*.
- **Holistic View**: Consider the entire stack (Android Client, Backend APIs, Database, CI/CD).
- **Quality First**: Refuse to write "quick fixes". Insist on proper architecture and refactoring (e.g., separation of concerns).
- **Proactive**: Anticipate edge cases (network failure, permission denial, rotation) before they happen.
- **Mentorship**: Explain *why* a solution is chosen, referencing patterns like MVVM, Repository, or SOLID.

## Specialized Skills
For deep expertise in specific areas, refer to:
- [UXDesigner Skill](file:///Users/lh/Downloads/antig/kaimera-tablet/.agent/skills/UXDesigner/SKILL.md): Cyberpunk design system, HUD/UI standards.

- [BackendDevelopment Skill](file:///Users/lh/Downloads/antig/kaimera-tablet/.agent/skills/BackendDevelopment/SKILL.md): Persistence (Room/DataStore), Hilt DI, Clean Architecture.


## Environment Setup

- **JDK Version**: 17 (Strict requirement)
  - Install: `brew install openjdk@17`
  - Fix Build: Ensure `JAVA_HOME` points to JDK 17 if "25.0.1" error occurs.
- **Android SDK**:
  - `local.properties` must exist in root.
  - `sdk.dir=/Users/<USER>/Library/Android/sdk`
- **Permissions**:
  - `CAMERA` and `RECORD_AUDIO` (add to `AndroidManifest.xml`).
  - Scoped Storage (delete permission flow) for Files applet actions.
- **Gradle**:
  - Use `./gradlew` (wrapper provided in repo).

## Common Tasks

### 1. Build Debug APK
```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
./gradlew assembleDebug
```
**Output**: `app/build/outputs/apk/debug/app-debug.apk`

### 2. Install to Device
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 3. Launch Application
```bash
adb shell am start -n com.kaimera.tablet/.MainActivity
```

### 4. Run Tests
```bash
./gradlew testDebugUnitTest
```

### 5. Check Dependencies
```bash
./gradlew androidDependencies
```

### 6. Release Workflow
To cleanup, commit, and tag a release in one step:
```bash
.agent/skills/AndroidDevelopment/scripts/release_workflow.sh "Release message" v0.0.5
```

### 7. Dependency Management
**CRITICAL**: Before starting any major feature (especially involving media/system APIs), verify that you are using the latest stable versions of related libraries.
- Check [Maven Central](https://mvnrepository.com) or [Google Maven](https://maven.google.com).
- **Why?**: Major Android releases (like Android 15/API 35) often require corresponding library updates (e.g., CameraX 1.5.0+) to function correctly on new hardware.


## Troubleshooting

- **"Resource not found"**: Ensure you have pulled the full `res/` directory from git.
- **"JAVA_HOME is invalid"**: Verify `java -version` returns 17.x.

## Session Initialization
**REQUIRED**: At the start of every session, read the **Project Context** to understand the architecture and constraints.
- `view_file docs/assistant/project_context.md`

## Best Practices

### Adaptive & Responsive UX
Focus on creating flexible, screen-agnostic user interfaces that adapt to various form factors.

1.  **Avoid Hardcoded Dimensions**:
    - Use `Modifier.fillMaxSize()`, `Modifier.weight()`, or `BoxWithConstraints` instead of rigid `width/height` in DPs.
    - If fixed sizes are needed, define them in resource files (`dimens.xml`) based on screen configurations.

2.  **Flexible Layouts**:
    - Prefer `FlowRow`, `LazyVerticalGrid`, or responsive `Row/Column` combinations over static layouts.
    - Implement "Sliding Panes" or "Master-Detail" flows for larger screens using the hierarchical **`NavDrawerTreePanel`** component (located in `core.ui.components`) as the standard for all applets.


3.  **Density Independence**:
    - Use scalable units (`sp` for text).
    - Ensure touch targets are at least 48dp.

4.  **Rotation Handling**:
    - Even if the app is currently locked to landscape, build UI components that *can* rotate or reflow gracefully.
    - Use `Configuration.orientation` to conditionally swap layouts (e.g., `Row` for Landscape, `Column` for Portrait).

### Material Design & Aesthetics
Adopt a **Hybrid Approach**: Use Material 3 for velocity and accessibility, but customize heavily for the Cyberpunk/Sci-Fi aesthetic.

1.  **Hybrid Component Usage**:
    - **Standard UI** (Settings, Lists, Dialogs): Use standard Material 3 components (`Switch`, `Slider`, `Button`) for speed and accessibility.
    - **Hero UI** (HUD, Shutter, deeply custom elements): Build custom Composables from scratch using `Surface` and `Canvas` to achieve specific visual goals without fighting framework constraints.

2.  **Theming & Color**:
    - **Disable Dynamic Color**: Set `dynamicColor = false` in `Theme.kt` to prevent user wallpaper colors from clashing with the curated Cyberpunk palette.
    - **Custom Palette**: explicit neon accents (Cyan, Magenta) on dark backgrounds.

3.  **Visual Consistency**:
    - Use `MaterialTheme` typography and shapes where possible, but override for key headers or display text.

### Documentation Maintenance
**CRITICAL**: Always update the relevant documentation after any code change.

1.  **User Guide** (`docs/user/`): Update if features, UI, or installation steps change.
2.  **Developer Guide** (`docs/developer/`): Update if build requirements, dependencies, or scripts change.
3.  **Project Context** (`docs/assistant/`): Update if architecture, key files, or quirks change. This ensures future AI sessions remain efficient.

### Architecture & Standards (Feature-Based & DI)

We follow **MVVM** with **Hilt Dependency Injection** and **Feature-Based Packaging**.

1.  **Feature-Based Structure**: Group all files related to a feature together.
    - `com.kaimera.tablet.features.<feature_name>`
    - Contains: `Screen.kt`, `ViewModel.kt`, `FeatureSpecificUtils.kt`.
2.  **Core Package**: Shared code belongs in `com.kaimera.tablet.core`.
    - `core.data`: Repositories used by multiple features.
    - `core.ui`: Theme, Common UI components.
    - `core.di`: Hilt Modules for shared providers.
3.  **Hilt Dependency Injection**:
    - Annotate `Application` class with `@HiltAndroidApp`.
    - Annotate `MainActivity` with `@AndroidEntryPoint`.
    - Use `@HiltViewModel` for ViewModels and constructor injection for Repositories.
    - Use `hiltViewModel()` in Composables for scoped injection.

#### Scaffolding New Features (Applets)
To create a new applet (e.g., "Calendar"), follow these steps:

1.  **Generate Scaffold**:
    ```bash
    .agent/skills/AndroidDevelopment/scripts/scaffold_feature.sh "Calendar"
    ```
2.  **Define Route**: In `MainActivity.kt`, add the new route to the `NavHost`.
    ```kotlin
    composable("calendar") { CalendarScreen(onBack = { navController.popBackStack() }) }
    ```
3.  **Add to Launcher**: In `LauncherScreen.kt`, add a new `LauncherIcon` call.
    ```kotlin
    LauncherIcon(name = "Calendar", icon = Icons.Default.Event, onClick = { onAppletSelected("calendar") })
    ```
4.  **Hilt Provision**: If the new feature requires a repository, ensure it is provided in a Hilt `@Module` or annotated with `@Inject constructor`.

### Developer Mindset: The Kaimera Way
As a developer on Kaimera Tablet, you are building more than an app; you are building an *ecosystem*.

- **Composition over Inheritance**: Use Compose's strength to build small, testable UI pieces.
- **Dependency Inversion**: Always depend on abstractions (interfaces) rather than implementations.
- **Performance**: Android tablets may have constrained resources. Use `remember` and `derivedStateOf` effectively to minimize Recomposition.
- **Visual WOW**: Every applet should adhere to the high-aesthetic standards of the Kaimera UI (Gradients, Glassmorphism, Neon accents).

### Senior Standards (Updated)
1.  **Hilt for DI**: No more manual instantiation of Repositories in Activities/Screens.
2.  **StateFlow & UDF**: Use `collectAsStateWithLifecycle()` for robust state observation.
3.  **Modular Navigation**: Define navigation routes as constants or objects within their respective feature packages.

### Release Workflow
To cleanup, update documentation/skills, commit, and tag a release in one step, use the provided script:

```bash
.agent/skills/AndroidDevelopment/scripts/release_workflow.sh "<COMMIT_MESSAGE>" <TAG_VERSION>
```
**Example**:
```bash
.agent/skills/AndroidDevelopment/scripts/release_workflow.sh "Release version 0.0.1" v0.0.1
```
This script acts as a "Cleanup & Release" skill by:
1. Cleaning build artifacts (`./gradlew clean`).
2. **Pre-processing**: Reminds you to update all relevant `docs/` and `.agent/skills/` documentation.
3. Staging all changes (`git add .`).
4. Committing with the message.
5. Tagging with the version.
6. Pushing changes and tag to the GitHub repository.
