REM ShopManager Launcher - VBScript Wrapper for ShopManager.bat
REM This VBScript hides the console window and launches ShopManager.bat

' Configure Windows Script Host behavior
On Error Resume Next

' Create Windows Shell object
Set objShell = CreateObject("WScript.Shell")

' Get the directory of this script
strScriptPath = WScript.ScriptFullName
Set objFso = CreateObject("Scripting.FileSystemObject")
strScriptDir = objFso.GetParentFolderName(strScriptPath)

' Construct path to batch file launcher
strBatchFile = strScriptDir & "\ShopManager.bat"

' Check if batch file exists
If Not objFso.FileExists(strBatchFile) Then
    objShell.Popup "Error: ShopManager.bat not found at " & strBatchFile, 0, "Shop Management System - Launcher Error", 16
    WScript.Quit 1
End If

' Launch batch file with hidden window
intReturn = objShell.Run(strBatchFile, 0, False)

' Exit with the batch file's exit code
WScript.Quit intReturn
