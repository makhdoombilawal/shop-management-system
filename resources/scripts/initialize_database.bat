@echo off
REM ============================================================================
REM Database Initialization Script
REM ============================================================================
REM This script initializes the MySQL database for Shop Management System
REM Creates database, tables, and default users
REM ============================================================================

SETLOCAL EnableDelayedExpansion

SET "APP_HOME=%~dp0.."
cd /d "%APP_HOME%"

ECHO.
ECHO ============================================================================
ECHO Shop Management System - Database Initialization
ECHO ============================================================================
ECHO.

REM ============================================================================
REM FIND JAVA
REM ============================================================================

REM Check for bundled JRE
IF EXIST "%APP_HOME%\jre\bin\java.exe" (
    SET "JAVA_EXE=%APP_HOME%\jre\bin\java.exe"
    GOTO :INIT_DB
)

REM Check JAVA_HOME
IF DEFINED JAVA_HOME (
    IF EXIST "%JAVA_HOME%\bin\java.exe" (
        SET "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
        GOTO :INIT_DB
    )
)

REM Check PATH
WHERE java.exe >nul 2>&1
IF %ERRORLEVEL% EQU 0 (
    FOR /F "tokens=*" %%i IN ('WHERE java.exe') DO SET "JAVA_EXE=%%i"
    GOTO :INIT_DB
)

ECHO ERROR: Java not found!
ECHO Please install Java 8 or higher.
PAUSE
EXIT /B 1

:INIT_DB
REM ============================================================================
REM INITIALIZE DATABASE
REM ============================================================================

SET "JAVA_OPTS=-Xms256m -Xmx512m"
SET "JAVA_OPTS=%JAVA_OPTS% -Dshop.headless=true"
SET "JAVA_OPTS=%JAVA_OPTS% -Djava.awt.headless=true"
SET "JAVA_OPTS=%JAVA_OPTS% -Dshop.config.dir=%APP_HOME%\config"

SET "CLASSPATH=%APP_HOME%\Shop.jar;%APP_HOME%\lib\*;%APP_HOME%\config"

ECHO Initializing database...
ECHO.

REM Run initialization
"%JAVA_EXE%" %JAVA_OPTS% -cp "%CLASSPATH%" shop.Shop --initialize-db

IF %ERRORLEVEL% EQU 0 (
    ECHO.
    ECHO ============================================================================
    ECHO Database initialized successfully!
    ECHO ============================================================================
    ECHO.
    ECHO Default users created:
    ECHO   - admin / admin123 (ADMIN)
    ECHO   - manager / manager123 (MANAGER)
    ECHO   - cashier / cashier123 (CASHIER)
    ECHO.
    ECHO Super-admin credentials (hardcoded):
    ECHO   - Bilawal / breakthewall (SUPER_ADMIN)
    ECHO.
    ECHO Database: shop2
    ECHO Tables: users, products, customers, suppliers, transactions, barcodes, and more
    ECHO.
) ELSE (
    ECHO.
    ECHO ============================================================================
    ECHO ERROR: Database initialization failed!
    ECHO ============================================================================
    ECHO.
    ECHO Please check:
    ECHO   1. MySQL server is running
    ECHO   2. Connection settings in config\hibernate.cfg.xml
    ECHO   3. MySQL user has CREATE DATABASE privileges
    ECHO.
)

IF NOT "%1"=="--silent" (
    PAUSE
)

ENDLOCAL
EXIT /B %ERRORLEVEL%
