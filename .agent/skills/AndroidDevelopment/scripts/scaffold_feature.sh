#!/bin/bash

# Scaffold Feature Script
# Generates a new feature package with Screen, ViewModel, and Repository.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEMPLATE_DIR="$SCRIPT_DIR/../templates"
APP_MAIN_SRC="$SCRIPT_DIR/../../../../app/src/main/java"
PACKAGE_ROOT="com/kaimera/tablet"

if [ -z "$1" ]; then
    echo "Usage: $0 <FeatureName>"
    echo "Example: $0 UserProfile"
    exit 1
fi

FEATURE_NAME="$1"
# Convert FeatureName to lowercase package name (e.g., UserProfile -> userprofile)
FEATURE_PACKAGE_NAME=$(echo "$FEATURE_NAME" | tr '[:upper:]' '[:lower:]')

TARGET_DIR="$APP_MAIN_SRC/$PACKAGE_ROOT/$FEATURE_PACKAGE_NAME"
FULL_PACKAGE_NAME="com.kaimera.tablet.$FEATURE_PACKAGE_NAME"

echo "Scaffolding feature: $FEATURE_NAME"
echo "Package: $FULL_PACKAGE_NAME"
echo "Target Directory: $TARGET_DIR"

if [ -d "$TARGET_DIR" ]; then
    echo "Error: Directory $TARGET_DIR already exists."
    exit 1
fi

mkdir -p "$TARGET_DIR"

# Function to process template
process_template() {
    local template_file="$1"
    local output_file="$2"

    sed -e "s/\${FEATURE_NAME}/$FEATURE_NAME/g" \
        -e "s/\${PACKAGE_NAME}/$FULL_PACKAGE_NAME/g" \
        "$template_file" > "$output_file"
}

# Generate Files
process_template "$TEMPLATE_DIR/Screen.kt.template" "$TARGET_DIR/${FEATURE_NAME}Screen.kt"
process_template "$TEMPLATE_DIR/ViewModel.kt.template" "$TARGET_DIR/${FEATURE_NAME}ViewModel.kt"
process_template "$TEMPLATE_DIR/Repository.kt.template" "$TARGET_DIR/${FEATURE_NAME}Repository.kt"

echo "✅ Generated ${FEATURE_NAME}Screen.kt"
echo "✅ Generated ${FEATURE_NAME}ViewModel.kt"
echo "✅ Generated ${FEATURE_NAME}Repository.kt"
echo "Feature scaffolding complete!"
