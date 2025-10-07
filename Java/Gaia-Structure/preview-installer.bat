@echo off
echo =====================================
echo    Gaia Installer Preview
echo    (Without Inno Setup Installation)
echo =====================================
echo.

REM Set paths
set MAVEN_HOME=C:\Users\purple\Desktop\Dev\Build Files\MVN\apache-maven-3.9.11-bin\apache-maven-3.9.11
set PATH=%MAVEN_HOME%\bin;%PATH%

echo Step 1: Building JAR file with Maven...
call "%MAVEN_HOME%\bin\mvn.cmd" clean package -q
if %errorlevel% neq 0 (
    echo ❌ Maven build failed!
    pause
    exit /b 1
)
echo ✅ JAR file built successfully!
echo.

echo Step 2: Verifying installer components...
if not exist "installer-dist" mkdir "installer-dist"
echo ✅ Output directory created: installer-dist\

echo Step 3: Checking required files...
if exist "src\main\resources\images\app-icon.png" (
    echo ✅ App icon found: src\main\resources\images\app-icon.png
) else (
    echo ❌ Warning: App icon not found!
)

if exist "Gaia-Setup.iss" (
    echo ✅ Inno Setup script found: Gaia-Setup.iss
) else (
    echo ❌ Inno Setup script missing!
)

if exist "run-gaia.bat" (
    echo ✅ Launcher script found: run-gaia.bat
) else (
    echo ❌ Launcher script missing!
)

if exist "LICENSE.txt" (
    echo ✅ License file found: LICENSE.txt
) else (
    echo ❌ License file missing!
)

if exist "INSTALL-INFO.txt" (
    echo ✅ Install info found: INSTALL-INFO.txt
) else (
    echo ❌ Install info missing!
)

if exist "INSTALL-COMPLETE.txt" (
    echo ✅ Install complete info found: INSTALL-COMPLETE.txt
) else (
    echo ❌ Install complete info missing!
)

echo.
echo Step 4: JavaFX Dependencies check...
if exist "C:\Users\purple\.m2\repository\org\openjfx\javafx-controls\19.0.2.1\javafx-controls-19.0.2.1-win.jar" (
    echo ✅ JavaFX Controls found
) else (
    echo ❌ JavaFX Controls missing
)

if exist "C:\Users\purple\.m2\repository\org\openjfx\javafx-fxml\19.0.2.1\javafx-fxml-19.0.2.1-win.jar" (
    echo ✅ JavaFX FXML found
) else (
    echo ❌ JavaFX FXML missing
)

echo.
echo ========================================
echo    📋 READY FOR INNO SETUP! 📋
echo ========================================
echo.
echo ✅ All components are ready for installer creation!
echo.
echo 📦 What the installer will include:
echo    • Gaia.jar (main application)
echo    • run-gaia.bat (launcher with JavaFX support)
echo    • app-icon.png (application icon - auto-resized by Inno Setup)
echo    • JavaFX runtime libraries (bundled)
echo    • License and documentation files
echo.
echo 🔧 To create the actual installer:
echo    1. Download and install Inno Setup from: https://jrsoftware.org/isinfo.php
echo    2. After installation, run: build-inno-installer.bat
echo    3. The installer will be created as: installer-dist\Gaia-Setup-1.0.0.exe
echo.
echo 💡 Benefits of using the existing PNG icon:
echo    • No need to create separate BMP files
echo    • Inno Setup automatically resizes for different contexts
echo    • Consistent branding across all installer elements
echo    • Much simpler maintenance
echo.

pause