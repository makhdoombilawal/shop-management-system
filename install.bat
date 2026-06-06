@echo off
REM ============================================================================
REM Shop Management System v2.0 - Installation & Setup Script
REM Checks MySQL installation and initializes the database
REM ============================================================================

SETLOCAL EnableDelayedExpansion

echo.
echo ================================================================
echo    SHOP MANAGEMENT SYSTEM v2.0 - INSTALLATION
echo ================================================================
echo.

REM Step 1: Check if Java is installed
echo [1/5] Checking Java installation...
java -version >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java is not installed or not in PATH
    echo.
    echo Please install Java JDK 11 or higher:
    echo https://www.oracle.com/java/technologies/downloads/
    echo.
    pause
    exit /b 1
)
echo [OK] Java is installed

REM Step 2: Check if MySQL is installed
echo.
echo [2/5] Checking MySQL installation...
mysql --version >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo [WARNING] MySQL command-line client not found in PATH
    echo.
    echo This is OK if MySQL is installed but not in PATH.
    echo Continuing with database check...
) ELSE (
    echo [OK] MySQL client is installed
)

REM Step 3: Check if MySQL service is running
echo.
echo [3/5] Checking MySQL service status...
sc query MySQL >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    sc query MySQL80 >nul 2>&1
    IF %ERRORLEVEL% NEQ 0 (
        echo [WARNING] MySQL service not found
        echo.
        echo Please ensure MySQL Server is installed and running.
        echo.
        echo Download MySQL Server 8.0+:
        echo https://dev.mysql.com/downloads/mysql/
        echo.
        pause
        REM Continue anyway - let the app handle the error
    ) ELSE (
        echo [OK] MySQL80 service found
    )
) ELSE (
    echo [OK] MySQL service found
)

REM Step 4: Initialize database
echo.
echo [4/5] Initializing database...
echo This may take a few moments...
echo.

java -jar dist\shop-management.jar --initialize-db

IF %ERRORLEVEL% EQU 0 (
    echo.
    echo [OK] Database initialized successfully!
) ELSE (
    echo.
    echo [ERROR] Database initialization failed
    echo.
    echo Common issues:
    echo   1. MySQL Server is not installed
    echo   2. MySQL Service is not running
    echo   3. Invalid MySQL credentials
    echo.
    echo Check configuration in: dist\hibernate.cfg.xml
    echo.
    pause
    exit /b 1
)

REM Step 5: Show completion message
echo.
echo [5/5] Installation complete!
echo.
echo ================================================================
echo    INSTALLATION SUCCESSFUL!
echo ================================================================
echo.
echo Database: shop2 (auto-created)
echo Tables: 11 tables initialized
echo Default Admin Credentials:
echo   Username: admin
echo   Password: admin
echo.
echo IMPORTANT: Change the admin password after first login!
echo.
echo ================================================================
echo.
echo Starting Shop Management System...
echo.

REM Launch the application
start javaw -jar dist\shop-management.jar

echo Application launched!
echo You can close this window.
echo.
pause
