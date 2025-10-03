@echo off
echo P2P Messaging Application Build Script
echo ======================================

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
    echo.
    echo Please install Apache Maven and add it to your PATH.
    echo You can download Maven from: https://maven.apache.org/download.cgi
    echo.
    echo Alternative: You can compile manually with javac if you have the dependencies.
    pause
    exit /b 1
)

echo Creating logs directory...
if not exist "logs" mkdir logs

echo Building application...
mvn clean compile

if %ERRORLEVEL% == 0 (
    echo.
    echo ======================================
    echo Build successful!
    echo ======================================
    echo You can now run the application with: run.bat
    echo Or package it with: mvn clean package
) else (
    echo.
    echo ======================================
    echo Build failed! 
    echo ======================================
    echo Please check the error messages above.
    pause
)