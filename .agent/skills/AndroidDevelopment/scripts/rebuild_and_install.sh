#!/bin/bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"

echo "Building and Installing Debug APK..."
./gradlew installDebug

if [ $? -eq 0 ]; then
    echo "Success! App installed."
    echo "To launch: adb shell am start -n com.kaimera.tablet/.MainActivity"
else
    echo "Build or Install failed."
    exit 1
fi
