@echo off
echo Building Gaia Installation Package...
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

echo Step 2: Creating native executable with jpackage...
echo.

:: First create app-image (native executable)
echo Creating native application image...
jpackage ^
    --input target ^
    --name "Gaia" ^
    --main-jar "project-structure-creator-1.0.0.jar" ^
    --main-class "com.structurecreation.ProjectStructureCreatorApp" ^
    --type app-image ^
    --dest installer-output ^
    --app-version "1.0.0" ^
    --description "Gaia - Project Structure Creator" ^
    --vendor "Gaia Development Team" ^
    --copyright "Copyright (c) 2025 Gaia Development Team" ^
    --icon "src\main\resources\images\app-icon.png" ^
    --module-path "C:\Users\purple\.m2\repository\org\openjfx\javafx-controls\19.0.2.1\javafx-controls-19.0.2.1-win.jar;C:\Users\purple\.m2\repository\org\openjfx\javafx-fxml\19.0.2.1\javafx-fxml-19.0.2.1-win.jar;C:\Users\purple\.m2\repository\org\openjfx\javafx-graphics\19.0.2.1\javafx-graphics-19.0.2.1-win.jar;C:\Users\purple\.m2\repository\org\openjfx\javafx-base\19.0.2.1\javafx-base-19.0.2.1-win.jar" ^
    --add-modules javafx.controls,javafx.fxml

if %errorlevel% equ 0 (
    echo.
    echo ✅ Native executable created successfully!
    echo 📁 Location: installer-output\Gaia\
    echo 🚀 Executable: installer-output\Gaia\Gaia.exe
    echo.
    echo Now attempting to create MSI installer...
    echo.
    
    :: Create MSI installer from the app-image
    jpackage ^
        --app-image "installer-output\Gaia" ^
        --name "Gaia" ^
        --type msi ^
        --dest installer-output ^
        --app-version "1.0.0" ^
        --description "Gaia - Project Structure Creator" ^
        --vendor "Gaia Development Team" ^
        --copyright "Copyright (c) 2025 Gaia Development Team" ^
        --win-dir-chooser ^
        --win-menu ^
        --win-shortcut
        
    if %errorlevel% equ 0 (
        echo.
        echo ✅ SUCCESS! MSI installer created successfully!
        echo.
        echo 📦 Installer: installer-output\Gaia-1.0.0.msi
        echo 🚀 Executable: installer-output\Gaia\Gaia.exe
        echo.
        echo You now have:
        echo   • Native Windows executable (can run standalone)
        echo   • MSI installer package (for professional distribution)
        echo   • Start Menu shortcuts
        echo   • Desktop shortcut option
        echo   • Proper Windows integration
        echo   • Uninstaller
        echo.
    ) else (
        echo.
        echo ⚠️  MSI installer creation failed, but you have the native executable!
        echo 🚀 You can run: installer-output\Gaia\Gaia.exe
        echo.
        echo For MSI installer, you need WiX Toolset:
        echo 1. Download from: https://wixtoolset.org
        echo 2. Install WiX Toolset v3.11 or later
        echo 3. Add WiX to your PATH environment variable
        echo 4. Run this script again
        echo.
    )
) else (
    echo.
    echo ❌ Native executable creation failed!
    echo.
)

if %errorlevel% equ 0 (
    echo.
    echo ✅ SUCCESS! Installation package created successfully!
    echo.
    echo 📦 Output location: installer-output\
    echo 🚀 Installer file: Gaia-1.0.0.exe
    echo.
    echo The installer includes:
    echo   • Native Windows executable
    echo   • Start Menu shortcuts
    echo   • Desktop shortcut option
    echo   • Proper Windows integration
    echo   • Uninstaller
    echo.
) else (
    echo.
    echo ❌ jpackage failed!
    echo.
    echo Possible solutions:
    echo 1. Make sure you're using JDK 17 or later
    echo 2. Verify JavaFX modules are available
    echo 3. Check that the icon file exists
    echo.
)

pause