# Docker Build Environment

This guide explains how to use the provided `Dockerfile` to create a consistent build environment for the Kaimera Tablet project. This ensures that builds are reproducible and do not depend on the host machine's specific configuration.

## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.

## Building the Image

Build the Docker image using the following command from the root of the project:

```bash
# For Apple Silicon / ARM64 Macs (Important!)
docker build --platform linux/amd64 -t kaimera-builder .

# For x86_64 / Intel
docker build -t kaimera-builder .
```

This process may take a few minutes the first time as it downloads the Android SDK and Gradle dependencies. Subsequent builds of the *image* will be much faster if `build.gradle.kts` files haven't changed, thanks to Docker layer caching.

## Running a Build

To build the Android application (APK) using the Docker container, run:

```bash
# For Apple Silicon / ARM64 Macs
docker run --rm --platform linux/amd64 \
    -v "$(pwd):/app" \
    -v "kaimera-gradle-cache:/home/gradle/.gradle" \
    kaimera-builder \
    ./gradlew assembleDebug

# For x86_64 / Intel
docker run --rm \
    -v "$(pwd):/app" \
    -v "kaimera-gradle-cache:/home/gradle/.gradle" \
    kaimera-builder \
    ./gradlew assembleDebug
```

### Command Breakdown:

- `--rm`: Automatically remove the container when it exits.
- `-v "$(pwd):/app"`: Mount the current directory (your source code) to `/app` inside the container. This allows Gradle to see your source code changes.
- `-v "kaimera-gradle-cache:/home/gradle/.gradle"`: (Optional but Recommended) Mount a Docker volume for the Gradle cache. This persists dependencies and build cache between *runs*, speeding up subsequent builds significantly.
- `kaimera-builder`: The name of the image we built.
- `./gradlew assembleDebug`: The command to run inside the container. You can replace this with any Gradle command (e.g., `./gradlew test`, `./gradlew lint`).

## Output

After the build completes, the APK will be available in your local `app/build/outputs/apk/debug/` directory, just as if you had run the build locally.

## Cleanup

To clean all build artifacts produced by Gradle:

```bash
docker run --rm --platform linux/amd64 \
    -v "$(pwd):/app" \
    -v "kaimera-gradle-cache:/home/gradle/.gradle" \
    kaimera-builder \
    ./gradlew clean
```

## Interactive Debugging

If you encounter build errors and need to investigate, you can start an interactive shell inside the container:

```bash
docker run --rm -it \
    -v "$(pwd):/app" \
    -v "kaimera-gradle-cache:/home/gradle/.gradle" \
    kaimera-builder \
    /bin/bash
```

Once inside, you can run Gradle commands manually to test specific tasks or check file states:

```bash
# Inside the container
./gradlew clean
./gradlew assembleDebug --stacktrace
```

## Troubleshooting

### Permissions Issues (Linux)
If you are running on Linux, you might encounter permission issues because the container runs as root by default. You can pass your user ID to the container:

```bash
docker run --rm \
    -u $(id -u):$(id -g) \
    -v "$(pwd):/app" \
    -v "kaimera-gradle-cache:/home/gradle/.gradle" \
    kaimera-builder \
    ./gradlew assembleDebug
```

### "SDK Location not found"
Ensure you are running the command from the root of the project so that `local.properties` (if it exists) doesn't conflict, or rely on the `ANDROID_HOME` environment variable set within the Dockerfile. The Dockerfile correctly sets `ANDROID_SDK_ROOT`, which Gradle respects.
