# Kaimera Tablet Android App

This is a Kotlin-based Android application designed for the Xiaomi Pad 2 Pro, built using Jetpack Compose.

## Prerequisites

- **Android Studio** (Koala Feature Drop or newer recommended)
- **JDK 17** (Usually embedded with Android Studio)
- A **Xiaomi Pad 2 Pro** (or compatible Android tablet) for testing
- USB Cable for debugging

## Building the Project

1.  **Open in Android Studio**:
    - Launch Android Studio.
    - Select **Open** and navigate to the project root directory.
    - Allow Gradle to sync. This may take a few minutes as it downloads dependencies.

2.  **Build**:
    - Go to **Build > Make Project** (or press `Cmd + F9` on Mac).
    - Ensure the build completes successfully in the "Build" tab at the bottom.

## Deploying to Android Device

To run the app on your physical Xiaomi tablet:

### 1. Enable Developer Options
1.  Open **Settings** on your tablet.
2.  Go to **About tablet** (or **My Device** > **All specs**).
3.  Tap on **Build number** (or **OS Version**) 7 times rapidly until you see the message "You are now a developer!".

### 2. Enable USB Debugging
1.  Go back to **Settings** > **Additional settings** > **Developer options**.
2.  Toggle **USB debugging** to **ON**.
3.  (Optional) Enable "Install via USB" if requested.

### 3. Connect and Run
1.  Connect your tablet to your computer via USB.
2.  On the tablet, a prompt will appear: "Allow USB debugging?". Check "Always allow from this computer" and tap **OK**.
3.  In Android Studio, look at the device selection dropdown (top toolbar). You should see your Xiaomi device listed.
4.  Click the green **Run** button (Play icon) or press `Ctrl + R`.
5.  The app will compile, install, and launch automatically on your tablet.

## Troubleshooting

- **Device not found**: Ensure ADB is running. Try unplugging and replugging the cable, or switching USB modes (Transfer files / PTP).
- **Gradle Sync Failed**: Check your internet connection or proxy settings.
- **License not accepted**: Run `./gradlew androidDependencies` to see if you need to accept SDK licenses.

## CLI Build & Deploy

You can also build and deploy completely from the command line using the Gradle Wrapper.

### Build APK
To build the debug APK:
```bash
./gradlew assembleDebug
```
The APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`

### Install and Run
To build and immediately install onto a connected device:
```bash
./gradlew installDebug
```
Then start the app with adb:
```bash
adb shell am start -n com.kaimera.tablet/.MainActivity
```

## ADB Manual Install & Launch

If you prefer to install the APK manually using `adb`:

1.  **Check Device Connection**:
    ```bash
    adb devices
    ```
    Ensure your device is listed.

2.  **Install APK**:
    ```bash
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    ```
    The `-r` flag reinstalls the app if it's already present.

3.  **Launch App**:
    ```bash
    adb shell am start -n com.kaimera.tablet/.MainActivity
    ```

## Setup Synthesis

To set up the development environment correctly:

1.  **Java Requirement**: The project **strictly requires JDK 17**.
    - **Check version**: `java -version` (Must output 17.x)
    - **Install (Mac)**: `brew install openjdk@17`
    - **Fix "25.0.1" error**: If you see build failures related to Java 25, ensure `JAVA_HOME` points to JDK 17.

2.  **SDK Configuration**:
    - Ensure `local.properties` is created in the root directory.
    - Content: `sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk`

3.  **Troubleshooting**:
    - If `gradlew` is missing, download it from the [GitHub repo](https://github.com/applh/kaimera).
    - If resources are missing (`res/` errors), ensure you have cloned the full repository.
    
https://github.com/applh/kaimera
