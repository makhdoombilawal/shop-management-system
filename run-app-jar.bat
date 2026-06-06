@echo off
REM ============================================================================
REM Quick Test Launcher - Shop Management System
REM Runs the application directly from JAR (after building)
REM ============================================================================

echo.
echo Starting Shop Management System...
echo.

REM Check if JAR exists
if exist "dist\shop-management.jar" (
    echo Running from: dist\shop-management.jar
    echo.
    java -Xms512m -Xmx2048m -Dfile.encoding=UTF-8 -jar dist\shop-management.jar
) else if exist "target\shop-management.jar" (
    echo Running from: target\shop-management.jar
    echo.
    java -Xms512m -Xmx2048m -Dfile.encoding=UTF-8 -jar target\shop-management.jar
) else (
    echo.
    echo ERROR: Application JAR not found!
    echo.
    echo Please build the application first:
    echo   1. Run: build-executable.bat
    echo   2. Or run: mvn clean package
    echo.
    pause
    exit /b 1
)
