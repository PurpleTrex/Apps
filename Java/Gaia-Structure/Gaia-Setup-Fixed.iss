; Script for Gaia - Fixed version
; Generated and modified for proper file handling

#define MyAppName "Gaia"
#define MyAppVersion "1.0"
#define MyAppPublisher "PurpleApps"
#define MyAppExeName "Gaia.vbs"
#define MyAppAssocName MyAppName + " Project File"
#define MyAppAssocExt ".gaia"
#define MyAppAssocKey StringChange(MyAppAssocName, " ", "") + MyAppAssocExt

[Setup]
AppId={{F951BA6B-3D0D-431B-B70A-C48169F69F6D}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\{#MyAppName}
ChangesAssociations=yes
DisableProgramGroupPage=yes
LicenseFile=C:\Users\purple\Desktop\Dev\Java\StructureCreation\LICENSE.txt
InfoBeforeFile=C:\Users\purple\Desktop\Dev\Java\StructureCreation\INSTALL-INFO.txt
InfoAfterFile=C:\Users\purple\Desktop\Dev\Java\StructureCreation\INSTALL-COMPLETE.txt
OutputDir=C:\Users\purple\Desktop\Dev\Java\StructureCreation\installer-dist
OutputBaseFilename=Gaia-Setup-1.0.0
SetupIconFile=C:\Users\purple\Desktop\Dev\Java\StructureCreation\src\main\resources\images\app-icon.ico
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "german"; MessagesFile: "compiler:Languages\German.isl"
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; Main launcher scripts
Source: "C:\Users\purple\Desktop\Dev\Java\StructureCreation\{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\Users\purple\Desktop\Dev\Java\StructureCreation\run-gaia.bat"; DestDir: "{app}"; Flags: ignoreversion

; Main JAR file - renamed to what the launcher expects
Source: "C:\Users\purple\Desktop\Dev\Java\StructureCreation\target\project-structure-creator-1.0.0.jar"; DestDir: "{app}"; DestName: "Gaia.jar"; Flags: ignoreversion

; JavaFX Libraries - copy to lib subfolder
Source: "C:\Users\purple\.m2\repository\org\openjfx\javafx-controls\19.0.2.1\javafx-controls-19.0.2.1-win.jar"; DestDir: "{app}\lib"; Flags: ignoreversion
Source: "C:\Users\purple\.m2\repository\org\openjfx\javafx-fxml\19.0.2.1\javafx-fxml-19.0.2.1-win.jar"; DestDir: "{app}\lib"; Flags: ignoreversion
Source: "C:\Users\purple\.m2\repository\org\openjfx\javafx-graphics\19.0.2.1\javafx-graphics-19.0.2.1-win.jar"; DestDir: "{app}\lib"; Flags: ignoreversion
Source: "C:\Users\purple\.m2\repository\org\openjfx\javafx-base\19.0.2.1\javafx-base-19.0.2.1-win.jar"; DestDir: "{app}\lib"; Flags: ignoreversion

; App icon and images
Source: "C:\Users\purple\Desktop\Dev\Java\StructureCreation\src\main\resources\images\app-icon.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\Users\purple\Desktop\Dev\Java\StructureCreation\src\main\resources\images\app-icon.png"; DestDir: "{app}"; Flags: ignoreversion

; Documentation and license files
Source: "C:\Users\purple\Desktop\Dev\Java\StructureCreation\LICENSE.txt"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\Users\purple\Desktop\Dev\Java\StructureCreation\INSTALL-INFO.txt"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\Users\purple\Desktop\Dev\Java\StructureCreation\INSTALL-COMPLETE.txt"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\Users\purple\Desktop\Dev\Java\StructureCreation\README.md"; DestDir: "{app}"; Flags: ignoreversion

[Registry]
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocExt}\OpenWithProgids"; ValueType: string; ValueName: "{#MyAppAssocKey}"; ValueData: ""; Flags: uninsdeletevalue
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocKey}"; ValueType: string; ValueName: ""; ValueData: "{#MyAppAssocName}"; Flags: uninsdeletekey
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocKey}\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\app-icon.ico,0"
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocKey}\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """wscript.exe"" ""{app}\{#MyAppExeName}"" ""%1"""

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "wscript.exe"; Parameters: """{app}\{#MyAppExeName}"""; IconFilename: "{app}\app-icon.ico"; WorkingDir: "{app}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "wscript.exe"; Parameters: """{app}\{#MyAppExeName}"""; IconFilename: "{app}\app-icon.ico"; WorkingDir: "{app}"; Tasks: desktopicon

[Run]
Filename: "wscript.exe"; Parameters: """{app}\{#MyAppExeName}"""; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: postinstall skipifsilent; WorkingDir: "{app}"