# ============================================================================
# SHOP MANAGEMENT SYSTEM - INSTALLER BUILDER SCRIPT
# ============================================================================

$ErrorActionPreference = "Stop"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " SHOP MANAGEMENT SYSTEM BUILD START" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# --------------------------------------------------------------------
# CONFIGURATION & INNO SETUP DETECTION
# --------------------------------------------------------------------
$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$DistDir = Join-Path $ProjectDir "dist"
$InstallerOutput = Join-Path $DistDir "installer"
$ISS_FILE = Join-Path $ProjectDir "installer_setup_enterprise.iss"

Write-Host "[1] Checking Inno Setup..." -ForegroundColor Yellow

$ISCC_Candidates = @(
    (Get-Command iscc.exe -ErrorAction SilentlyContinue).Source,
    "C:\Users\Bilawal Pc\AppData\Local\Programs\Inno Setup 6\iscc.exe",
    "$env:LocalAppData\Programs\Inno Setup 6\iscc.exe",
    "${env:ProgramFiles(x86)}\Inno Setup 6\iscc.exe",
    "${env:ProgramFiles}\Inno Setup 6\iscc.exe"
)

$ISCC = $null
foreach ($candidate in $ISCC_Candidates) {
    if ($candidate -and (Test-Path $candidate)) {
        $ISCC = $candidate
        break
    }
}

if (-not $ISCC) {
    Write-Host "[ERROR] Inno Setup compiler (iscc.exe) not found!" -ForegroundColor Red
    Write-Host "Please install Inno Setup 6 or add it to PATH." -ForegroundColor Yellow
    exit 1
}

Write-Host "[OK] Inno Setup found: $ISCC" -ForegroundColor Green

# --------------------------------------------------------------------
# CHECK PROJECT BUILD OUTPUT
# --------------------------------------------------------------------
Write-Host "[2] Checking build output..." -ForegroundColor Yellow

$JarFile = Join-Path $DistDir "shop-management.jar"

if (!(Test-Path $JarFile)) {
    Write-Host "[ERROR] JAR file not found. Please build project first." -ForegroundColor Red
    exit 1
}

Write-Host "[OK] JAR file exists" -ForegroundColor Green

# --------------------------------------------------------------------
# CREATE INSTALLER OUTPUT DIRECTORY
# --------------------------------------------------------------------
Write-Host "[3] Preparing installer folder..." -ForegroundColor Yellow

if (!(Test-Path $InstallerOutput)) {
    New-Item -ItemType Directory -Path $InstallerOutput | Out-Null
}

# --------------------------------------------------------------------
# RUN INNO SETUP COMPILER
# --------------------------------------------------------------------
Write-Host "[4] Building installer with Inno Setup..." -ForegroundColor Yellow

& $ISCC $ISS_FILE

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Installer build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "[OK] Installer created successfully" -ForegroundColor Green

# --------------------------------------------------------------------
# FIND OUTPUT FILE
# --------------------------------------------------------------------
$InstallerFile = Join-Path $InstallerOutput "ShopManager_Installer_v2.0.exe"

if (Test-Path $InstallerFile) {
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host " BUILD SUCCESSFUL" -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host "Installer Location:" -ForegroundColor Cyan
    Write-Host $InstallerFile -ForegroundColor White
    Write-Host "==========================================" -ForegroundColor Green
}
else {
    Write-Host "[WARNING] Installer built but file not found in expected location." -ForegroundColor Yellow
}

# --------------------------------------------------------------------
# OPTIONAL: OPEN OUTPUT FOLDER
# --------------------------------------------------------------------
Write-Host "[5] Opening output folder..." -ForegroundColor Yellow
Start-Process $InstallerOutput