FROM eclipse-temurin:17-jdk-jammy

# Environment variables
ENV ANDROID_SDK_ROOT /opt/android-sdk
ENV CMDLINE_TOOLS_VERSION 11076708
# latest as of 2024-01 - check https://developer.android.com/studio#command-tools
ENV PATH ${PATH}:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools

# Install dependencies
RUN apt-get update && apt-get install -y \
    unzip \
    wget \
    git \
    libatomic1 \
    && rm -rf /var/lib/apt/lists/*

# Setup Android SDK
WORKDIR /opt
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools \
    && wget -q https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip -O android_tools.zip \
    && unzip -q android_tools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools \
    && mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest \
    && rm android_tools.zip

# Accept licenses
RUN yes | sdkmanager --licenses

# Install SDK packages
# Build Tools 34.0.0 is often a good default, but check project needs. 
# We saw compileSdk 35 in build.gradle.kts, so we need platform-35.
# Using build-tools 35.0.0 to ensure better ARM64 support.
RUN sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"

# --- Gradle Caching Layer ---

WORKDIR /app

# 1. Copy Gradle wrapper files first (to cache Gradle distribution download)
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/
COPY gradlew .

# Make gradlew executable
RUN chmod +x gradlew

# Download Gradle distribution (this will verify the wrapper works)
RUN ./gradlew --version

# 2. Copy build configuration files (to cache project dependencies)
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY app/build.gradle.kts app/

# 3. Skip pre-downloading dependencies to speed up environment setup.
# Dependencies will be cached in the 'kaimera-gradle-cache' volume during the first run.

# --- Source Code Layer (Optional if building in image) ---
# For a "Build Environment" image usually used in CI or with volume mounts, 
# we stop here. The source code will be mounted at runtime.
# This allows the image to be reused across commits without rebuilding.

CMD ["./gradlew", "assembleDebug"]
