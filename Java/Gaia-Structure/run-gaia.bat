@echo off
REM Gaia Application Launcher
REM This batch file launches Gaia with the proper JavaFX configuration

setlocal

REM Set the application directory
set APP_DIR=%~dp0

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not found in PATH.
    echo.
    echo Please install Java 17 or later from:
    echo https://www.oracle.com/java/technologies/downloads/
    echo.
    echo Or ensure Java is added to your PATH environment variable.
    pause
    exit /b 1
)

REM Launch Gaia with JavaFX modules
echo Starting Gaia...
java --module-path "%APP_DIR%lib\javafx-controls-19.0.2.1-win.jar;%APP_DIR%lib\javafx-fxml-19.0.2.1-win.jar;%APP_DIR%lib\javafx-graphics-19.0.2.1-win.jar;%APP_DIR%lib\javafx-base-19.0.2.1-win.jar" --add-modules javafx.controls,javafx.fxml -jar "%APP_DIR%Gaia.jar" %*

REM Check if the application started successfully
if %errorlevel% neq 0 (
    echo.
    echo Error: Failed to start Gaia.
    echo.
    echo Possible solutions:
    echo 1. Ensure Java 17 or later is installed
    echo 2. Check that all required files are present
    echo 3. Try running as administrator
    echo.
    pause
)

endlocal