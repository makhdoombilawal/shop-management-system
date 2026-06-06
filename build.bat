@echo off
REM ============================================================================
REM Shop Management System v2.0 - Build Wrapper
REM This batch file runs the automated PowerShell build script
REM ============================================================================

setlocal enabledelayedexpansion

REM Get script directory
set SCRIPT_DIR=%~dp0

REM Check PowerShell availability
where pwsh.exe >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set PS_EXE=pwsh.exe
    goto run_build
)

where powershell.exe >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set PS_EXE=powershell.exe
    goto run_build
)

echo ERROR: PowerShell not found!
echo Please install PowerShell from: https://github.com/PowerShell/PowerShell/releases
pause
exit /b 1

:run_build
echo ============================================================================
echo Building Shop Management System v2.0
echo ============================================================================
echo.

REM Run PowerShell build script
REM Parameters:
REM   -NoProfile: Don't load profile scripts
REM   -ExecutionPolicy Bypass: Allow unsigned scripts
REM   -File: Execute the script file
REM   -ProjectRoot: Pass project root directory to script
REM   %*: Pass command line arguments to script

%PS_EXE% -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%build-automated.ps1" -ProjectRoot "%SCRIPT_DIR:~0,-1%" %*

set BUILD_EXIT_CODE=!ERRORLEVEL!

echo.
if !BUILD_EXIT_CODE! EQU 0 (
    echo ============================================================================
    echo BUILD COMPLETED SUCCESSFULLY
    echo ============================================================================
    echo.
    echo Next steps:
    echo   1. Verify JAR: java -jar dist\shop-management.jar
    echo   2. Build installer: iscc installer_setup_enterprise.iss
    echo   3. Install and test
    echo.
) else (
    echo ============================================================================
    echo BUILD FAILED - Exit Code: !BUILD_EXIT_CODE!
    echo ============================================================================
    echo Check build.log for details
    echo.
)

pause
exit /b !BUILD_EXIT_CODE!
