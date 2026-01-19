---
name: AndroidDevelopment
description: Comprehensive guide and scripts for building, testing, and deploying the Kaimera Tablet application.
---

# Android Development Skill

This skill provides the necessary commands and knowledge to work with the Kaimera Tablet Android project.

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

## Troubleshooting

- **"Resource not found"**: Ensure you have pulled the full `res/` directory from git.
- **"JAVA_HOME is invalid"**: Verify `java -version` returns 17.x.

## Session Initialization
**REQUIRED**: At the start of every session, read the **Project Context** to understand the architecture and constraints.
- `view_file docs/assistant/project_context.md`

## Best Practices

### Documentation Maintenance
**CRITICAL**: Always update the relevant documentation after any code change.

1.  **User Guide** (`docs/user/`): Update if features, UI, or installation steps change.
2.  **Developer Guide** (`docs/developer/`): Update if build requirements, dependencies, or scripts change.
3.  **Project Context** (`docs/assistant/`): Update if architecture, key files, or quirks change. This ensures future AI sessions remain efficient.

### Architecture & Design Patterns

We follow the standard **Model-View-ViewModel (MVVM)** architecture with the **Repository Pattern**.

1.  **Screen (View)**: Composable functions (in `ui/` packages) that observe the `ViewModel`.
2.  **ViewModel**: Manages UI state (using `StateFlow`) and business logic.
3.  **Repository**: Abstracts data sources (API, Database).

#### Scaffolding New Features
Use the scaffolding script to generate standard boilerplate for a new feature:

```bash
.agent/skills/AndroidDevelopment/scripts/scaffold_feature.sh "FeatureName"
```

This will create:
- `FeatureNameScreen.kt`
- `FeatureNameViewModel.kt`
- `FeatureNameRepository.kt`

Make sure to register the new Screen in `MainActivity.kt` navigation.

### Release Workflow
To cleanup, commit, and tag a release in one step, use the provided script:

```bash
.agent/skills/AndroidDevelopment/scripts/release_workflow.sh "<COMMIT_MESSAGE>" <TAG_VERSION>
```
**Example**:
```bash
.agent/skills/AndroidDevelopment/scripts/release_workflow.sh "Release version 0.0.1" v0.0.1
```
This script acts as a "Cleanup & Release" skill by:
1. Cleaning build artifacts (`./gradlew clean`).
2. Staging all changes (`git add .`).
3. Committing with the message.
4. Tagging with the version.
5. Pushing changes and tag to remote (`git push origin`).
