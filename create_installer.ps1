# Shop Management System - Installer Generator
# This script ensures Inno Setup is installed and generates the final setup.exe for distribution

Write-Host "=========================================================================="
Write-Host " SHOP MANAGEMENT SYSTEM - AUTOMATED INSTALLER GENERATOR"
Write-Host "=========================================================================="

$paths = @(
    "C:\Program Files\Inno Setup 6\iscc.exe",
    "C:\Program Files (x86)\Inno Setup 6\iscc.exe",
    "C:\Program Files\Inno Setup 5\iscc.exe",
    "C:\Program Files (x86)\Inno Setup 5\iscc.exe",
    "$env:LOCALAPPDATA\Programs\Inno Setup 6\iscc.exe",
    "$env:LOCALAPPDATA\Programs\Inno Setup 5\iscc.exe"
)

$isccPath = ""
foreach ($p in $paths) {
    if (Test-Path $p) {
        $isccPath = $p
        break
    }
}

# Step 1: Check and Install Inno Setup
if (-not $isccPath) {
    Write-Host "[INFO] Inno Setup compiler not found. Installing via winget..."
    winget install JRSoftware.InnoSetup --silent --accept-package-agreements --accept-source-agreements
    
    foreach ($p in $paths) {
        if (Test-Path $p) {
            $isccPath = $p
            break
        }
    }
    
    if (-not $isccPath) {
        Write-Host "[ERROR] Failed to install Inno Setup automatically."
        Write-Host "Please install it manually from: https://jrsoftware.org/isdl.php"
        exit 1
    }
    Write-Host "[INFO] Inno Setup installed successfully."
}

Write-Host "[INFO] Inno Setup compiler found at: $isccPath"

# Step 2: Build the project to ensure JAR and Dist are fresh
Write-Host "[INFO] Ensuring the project is built..."
& .\cleanup_and_build.ps1 -SkipInstaller $true

# Step 3: Run Inno Setup Compiler
$issFile = ".\installer_setup_enterprise.iss"

if (-not (Test-Path $issFile)) {
    Write-Host "[ERROR] Installer script ($issFile) not found!"
    exit 1
}

Write-Host "[INFO] Building the Setup executable... This may take a minute."
& $isccPath $issFile

if ($LASTEXITCODE -eq 0) {
    Write-Host "=========================================================================="
    Write-Host " SUCCESS: Setup executable has been created!"
    Write-Host " Location: dist\installer\ShopManager_Installer_v2.0.exe"
    Write-Host " You can now send this .exe to other users to install the application."
    Write-Host "=========================================================================="
} else {
    Write-Host "=========================================================================="
    Write-Host " ERROR: Failed to build setup."
    Write-Host "=========================================================================="
    exit 1
}
