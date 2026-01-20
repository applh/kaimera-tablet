# Developer Setup Guide

## Prerequisites

- **JDK 17**: This project strictly requires JDK 17.
    ```bash
    brew install openjdk@17
    ```
- **Android SDK**: Ensure `local.properties` points to your SDK.
    ```properties
    sdk.dir=/Users/lh/Library/Android/sdk
    ```
    *(Replace `lh` with your system username if different)*
- **Android Studio**: Recommended for Layout Inspector and device management.

## Building

Build the debug APK:
```bash
./agent/skills/AndroidDevelopment/scripts/build_debug.sh
```
Or manually:
```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
./gradlew assembleDebug
```

## Running

Deploy to connected device:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.kaimera.tablet/.MainActivity
```
