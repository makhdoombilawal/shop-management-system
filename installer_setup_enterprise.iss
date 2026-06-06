; ============================================================================
; Shop Management System - Production Installer
; Inno Setup v6.0+
; ============================================================================
; Features:
; - Single .exe installer package
; - Auto-detects MySQL, falls back to SQLite
; - Creates default users on first launch
; - Desktop and Start Menu shortcuts
; - Automatic database initialization
; - Full uninstall support
; ============================================================================

#define MyAppName "Shop Management System"
#define MyAppVersion "2.0.0"
#define MyAppPublisher "Shop Manager Pro"
#define MyAppURL "https://shopmanagement.pro"
#define MyAppExeName "ShopManager.exe"
#define MyAppIcon "resources\icons\shop_icon.ico"

[Setup]
; Application Information
AppId={{7B8C9D0E-1F2A-4B5C-8D9E-0F1A2B3C4D5E}}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName={#MyAppName} v{#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL=https://shopmanagement.pro
AppSupportURL=https://shopmanagement.pro/support
AppUpdatesURL=https://shopmanagement.pro/updates

; Installation Directories
DefaultDirName={autopf}\ShopManagement
DefaultGroupName=ShopManagement
DisableProgramGroupPage=no

; Output Configuration
OutputDir=dist\installer
OutputBaseFilename=ShopManager_Installer_v2.0

; Compression
Compression=lzma2/ultra64
SolidCompression=yes
LZMAUseSeparateProcess=yes
LZMANumBlockThreads=4

; Privileges & Windows Version
PrivilegesRequired=admin
PrivilegesRequiredOverridesAllowed=dialog
MinVersion=6.1sp1
ArchitecturesInstallIn64BitMode=x64compatible

; Appearance
WizardStyle=modern
DisableWelcomePage=no

; License & Information
LicenseFile=LICENSE.txt
InfoBeforeFile=resources\installer\PREINSTALL_INFO.txt
InfoAfterFile=resources\installer\POSTINSTALL_INFO.txt

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: checkedonce

[Files]
; ============================================================================
; APPLICATION FILES
; ============================================================================

; Main Application JAR
Source: "dist\shop-management.jar"; DestDir: "{app}\lib"; Flags: ignoreversion

; Launcher executable (Java wrapper)
Source: "resources\launchers\ShopManager.exe"; DestDir: "{app}"; Flags: ignoreversion

; Launcher batch script
Source: "resources\launchers\ShopManager.bat"; DestDir: "{app}"; Flags: ignoreversion

; ============================================================================
; DEPENDENCIES & LIBRARIES
; ============================================================================

; All JAR dependencies
Source: "dist\lib\*.jar"; DestDir: "{app}\lib"; Flags: ignoreversion

; ============================================================================
; CONFIGURATION FILES
; ============================================================================

; Application configuration
Source: "resources\config\application.properties"; DestDir: "{app}\config"; Flags: ignoreversion onlyifdoesntexist
Source: "config.properties"; DestDir: "{app}"; Flags: ignoreversion

; Hibernate configuration template
Source: "src\hibernate.cfg.xml"; DestDir: "{app}\config"; Flags: ignoreversion

; ============================================================================
; RESOURCES & ICONS
; ============================================================================

; Application Icons
Source: "resources\icons\*.ico"; DestDir: "{app}\resources\icons"; Flags: ignoreversion

; Barcode Storage Directory
Source: "barcodes\*"; DestDir: "{app}\barcodes"; Flags: ignoreversion recursesubdirs createallsubdirs

; ============================================================================
; SCRIPTS & SQL
; ============================================================================

; Database initialization
Source: "resources\scripts\db_init.ps1"; DestDir: "{app}\scripts"; Flags: ignoreversion
Source: "resources\scripts\initialize_database.bat"; DestDir: "{app}\scripts"; Flags: ignoreversion onlyifdoesntexist

; SQL scripts for reference
Source: "sql\*.sql"; DestDir: "{app}\sql"; Flags: ignoreversion

; ============================================================================
; DOCUMENTATION
; ============================================================================

; README
Source: "README.md"; DestDir: "{app}"; Flags: ignoreversion isreadme

; License
Source: "LICENSE.txt"; DestDir: "{app}"; Flags: ignoreversion

; Database setup guide
; Documentation folder
Source: "docs\*"; DestDir: "{app}\docs"; Flags: ignoreversion recursesubdirs onlyifdoesntexist

[Dirs]
; ============================================================================
; DATA DIRECTORIES
; ============================================================================

Name: "{app}\data"
Name: "{app}\logs"
Name: "{app}\temp"

; ============================================================================
; BUNDLED JRE (Optional - Uncomment if bundling JRE)
; ============================================================================
; Name: "{app}\jre"
[Icons]
; ============================================================================
; START MENU & DESKTOP SHORTCUTS
; ============================================================================

; Main application shortcut in Start Menu - Use batch file as launcher
Name: "{group}\Shop Management System"; Filename: "{app}\ShopManager.bat"; IconFileName: "{app}\resources\icons\shop_icon.ico"; Comment: "Launch Shop Management System"

; Desktop Shortcut - Use batch file as launcher
Name: "{commondesktop}\Shop Management"; Filename: "{app}\ShopManager.bat"; IconFileName: "{app}\resources\icons\shop_icon.ico"; Comment: "Launch Shop Management System"; Tasks: desktopicon

; Uninstall shortcut
Name: "{group}\Uninstall {#MyAppName}"; Filename: "{uninstallexe}"

[Run]
; ============================================================================
; POST-INSTALLATION ACTIONS
; ============================================================================

; Display success message after installation
Filename: "{app}\ShopManager.bat"; Description: "Launch Shop Management System"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
; ============================================================================
; CLEANUP ON UNINSTALL
; ============================================================================

Type: filesandordirs; Name: "{app}\data"
Type: filesandordirs; Name: "{app}\logs"
Type: filesandordirs; Name: "{app}\temp"
Type: files; Name: "{app}\*.log"

[Code]
var
  LogFile: String;

{ Log setup events to file }
procedure LogMessage(Msg: String);
begin
  SaveStringToFile(LogFile, GetDateTimeString('yyyy-mm-dd hh:nn:ss', '-', ':') + ' - ' + Msg + #13#10, True);
end;

{ Initialize wizard and create database selection page }
procedure InitializeWizard;
begin
  LogFile := ExpandConstant('{tmp}\shopmanagement_setup.log');
  
  LogMessage('Setup wizard initialized');
end;

{ Validate setup completion }
function NextButtonClick(CurPageID: Integer): Boolean;
begin
  Result := True;
end;

{ Perform post-installation configuration }
procedure CurStepChanged(CurStep: TSetupStep);
var
  DataDir: String;
  LogDir: String;
begin
  if CurStep = ssPostInstall then
  begin
    LogMessage('Post-installation configuration started');
    LogMessage('Using packaged AUTO configuration (MySQL with SQLite fallback)');
    
    { Ensure data directories exist }
    DataDir := ExpandConstant('{app}\data');
    LogDir := ExpandConstant('{app}\logs');
    
    if not DirExists(DataDir) then
      CreateDir(DataDir);
    if not DirExists(LogDir) then
      CreateDir(LogDir);
    
    LogMessage('Installation completed successfully');
    LogMessage('Default logins initialized: admin, manager, cashier');
  end;
end;

{ Show completion message }
procedure CurPageChanged(CurPageID: Integer);
var
  InfoMsg: String;
begin
  if CurPageID = wpFinished then
  begin
    InfoMsg := 'Installation Complete!' + #13#10 + #13#10 +
      'Shop Management System has been successfully installed to:' + #13#10 +
      ExpandConstant('{app}') + #13#10 + #13#10 +
      'DEFAULT LOGIN CREDENTIALS:' + #13#10 +
      '  Admin: admin / admin123' + #13#10 +
      '  Manager: manager / manager123' + #13#10 +
      '  Cashier: cashier / cashier123' + #13#10 + #13#10 +
      'IMPORTANT: Change all default passwords after your first login!' + #13#10 + #13#10 +
      'The application will launch automatically and initialize the database on first run.' + #13#10 + #13#10 +
      'A shortcut has been created on your Desktop for easy access.';
    
    MsgBox(InfoMsg, mbInformation, MB_OK);
  end;
end;

