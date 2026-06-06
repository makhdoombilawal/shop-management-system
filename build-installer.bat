@echo off
REM ============================================================================
REM Shop Management System v2.0 - Installer Builder
REM This script compiles the Inno Setup script into a Windows .exe installer
REM ============================================================================

setlocal enabledelayedexpansion

echo.
echo ============================================================================
echo  Shop Management System v2.0 - Windows Installer Builder
echo ============================================================================
echo.

set SCRIPT_DIR=%~dp0
set ISS_FILE=%SCRIPT_DIR%installer_setup_enterprise.iss
set OUTPUT_DIR=%SCRIPT_DIR%dist\installer

REM ============================================================================
REM CHECK IF ISS SCRIPT EXISTS
REM ============================================================================

if not exist "%ISS_FILE%" (
    echo ERROR: Inno Setup script not found: %ISS_FILE%
    echo.
    pause
    exit /b 1
)

echo [OK] Found Inno Setup script: %ISS_FILE%
echo.

REM ============================================================================
REM FIND INNO SETUP COMPILER
REM ============================================================================

echo Searching for Inno Setup compiler (iscc.exe)...
echo.

REM Try to find iscc.exe in PATH
where iscc.exe >nul 2>&1
if !ERRORLEVEL! EQU 0 (
    for /f "delims=" %%A in ('where iscc.exe') do set ISCC_PATH=%%A
    echo [OK] Found Inno Setup at: !ISCC_PATH!
    echo.
    goto build_installer
)

REM Try common installation directories
set COMMON_PATHS=^
    "C:\Program Files (x86)\Inno Setup 6\iscc.exe" ^
    "C:\Program Files\Inno Setup 6\iscc.exe" ^
    "%LOCALAPPDATA%\Programs\Inno Setup 6\ISCC.exe" ^
    "C:\Program Files (x86)\Inno Setup 5\iscc.exe" ^
    "C:\Program Files\Inno Setup 5\iscc.exe"

for %%P in (%COMMON_PATHS%) do (
    if exist %%P (
        set ISCC_PATH=%%~P
        echo [OK] Found Inno Setup at: !ISCC_PATH!
        echo.
        goto build_installer
    )
)

echo.
echo ============================================================================
echo  ERROR: Inno Setup NOT FOUND
echo ============================================================================
echo.
echo Inno Setup v5.0+ is required to build the Windows installer.
echo.
echo SOLUTION: Download and install Inno Setup
echo   Download: https://jrsoftware.org/isdl.php
echo   Files: Choose either issetup-6.x.x.exe (recommended)
echo   Installation: Use default installation path
echo.
echo AFTER INSTALLING:
echo   1. Run this script again: build-installer.bat
echo   2. OR use Inno Setup GUI to open: %ISS_FILE%
echo.
echo ALTERNATIVE: Command line (after installing Inno Setup)
echo   "C:\Program Files (x86)\Inno Setup 6\iscc.exe" "%ISS_FILE%"
echo.
pause
exit /b 1

REM ============================================================================
REM BUILD INSTALLER
REM ============================================================================

:build_installer

echo ============================================================================
echo  BUILDING INSTALLER
echo ============================================================================
echo.
echo Script: %ISS_FILE%
echo Output: %OUTPUT_DIR%
echo.
echo Building... This may take 1-2 minutes depending on your system.
echo.

REM Create output directory if it doesn't exist
if not exist "%OUTPUT_DIR%" (
    mkdir "%OUTPUT_DIR%"
)

REM Run Inno Setup compiler
"!ISCC_PATH!" "%ISS_FILE%"

set BUILD_EXIT=%ERRORLEVEL%

if not "%BUILD_EXIT%"=="0" goto build_failed

echo.
echo ============================================================================
echo  ✓ INSTALLER BUILD SUCCESSFUL!
echo ============================================================================
echo.

set INSTALLER_PATH=%OUTPUT_DIR%\ShopManager_Installer_v2.0.exe

if exist "!INSTALLER_PATH!" (
    for /F "tokens=*" %%A in ('dir /b /s "!INSTALLER_PATH!"') do (
        set INSTALLER_SIZE=%%~zA
    )
    
    REM Convert bytes to MB
    set /a INSTALLER_MB=!INSTALLER_SIZE! / 1048576
    
    echo Installer: !INSTALLER_PATH!
    echo Size: !INSTALLER_MB! MB
    echo.
)

echo INSTALLATION INSTRUCTIONS:
echo   1. Copy ShopManager_Installer_v2.0.exe to target machine
echo   2. Double-click the installer to start installation wizard
echo   3. Follow the installation steps
echo   4. Application will launch automatically
echo   5. Use default credentials to login (see deployment guide)
echo.
echo DEPLOYMENT DOCUMENTATION:
echo   - QUICK_DEPLOYMENT_REFERENCE.md - Quick start guide
echo   - DEPLOYMENT_COMPLETE_SUMMARY.md - Full deployment instructions
echo.
goto done

:build_failed
    echo.
    echo ============================================================================
    echo  ✗ INSTALLER BUILD FAILED
    echo ============================================================================
    echo.
echo Exit Code: !BUILD_EXIT!
echo.
echo Troubleshooting:
echo   1. Verify Inno Setup is properly installed
echo   2. Check that build.bat ran successfully (dist\shop-management.jar exists)
echo   3. Verify all required files are in the project directory
echo   4. Try opening the .iss script in Inno Setup GUI and compiling manually
echo.

:done
echo.
pause
exit /b !BUILD_EXIT!
