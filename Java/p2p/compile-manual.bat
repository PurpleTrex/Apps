@echo off
echo P2P Messaging - Manual Compilation Script
echo =========================================
echo.
echo NOTE: This is a basic compilation script for development.
echo For production builds, please use Maven (build.bat) which handles dependencies automatically.
echo.

echo Checking Java installation...
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not installed or not in PATH!
    echo Please install Java 17 or higher and add it to your PATH.
    pause
    exit /b 1
)

echo Creating output directories...
if not exist "target\classes" mkdir target\classes
if not exist "logs" mkdir logs

echo.
echo Compiling Java files...
echo WARNING: This compilation requires JavaFX, Jackson, Netty, and other dependencies to be in classpath!
echo For automatic dependency management, use Maven with build.bat instead.
echo.

javac -d target\classes -cp "src\main\java" src\main\java\com\p2p\messaging\*.java src\main\java\com\p2p\messaging\model\*.java src\main\java\com\p2p\messaging\network\*.java src\main\java\com\p2p\messaging\ui\*.java

if %ERRORLEVEL% == 0 (
    echo.
    echo ======================================
    echo Basic compilation successful!
    echo ======================================
    echo NOTE: You still need to add all required dependencies to run the application.
    echo Recommend using Maven (build.bat) for proper dependency management.
) else (
    echo.
    echo ======================================
    echo Compilation failed!
    echo ======================================
    echo This is expected if dependencies are not in classpath.
    echo Please use Maven (build.bat) for automatic dependency resolution.
)

pause