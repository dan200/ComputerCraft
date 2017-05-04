echo "Setting up IntelliJ development environment with gradle..."
rmdir /s /q .\build
call gradlew.bat --stacktrace setupDecompWorkspace --refresh-dependencies
call gradlew.bat --stacktrace cleanIdea idea

echo "Done."
pause