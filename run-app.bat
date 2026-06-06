@echo off
REM Shop Management System - Quick Run Script
REM For Windows

echo ========================================
echo   SHOP MANAGEMENT SYSTEM - LAUNCHER
echo ========================================
echo.

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java JDK 17 or higher
    pause
    exit /b 1
)

echo [*] Java found
echo [*] Starting application...
echo.

REM Option 1: If using NetBeans built JAR
if exist "dist\Shop.jar" (
    echo Running from dist\Shop.jar...
    java -cp "dist\Shop.jar;dist\lib\*" shop.Shop
    goto :end
)

REM Option 2: If running from build directory
if exist "build\classes" (
    echo Running from build\classes...
    cd build\classes
    java -cp ".;..\..\lib\*" shop.Shop
    cd ..\..
    goto :end
)

echo ERROR: Application not built!
echo Please build the project first using NetBeans:
echo   1. Open project in NetBeans
echo   2. Press Shift+F11 (Clean and Build)
echo   3. Press F6 (Run)
echo.

:end
pause
