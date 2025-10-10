@echo off
echo ========================================
echo   Building Snippy Executable
echo ========================================
echo.

REM Check if PyInstaller is installed
C:/Users/purple/AppData/Local/Microsoft/WindowsApps/python3.10.exe -c "import PyInstaller" 2>NUL
if errorlevel 1 (
    echo PyInstaller not found. Installing...
    C:/Users/purple/AppData/Local/Microsoft/WindowsApps/python3.10.exe -m pip install pyinstaller==6.3.0
    echo.
)

echo Building executable...
echo This may take a few minutes...
echo.

REM Build using the spec file
C:/Users/purple/AppData/Local/Microsoft/WindowsApps/python3.10.exe -m PyInstaller --clean --noconfirm Snippy.spec

if errorlevel 1 (
    echo.
    echo ========================================
    echo   BUILD FAILED!
    echo ========================================
    echo Check the output above for errors.
    pause
    exit /b 1
)

echo.
echo ========================================
echo   BUILD SUCCESSFUL!
echo ========================================
echo.
echo Executable location:
echo   dist\Snippy.exe
echo.
echo You can now:
echo 1. Run dist\Snippy.exe directly
echo 2. Copy Snippy.exe anywhere you want
echo 3. Create a desktop shortcut
echo.
echo Note: No CMD window will appear!
echo.

REM Ask if user wants to run the exe
set /p RUN="Do you want to run Snippy.exe now? (Y/N): "
if /i "%RUN%"=="Y" (
    echo.
    echo Launching Snippy...
    start "" "dist\Snippy.exe"
)

echo.
echo Press any key to exit...
pause >nul
