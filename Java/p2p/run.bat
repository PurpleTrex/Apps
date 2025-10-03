@echo off
echo P2P Messaging Application Run Script  
echo =====================================

echo Checking Java installation...
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not installed or not in PATH!
    echo Please install Java 17 or higher and add it to your PATH.
    pause
    exit /b 1
)

echo Checking Maven installation...
mvn -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven is not installed or not in PATH!
    echo Please install Apache Maven or use an IDE to run the application.
    pause
    exit /b 1
)

echo Creating logs directory...
if not exist "logs" mkdir logs

echo Starting P2P Messaging Application...
mvn javafx:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ======================================
    echo Failed to start application!
    echo ======================================
    echo Make sure you have built it first with: build.bat
    echo Or check if all dependencies are properly downloaded.
    pause
)