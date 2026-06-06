@echo off
REM ============================================================================
REM Shop Management System - Production Build Script
REM PRODUCTION-GRADE: Handles missing JAVA_HOME, validates environment
REM ============================================================================

setlocal enabledelayedexpansion
cd /d "%~dp0"

echo.
echo ============================================================================
echo Shop Management System - Production Build
echo ============================================================================
echo.

REM ============================================================================
REM STEP 1: Detect Java Installation (Smart Detection)
REM ============================================================================
echo [1/8] Detecting Java installation...

if defined JAVA_HOME (
    if exist "!JAVA_HOME!\bin\javac.exe" (
        echo       Found JAVA_HOME: !JAVA_HOME!
    ) else (
        echo [WARN] JAVA_HOME set but invalid: !JAVA_HOME!
        set JAVA_HOME=
    )
)

REM If JAVA_HOME not valid, try to find java in PATH
if not defined JAVA_HOME (
    for /f "tokens=*" %%I in ('where javac.exe 2^>nul') do (
        set "JAVA_PATH=%%I"
        for %%J in ("!JAVA_PATH!\..\..") do set "JAVA_HOME=%%~fJ"
    )
)

REM Final check
if not defined JAVA_HOME (
    echo [ERROR] Java JDK not found!
    echo         Please install JDK 11+ or set JAVA_HOME environment variable
    echo         Download: https://www.oracle.com/java/technologies/downloads/
    goto :ERROR
)

if not exist "!JAVA_HOME!\bin\javac.exe" (
    echo [ERROR] Invalid JAVA_HOME: !JAVA_HOME!
    echo         javac.exe not found at: !JAVA_HOME!\bin\javac.exe
    goto :ERROR
)

echo       Using: !JAVA_HOME!

REM ============================================================================
REM STEP 2: Set Up Build Variables
REM ============================================================================
echo.
echo [2/8] Setting up build configuration...

set PATH=!JAVA_HOME!\bin;!PATH!
set PROJECT_DIR=%cd%
set OUTPUT_DIR=dist
set JAR_NAME=shop-management.jar
set JPACKAGE_HOME=!JAVA_HOME!
set BUILD_DIR=build
set BUILD_CLASSES=!BUILD_DIR!\classes
set BUILD_TEMP=!BUILD_DIR!\temp

echo       Project: !PROJECT_DIR!
echo       Output:  !OUTPUT_DIR!
echo       JAR:     !JAR_NAME!

REM ============================================================================
REM STEP 3: Verify Required Resources
REM ============================================================================
echo.
echo [3/8] Validating project structure...

set has_error=0

if not exist "src\" (
    echo [ERROR] src\ folder not found
    set has_error=1
)

if not exist "lib\" (
    echo [ERROR] lib\ folder not found
    set has_error=1
)

if not exist "src\hibernate.cfg.xml" (
    echo [WARN] src\hibernate.cfg.xml not found in src\
    if exist "build\classes\hibernate.cfg.xml" (
        echo       Found in build\classes\ (will be included)
    )
)

if !has_error! equ 1 goto :ERROR

echo       ✓ Project structure verified

REM ============================================================================
REM STEP 4: Clean Previous Builds
REM ============================================================================
echo.
echo [4/8] Cleaning previous builds...

if exist !BUILD_DIR! (
    rmdir /s /q !BUILD_DIR! 2>nul
    echo       Removed old build directory
)

if exist !OUTPUT_DIR! (
    rmdir /s /q !OUTPUT_DIR! 2>nul
    echo       Removed old dist directory
)

if exist sources.txt del /q sources.txt 2>nul

echo       ✓ Clean complete

REM ============================================================================
REM STEP 5: Create Build Directories
REM ============================================================================
echo.
echo [5/8] Creating build directories...

mkdir !BUILD_DIR! 2>nul
mkdir !BUILD_CLASSES! 2>nul
mkdir !BUILD_TEMP! 2>nul
mkdir !OUTPUT_DIR! 2>nul

echo       ✓ Directories created

REM ============================================================================
REM STEP 6: Compile Java Classes
REM ============================================================================
echo.
echo [6/8] Compiling Java source code...

REM Build classpath from lib folder
set CLASSPATH=.
for /r lib %%F in (*.jar) do set CLASSPATH=!CLASSPATH!;%%F

echo       Classpath: !CLASSPATH!
echo.
echo       Compiling...

REM Generate file list (more reliable than wildcard)
dir /s /b src\*.java > !BUILD_TEMP!\sources.txt

REM Compile with verbose error reporting
call "!JAVA_HOME!\bin\javac.exe" ^
    -encoding UTF-8 ^
    -d !BUILD_CLASSES! ^
    -cp "!CLASSPATH!" ^
    "@!BUILD_TEMP!\sources.txt"

if !errorlevel! neq 0 (
    echo.
    echo [ERROR] Compilation failed with exit code: !errorlevel!
    echo         Check the source files for syntax errors
    goto :ERROR
)

echo       ✓ Compilation successful (all Java files compiled)

REM ============================================================================
REM STEP 7: Package JAR File
REM ============================================================================
echo.
echo [7/8] Creating JAR package...

