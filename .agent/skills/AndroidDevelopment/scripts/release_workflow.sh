#!/bin/bash
set -e

if [ -z "$1" ] || [ -z "$2" ]; then
  echo "Usage: $0 <commit_message> <tag_version>"
  echo "Example: $0 \"Release version 1.0\" v1.0"
  exit 1
fi

MSG="$1"
TAG="$2"

echo "=== [1/7] Cleaning Build Artifacts ==="
./gradlew clean || echo "Gradle clean failed, forcing removal..."
rm -f *.log
rm -rf app/build .gradle/ .DS_Store captures/

echo "=== [2/7] Update Docs & Skills ==="
echo "REMINDER: Ensure you have updated all relevant documentation and skills."
echo "Check: docs/**/*.md"
echo "Check: .agent/skills/**/*.md"
# Wait for user confirmation or just proceed as a task
# read -p "Press enter to continue after checking docs/skills..."

echo "=== [3/7] Staging Changes ==="
git add .

echo "=== [4/7] Committing ==="
git commit -m "$MSG"

echo "=== [5/7] Tagging $TAG ==="
git tag -a "$TAG" -m "Release $TAG"

echo "=== [6/7] Pushing Changes to GitHub ==="
git push origin main

echo "=== [7/7] Pushing Tag to GitHub ==="
git push origin "$TAG"

echo "SUCCESS: Released $TAG and published to GitHub"
