[Setup]
; Basic application information
AppId={{8B5F9C2D-4A1E-4F3B-9A8C-5D2E7F1A6B9C}
AppName=Gaia
AppVersion=1.0.0
AppVerName=Gaia 1.0.0
AppPublisher=Gaia Development Team
AppPublisherURL=https://github.com/gaia-project
AppSupportURL=https://github.com/gaia-project/issues
AppUpdatesURL=https://github.com/gaia-project
AppCopyright=Copyright (C) 2025 Gaia Development Team

; Default installation directory
DefaultDirName={autopf}\Gaia
DefaultGroupName=Gaia
AllowNoIcons=yes

; Output settings
OutputDir=installer-dist
OutputBaseFilename=Gaia-Setup-1.0.0
SetupIconFile=src\main\resources\images\app-icon.png
UninstallDisplayIcon={app}\app-icon.png

; Compression
Compression=lzma
SolidCompression=yes

; Visual settings
WizardStyle=modern
; Use the existing app icon for wizard images (Inno Setup will auto-resize)
WizardImageFile=src\main\resources\images\app-icon.png
WizardSmallImageFile=src\main\resources\images\app-icon.png

; License and info files
LicenseFile=LICENSE.txt
InfoBeforeFile=INSTALL-INFO.txt
InfoAfterFile=INSTALL-COMPLETE.txt

; Requirements
MinVersion=10.0.17763
ArchitecturesAllowed=x64
ArchitecturesInstallIn64BitMode=x64

; Privileges
PrivilegesRequired=admin
PrivilegesRequiredOverridesAllowed=dialog

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "quicklaunchicon"; Description: "{cm:CreateQuickLaunchIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked; OnlyBelowVersion: 6.1; Check: not IsAdminInstallMode
Name: "associate"; Description: "Associate .gaia project files with Gaia"; GroupDescription: "File associations:"; Flags: unchecked

[Files]
; Main application JAR and dependencies
Source: "target\project-structure-creator-1.0.0.jar"; DestDir: "{app}"; DestName: "Gaia.jar"; Flags: ignoreversion
Source: "src\main\resources\images\app-icon.png"; DestDir: "{app}"; Flags: ignoreversion

; Batch file to run the application
Source: "run-gaia.bat"; DestDir: "{app}"; Flags: ignoreversion

; JavaFX runtime libraries (if bundling)
Source: "C:\Users\purple\.m2\repository\org\openjfx\javafx-controls\19.0.2.1\javafx-controls-19.0.2.1-win.jar"; DestDir: "{app}\lib"; Flags: ignoreversion
Source: "C:\Users\purple\.m2\repository\org\openjfx\javafx-fxml\19.0.2.1\javafx-fxml-19.0.2.1-win.jar"; DestDir: "{app}\lib"; Flags: ignoreversion
Source: "C:\Users\purple\.m2\repository\org\openjfx\javafx-graphics\19.0.2.1\javafx-graphics-19.0.2.1-win.jar"; DestDir: "{app}\lib"; Flags: ignoreversion
Source: "C:\Users\purple\.m2\repository\org\openjfx\javafx-base\19.0.2.1\javafx-base-19.0.2.1-win.jar"; DestDir: "{app}\lib"; Flags: ignoreversion

; Documentation
Source: "README.md"; DestDir: "{app}"; DestName: "README.txt"; Flags: ignoreversion isreadme

[Icons]
; Start Menu
Name: "{group}\Gaia"; Filename: "{app}\run-gaia.bat"; IconFilename: "{app}\app-icon.png"; WorkingDir: "{app}"; Comment: "Gaia - Project Structure Creator"
Name: "{group}\{cm:UninstallProgram,Gaia}"; Filename: "{uninstallexe}"

; Desktop icon (optional)
Name: "{autodesktop}\Gaia"; Filename: "{app}\run-gaia.bat"; IconFilename: "{app}\app-icon.png"; WorkingDir: "{app}"; Tasks: desktopicon; Comment: "Gaia - Project Structure Creator"

; Quick Launch (Windows 7 and earlier)
Name: "{userappdata}\Microsoft\Internet Explorer\Quick Launch\Gaia"; Filename: "{app}\run-gaia.bat"; IconFilename: "{app}\app-icon.png"; WorkingDir: "{app}"; Tasks: quicklaunchicon

[Registry]
; File associations
Root: HKA; Subkey: "Software\Classes\.gaia"; ValueType: string; ValueName: ""; ValueData: "GaiaProjectFile"; Flags: uninsdeletevalue; Tasks: associate
Root: HKA; Subkey: "Software\Classes\GaiaProjectFile"; ValueType: string; ValueName: ""; ValueData: "Gaia Project File"; Flags: uninsdeletekey; Tasks: associate
Root: HKA; Subkey: "Software\Classes\GaiaProjectFile\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\app-icon.png"; Tasks: associate
Root: HKA; Subkey: "Software\Classes\GaiaProjectFile\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\run-gaia.bat"" ""%1"""; Tasks: associate

; Add to Windows "Open with" context menu
Root: HKA; Subkey: "Software\Classes\*\shell\Open with Gaia"; ValueType: string; ValueName: ""; ValueData: "Open with Gaia"; Flags: uninsdeletekey; Tasks: associate
Root: HKA; Subkey: "Software\Classes\*\shell\Open with Gaia\command"; ValueType: string; ValueName: ""; ValueData: """{app}\run-gaia.bat"" ""%1"""; Tasks: associate

[Run]
; Option to launch application after installation
Filename: "{app}\run-gaia.bat"; Description: "{cm:LaunchProgram,Gaia}"; Flags: nowait postinstall skipifsilent; WorkingDir: "{app}"

[UninstallDelete]
; Clean up any created files
Type: files; Name: "{app}\*.log"
Type: files; Name: "{app}\*.tmp"

[Code]
// Check if Java is installed
function IsJavaInstalled: Boolean;
var
  JavaVersion: String;
begin
  Result := RegQueryStringValue(HKLM, 'SOFTWARE\JavaSoft\JRE', 'CurrentVersion', JavaVersion) or
            RegQueryStringValue(HKLM, 'SOFTWARE\JavaSoft\JDK', 'CurrentVersion', JavaVersion) or
            RegQueryStringValue(HKLM, 'SOFTWARE\JavaSoft\Java Runtime Environment', 'CurrentVersion', JavaVersion) or
            RegQueryStringValue(HKLM, 'SOFTWARE\JavaSoft\Java Development Kit', 'CurrentVersion', JavaVersion);
end;

// Check Java version during installation
function InitializeSetup(): Boolean;
begin
  Result := True;
  
  if not IsJavaInstalled then
  begin
    if MsgBox('Java Runtime Environment (JRE) 17 or later is required to run Gaia.' + #13#13 + 
              'Would you like to continue with the installation?' + #13 + 
              'You can download Java from: https://www.oracle.com/java/technologies/downloads/', 
              mbConfirmation, MB_YESNO) = IDNO then
    begin
      Result := False;
    end;
  end;
end;

// Custom welcome message
procedure InitializeWizard;
begin
  WizardForm.WelcomeLabel2.Caption := 
    'This will install Gaia version 1.0.0 on your computer.' + #13#13 +
    'Gaia is a powerful project structure creator with automated dependency management.' + #13#13 +
    'It is recommended that you close all other applications before continuing.';
end;

// Post-installation tasks
procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssPostInstall then
  begin
    // Create additional shortcuts or perform cleanup
    // This section can be expanded as needed
  end;
end;