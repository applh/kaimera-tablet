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

## Troubleshooting

- **"Resource not found"**: Ensure you have pulled the full `res/` directory from git.
- **"JAVA_HOME is invalid"**: Verify `java -version` returns 17.x.
