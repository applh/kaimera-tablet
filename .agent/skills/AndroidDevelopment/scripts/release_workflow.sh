#!/bin/bash
set -e

if [ -z "$1" ] || [ -z "$2" ]; then
  echo "Usage: $0 <commit_message> <tag_version>"
  echo "Example: $0 \"Release version 1.0\" v1.0"
  exit 1
fi

MSG="$1"
TAG="$2"

echo "=== [1/4] Cleaning Build Artifacts ==="
./gradlew clean || echo "Gradle clean failed, forcing removal..."
rm -f build.log
rm -rf app/build .gradle/ .DS_Store

echo "=== [2/4] Staging Changes ==="
git add .

echo "=== [3/4] Committing ==="
git commit -m "$MSG"

echo "=== [4/4] Tagging $TAG ==="
git tag -a "$TAG" -m "Release $TAG"


echo "=== [5/6] Pushing Changes ==="
git push origin main

echo "=== [6/6] Pushing Tag ==="
git push origin "$TAG"

echo "SUCCESS: Released $TAG and pushed to remote"
