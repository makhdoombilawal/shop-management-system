; ============================================================================
; Shop Management System - Production Installer (FIXED & STABLE)
; ============================================================================

#define MyAppName "Shop Management System"
#define MyAppVersion "2.0.0"
#define MyAppPublisher "Shop Manager Pro"
#define MyAppExeName "ShopManagement.bat"

[Setup]
AppId={{7B8C9D0E-1F2A-4B5C-8D9E-0F1A2B3C4D5E}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName={#MyAppName} v{#MyAppVersion}
AppPublisher={#MyAppPublisher}

DefaultDirName={autopf}\ShopManagement
DefaultGroupName=ShopManagement

OutputDir=dist\installer
OutputBaseFilename=ShopManager_Installer_v2.0

Compression=lzma2
SolidCompression=yes

WizardStyle=modern
PrivilegesRequired=admin

LicenseFile=LICENSE.txt

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "Create Desktop Shortcut"; GroupDescription: "Additional Icons"; Flags: checkedonce

; ============================================================================
; MAIN APPLICATION FILES
; ============================================================================
[Files]

Source: "dist\shop-management.jar"; DestDir: "{app}\lib"; Flags: ignoreversion
Source: "dist\lib\*"; DestDir: "{app}\lib"; Flags: ignoreversion recursesubdirs createallsubdirs

Source: "dist\ShopManagement.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "dist\ShopManagement.ps1"; DestDir: "{app}"; Flags: ignoreversion

; ============================================================================
; CONFIGURATION
; ============================================================================
Source: "resources\config\application.properties"; DestDir: "{app}\config"; Flags: ignoreversion onlyifdoesntexist
Source: "config.properties"; DestDir: "{app}"; Flags: ignoreversion onlyifdoesntexist

Source: "src\hibernate.cfg.xml"; DestDir: "{app}\config"; Flags: ignoreversion onlyifdoesntexist

; ============================================================================
; RESOURCES
; ============================================================================
Source: "resources\icons\*.ico"; DestDir: "{app}\resources\icons"; Flags: ignoreversion
Source: "barcodes\*"; DestDir: "{app}\barcodes"; Flags: ignoreversion recursesubdirs createallsubdirs

; ============================================================================
; SQL FILES (FIXED - NO WILDCARD CRASH)
; ============================================================================
Source: "sql\schema\complete_schema.sql"; DestDir: "{app}\sql\schema"; Flags: ignoreversion

Source: "sql\tables\categories.sql"; DestDir: "{app}\sql\tables"; Flags: ignoreversion
Source: "sql\tables\customers.sql"; DestDir: "{app}\sql\tables"; Flags: ignoreversion
Source: "sql\tables\products.sql"; DestDir: "{app}\sql\tables"; Flags: ignoreversion
Source: "sql\tables\transactions.sql"; DestDir: "{app}\sql\tables"; Flags: ignoreversion
Source: "sql\tables\users.sql"; DestDir: "{app}\sql\tables"; Flags: ignoreversion
Source: "sql\tables\roles.sql"; DestDir: "{app}\sql\tables"; Flags: ignoreversion
Source: "sql\tables\stock_audits.sql"; DestDir: "{app}\sql\tables"; Flags: ignoreversion

; ============================================================================
; DOCUMENTATION
; ============================================================================
Source: "README.md"; DestDir: "{app}"; Flags: ignoreversion isreadme
Source: "LICENSE.txt"; DestDir: "{app}"; Flags: ignoreversion

[Dirs]
Name: "{app}\data"
Name: "{app}\logs"
Name: "{app}\temp"

; ============================================================================
; SHORTCUTS
; ============================================================================
[Icons]

Name: "{group}\Shop Management System"; Filename: "{app}\ShopManagement.bat"
Name: "{commondesktop}\Shop Management"; Filename: "{app}\ShopManagement.bat"; Tasks: desktopicon
Name: "{group}\Uninstall"; Filename: "{uninstallexe}"

; ============================================================================
; RUN AFTER INSTALL
; ============================================================================
[Run]

Filename: "{app}\ShopManagement.bat"; Description: "Launch Application"; Flags: nowait postinstall skipifsilent

; ============================================================================
; CLEANUP
; ============================================================================
[UninstallDelete]

Type: filesandordirs; Name: "{app}\logs"
Type: filesandordirs; Name: "{app}\temp"

; ============================================================================
; CODE (MINIMAL SAFE)
; ============================================================================
[Code]

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssPostInstall then
  begin
    Log('Installation completed successfully');
  end;
end;

procedure CurPageChanged(CurPageID: Integer);
begin
  if CurPageID = wpFinished then
  begin
    MsgBox(
      'Installation Complete!' + #13#10#13#10 +
      'Shop Management System installed successfully.' + #13#10 +
      'Please launch the application to log in.',
      mbInformation, MB_OK);
  end;
end;