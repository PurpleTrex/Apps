@echo off
echo Building Gaia Native Executable...
echo.

set JAVA_HOME=C:\Program Files\Java\jdk-21
set MAVEN_HOME=C:\Users\purple\Desktop\Dev\Build Files\MVN\apache-maven-3.9.11-bin\apache-maven-3.9.11
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo Step 1: Clean and build the application...
call "%MAVEN_HOME%\bin\mvn.cmd" clean package -q
if %errorlevel% neq 0 (
    echo Maven build failed!
    pause
    exit /b 1
)

echo Step 2: Creating native executable...
echo.

:: Create native executable (app-image)
jpackage ^
    --input target ^
    --name "Gaia" ^
    --main-jar "project-structure-creator-1.0.0.jar" ^
    --main-class "com.structurecreation.ProjectStructureCreatorApp" ^
    --type app-image ^
    --dest native-build ^
    --app-version "1.0.0" ^
    --description "Gaia - Project Structure Creator" ^
    --vendor "Gaia Development Team" ^
    --copyright "Copyright (c) 2025 Gaia Development Team" ^
    --icon "src\main\resources\images\app-icon.png" ^
    --module-path "C:\Users\purple\.m2\repository\org\openjfx\javafx-controls\19.0.2.1\javafx-controls-19.0.2.1-win.jar;C:\Users\purple\.m2\repository\org\openjfx\javafx-fxml\19.0.2.1\javafx-fxml-19.0.2.1-win.jar;C:\Users\purple\.m2\repository\org\openjfx\javafx-graphics\19.0.2.1\javafx-graphics-19.0.2.1-win.jar;C:\Users\purple\.m2\repository\org\openjfx\javafx-base\19.0.2.1\javafx-base-19.0.2.1-win.jar" ^
    --add-modules javafx.controls,javafx.fxml

if %errorlevel% equ 0 (
    echo.
    echo ✅ SUCCESS! Native executable created!
    echo.
    echo 📁 Location: native-build\Gaia\
    echo 🚀 Executable: native-build\Gaia\Gaia.exe
    echo.
    echo The executable includes:
    echo   • Bundled Java Runtime (no need to install Java)
    echo   • All JavaFX libraries included
    echo   • Native Windows executable
    echo   • App icon integration
    echo   • Can be distributed as a folder
    echo.
    echo 💡 Next steps:
    echo   1. Test by running: native-build\Gaia\Gaia.exe
    echo   2. For installer, install WiX Toolset and run build-installer.bat
    echo   3. Or use third-party tools like NSIS, Advanced Installer, etc.
    echo.
) else (
    echo.
    echo ❌ Native executable creation failed!
    echo.
    echo Possible issues:
    echo 1. JavaFX modules not accessible
    echo 2. Icon file not found
    echo 3. JAR file missing
    echo.
)

pause