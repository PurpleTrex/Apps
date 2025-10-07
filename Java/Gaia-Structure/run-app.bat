@echo off
echo Starting Project Structure Creator...

REM Set JavaFX module path
set JAVAFX_PATH=C:\Users\purple\.m2\repository\org\openjfx

REM Run the application
java --module-path "%JAVAFX_PATH%\javafx-controls\19.0.2.1\javafx-controls-19.0.2.1-win.jar;%JAVAFX_PATH%\javafx-fxml\19.0.2.1\javafx-fxml-19.0.2.1-win.jar;%JAVAFX_PATH%\javafx-graphics\19.0.2.1\javafx-graphics-19.0.2.1-win.jar;%JAVAFX_PATH%\javafx-base\19.0.2.1\javafx-base-19.0.2.1-win.jar" --add-modules javafx.controls,javafx.fxml -jar "target\project-structure-creator-1.0.0.jar"

pause