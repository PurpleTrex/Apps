@echo off
echo =====================================
echo    Building Gaia Installer
echo    Using Inno Setup
echo =====================================
echo.

REM Set paths
set MAVEN_HOME=C:\Users\purple\Desktop\Dev\Build Files\MVN\apache-maven-3.9.11-bin\apache-maven-3.9.11
set PATH=%MAVEN_HOME%\bin;%PATH%

REM Set Inno Setup path
set INNO_SETUP="C:\Program Files (x86)\Inno Setup 6\iscc.exe"

REM Check if Inno Setup is installed
if not exist %INNO_SETUP% (
    echo ❌ Error: Inno Setup Compiler not found at: %INNO_SETUP%
    echo.
    echo Please verify Inno Setup 6 is installed at the expected location.
    echo.
    pause
    exit /b 1
)

echo ✅ Inno Setup found!
echo.

echo Step 1: Building JAR file with Maven...
call "%MAVEN_HOME%\bin\mvn.cmd" clean package -q
if %errorlevel% neq 0 (
    echo ❌ Maven build failed!
    pause
    exit /b 1
)
echo ✅ JAR file built successfully!
echo.

echo Step 2: Creating installer directories...
if not exist "installer-dist" mkdir "installer-dist"
echo ✅ Output directory created!

echo Step 3: Verifying icon file...
if exist "src\main\resources\images\app-icon.png" (
    echo ✅ App icon found: src\main\resources\images\app-icon.png
) else (
    echo ❌ Warning: App icon not found! Installer will use default icon.
)
echo.

echo Step 4: Compiling installer with Inno Setup...
echo.
%INNO_SETUP% "Gaia-Setup.iss"

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo    🎉 SUCCESS! 🎉
    echo ========================================
    echo.
    echo ✅ Gaia installer created successfully!
    echo.
    echo 📁 Location: installer-dist\Gaia-Setup-1.0.0.exe
    echo 📦 Size: 
    for %%I in ("installer-dist\Gaia-Setup-1.0.0.exe") do echo    %%~zI bytes
    echo.
    echo 🚀 The installer includes:
    echo    • Professional Windows installer wizard
    echo    • Java compatibility checking
    echo    • Start Menu shortcuts
    echo    • Optional desktop shortcut
    echo    • File associations for .gaia files
    echo    • Proper uninstaller
    echo    • Modern UI with custom branding
    echo.
    echo 💡 Next steps:
    echo    1. Test the installer: installer-dist\Gaia-Setup-1.0.0.exe
    echo    2. Distribute to users
    echo    3. The installer will guide users through installation
    echo.
    
    REM Try to open the output directory
    explorer "installer-dist" 2>nul
    
) else (
    echo.
    echo ❌ Inno Setup compilation failed!
    echo.
    echo Check the error messages above for details.
    echo Common issues:
    echo • Missing files referenced in the .iss script
    echo • Incorrect file paths
    echo • Inno Setup syntax errors
    echo.
)

pause