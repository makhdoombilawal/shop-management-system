@echo off
setlocal enabledelayedexpansion
setlocal enableextensions

REM ============================================================================
REM Shop Management System - Application Launcher
REM Version: 2.0 Enterprise
REM ============================================================================
REM This batch file launches the Shop Management System with proper Java
REM classpath, memory settings, and environment configuration
REM ============================================================================

REM Change to script directory (installation directory)
cd /d "%~dp0"

REM ============================================================================
REM JAVA CONFIGURATION
REM ============================================================================

REM Java memory settings (can be modified as needed)
set JAVA_OPTS=-Xms512m -Xmx2048m -Dfile.encoding=UTF-8

REM Installation directory
set INSTALL_DIR=%CD%

REM Find Java executable
if defined JAVA_HOME (
    if exist "!JAVA_HOME!\bin\java.exe" (
        set JAVA_EXE=!JAVA_HOME!\bin\java.exe
        goto java_found
    )
)

REM Search PATH for java
for /f "delims=" %%A in ('where java 2^>nul') do (
    set "JAVA_EXE=%%A"
    goto java_found
)

REM Java not found error
echo.
echo ============================================================================
echo ERROR: Java Runtime Environment (JRE) not found!
echo ============================================================================
echo.
echo Shop Management System requires Java 8 or later to run.
echo.
echo Please download and install Java from one of these sources:
echo   - Oracle: https://www.oracle.com/java/technologies/javase-jre8-downloads.html
echo   - Adoptium: https://adoptium.net/
echo   - Eclipse Temurin: https://projects.eclipse.org/projects/adoptium.temurin/downloads
echo.
echo After installing Java, restart the application.
echo.
pause
exit /b 1

:java_found
REM ============================================================================
REM BUILD CLASSPATH
REM ============================================================================

REM Add config directory first (so hibernate.cfg.xml can be found)
set CLASSPATH=%INSTALL_DIR%

REM Add the main JAR
set CLASSPATH=!CLASSPATH!;%INSTALL_DIR%\lib\shop-management.jar

REM Add all JARs from lib directory
for /r "!INSTALL_DIR!\lib" %%A in (*.jar) do (
    set "CLASSPATH=!CLASSPATH!;%%A"
)

REM Add config and resources directories explicitly
set CLASSPATH=!CLASSPATH!;%INSTALL_DIR%\config
set CLASSPATH=!CLASSPATH!;%INSTALL_DIR%\resources

REM ============================================================================
REM SETUP DIRECTORIES
REM ============================================================================

REM Create necessary directories if they don't exist
if not exist "!INSTALL_DIR!\logs" mkdir "!INSTALL_DIR!\logs"
if not exist "!INSTALL_DIR!\data" mkdir "!INSTALL_DIR!\data"
if not exist "!INSTALL_DIR!\temp" mkdir "!INSTALL_DIR!\temp"

set LOG_DIR=!INSTALL_DIR!\logs
set LOG_FILE=!LOG_DIR!\shopmanagement.log
set TEMP_DIR=!INSTALL_DIR!\temp

REM ============================================================================
REM LAUNCH APPLICATION
REM ============================================================================

echo.
echo Launching Shop Management System...
echo.

REM Log startup
echo. >> "!LOG_FILE!"
echo ============================================================================ >> "!LOG_FILE!"
echo %date% %time% - Application Startup >> "!LOG_FILE!"
echo Java: !JAVA_EXE! >> "!LOG_FILE!"
echo ClassPath: !CLASSPATH! >> "!LOG_FILE!"
echo ============================================================================ >> "!LOG_FILE!"

REM Execute Java application
!JAVA_EXE! !JAVA_OPTS! -cp "!CLASSPATH!" shop.Shop >> "!LOG_FILE!" 2>&1

REM Capture exit code
set EXIT_CODE=!ERRORLEVEL!

REM Log shutdown
echo %date% %time% - Application Shutdown (Exit Code: !EXIT_CODE!) >> "!LOG_FILE!"

REM Return exit code
exit /b !EXIT_CODE!
endlocal
