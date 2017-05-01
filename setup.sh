#!/bin/sh

echo "Setting permissions..."
chmod +x build_luaj.sh
chmod +x deploy.sh
chmod +x gradlew

echo "Setting up IntelliJ development environment with gradle..."
rm -rf build
./gradlew setupDecompWorkspace --refresh-dependencies
./gradlew cleanIdea idea

echo "Done."
