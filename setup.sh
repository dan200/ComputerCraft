#!/bin/sh

echo "Setting permissions..."
chmod +x codesize.sh
chmod +x build_luaj.sh
chmod +x deploy.sh
chmod +x gradlew

echo "Setting up IntelliJ development environment with gradle..."
rm -rf build
./gradlew --stacktrace setupDecompWorkspace --refresh-dependencies
./gradlew --stacktrace cleanIdea idea

echo "Done."