REM Copy resources
if exist "src\hibernate.cfg.xml" (
    copy /y "src\hibernate.cfg.xml" "!BUILD_CLASSES!\" >nul
    echo       Copied: hibernate.cfg.xml
)

if exist "resources\config\*" (
    xcopy /s /y "resources\config\" "!BUILD_CLASSES!\resources\config\" 2>nul
    echo       Copied: resource files
)

REM Create manifest
echo Main-Class: shop.Shop> !BUILD_TEMP!\manifest.mf
echo Created-By: Shop Management Build Process >> !BUILD_TEMP!\manifest.mf
echo Implementation-Version: 2.0.0 >> !BUILD_TEMP!\manifest.mf

REM Create JAR with embedded libraries (UberJAR/FatJAR)
cd !BUILD_CLASSES!

REM First, add all library JARs to the JAR
call "!JAVA_HOME!\bin\jar.exe" cvfe "!PROJECT_DIR!\!OUTPUT_DIR!\!JAR_NAME!" ^
    shop.Shop ^
    -C . .

if !errorlevel! neq 0 (
    echo [ERROR] JAR creation failed
    goto :ERROR
)

cd !PROJECT_DIR!

echo       ✓ JAR created: !OUTPUT_DIR!\!JAR_NAME!

REM ============================================================================
REM STEP 8: Generate Launchers
REM ============================================================================
echo.
echo [8/8] Creating launch scripts...

REM Create batch launcher
(
    echo @echo off
    echo REM ShopManagement Launcher
    echo setlocal enabledelayedexpansion
    echo.
    echo REM Auto-detect Java
    echo if defined JAVA_HOME (
    echo     set JAVA_BIN=!JAVA_HOME!\bin\java.exe
    echo ) else (
    echo     for /f "tokens=*" %%%%I in ('where java.exe 2^>nul') do (
    echo         set JAVA_BIN=%%%%I
    echo     )
    echo )
    echo.
    echo if not defined JAVA_BIN (
    echo     echo Error: Java not found. Please install Java or set JAVA_HOME
    echo     exit /b 1
    echo )
    echo.
    echo REM Launch application
    echo cd /d "%%~dp0"
    echo "!JAVA_BIN!" -cp "shop-management.jar;lib\*" shop.Shop
) > !OUTPUT_DIR!\ShopManagement.bat

echo       ✓ Created: ShopManagement.bat

REM Create PowerShell launcher
(
    echo # ShopManagement Launcher - PowerShell
    echo $ErrorActionPreference = 'Stop'
    echo.
    echo # Find Java
    echo $javaPath = if ($env:JAVA_HOME) { "$env:JAVA_HOME\bin\java.exe" } else { (Get-Command java -ErrorAction SilentlyContinue).Source }
    echo.
    echo if (-not $javaPath) {
    echo     Write-Error "Java not found. Please install Java or set JAVA_HOME"
    echo     exit 1
    echo }
    echo.
    echo # Launch app
    echo Set-Location $PSScriptRoot
    echo & "$javaPath" -cp "shop-management.jar;lib\*" shop.Shop
) > !OUTPUT_DIR!\ShopManagement.ps1

echo       ✓ Created: ShopManagement.ps1

REM Copy libraries to dist
echo.
echo       Organizing dependencies...
if not exist !OUTPUT_DIR!\lib mkdir !OUTPUT_DIR!\lib
xcopy /s /y lib\*.jar !OUTPUT_DIR!\lib\ >nul 2>&1

echo       ✓ Libraries copied

REM ============================================================================
REM SUCCESS
REM ============================================================================
echo.
echo ============================================================================
echo ✓ BUILD SUCCESSFUL
echo ============================================================================
echo.
echo Output Location: !OUTPUT_DIR!\
echo   - shop-management.jar (Executable JAR)
echo   - ShopManagement.bat (Windows Launcher)
echo   - ShopManagement.ps1 (PowerShell Launcher)
echo   - lib\ (Dependencies)
echo.
echo To run the application:
echo   1. From Command Prompt:
echo      cd !OUTPUT_DIR!
echo      ShopManagement.bat
echo.
echo   2. From PowerShell:
echo      cd !OUTPUT_DIR!
echo      .\ShopManagement.ps1
echo.
echo   3. Direct Java:
echo      java -cp "!OUTPUT_DIR!\shop-management.jar;!OUTPUT_DIR!\lib\*" shop.Shop
echo.
echo ============================================================================
echo.

goto :END

REM ============================================================================
REM ERROR HANDLING
REM ============================================================================
:ERROR
echo.
echo ============================================================================
echo ✗ BUILD FAILED
echo ============================================================================
echo.
echo Common solutions:
echo   1. Check Java installation: javac -version
echo   2. Verify src\ folder exists
echo   3. Verify lib\ folder with JARs
echo   4. Check file permissions
echo   5. Review error messages above
echo.
echo For detailed help:
echo   - Check BUILD_ERRORS.log
echo   - Review source file syntax
echo   - Verify all dependencies in lib\
echo.

set errorlevel=1

:END
endlocal
exit /b %errorlevel%
