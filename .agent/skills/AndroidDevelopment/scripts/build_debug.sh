#!/bin/bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
./gradlew assembleDebug
echo "APK Location: app/build/outputs/apk/debug/app-debug.apk"
