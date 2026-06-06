################################################################################
# Shop Management System v2.0 - Automated Build & Package Script
# Platform: Windows PowerShell
################################################################################

param(
    [string]$ProjectRoot = "",
    [switch]$SkipInstaller = $false
)

# If ProjectRoot not provided, try to determine it
if (-not $ProjectRoot -or $ProjectRoot -eq "") {
    if ($PSScriptRoot -and $PSScriptRoot.Trim().Length -gt 0) {
        $ProjectRoot = $PSScriptRoot
    } elseif ($MyInvocation.MyCommand.Path) {
        $ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
    } else {
        $ProjectRoot = (Get-Location).Path
    }
}

# Handle case where path ends with backslash
if ($ProjectRoot.EndsWith("\")) {
    $ProjectRoot = $ProjectRoot.Substring(0, $ProjectRoot.Length - 1)
}
$SrcDir = "$ProjectRoot\src"
$LibDir = "$ProjectRoot\lib"
$DistLibDir = "$ProjectRoot\dist\lib"
$DistDir = "$ProjectRoot\dist"
$BuildDir = "$ProjectRoot\build"
$ClassesDir = "$BuildDir\classes"
$MainClass = "shop.Shop"
$logFile = "$ProjectRoot\build.log"

# Clear log
"" | Out-File -FilePath $logFile -Force

function Log-Msg {
    param([string]$Message, [string]$Level = "INFO")
    $msg = "[$Level] $Message"
    Write-Host $msg
    Add-Content -Path $logFile -Value $msg
}

function Log-Section {
    param([string]$Title)
    Log-Msg "========================================================================="
    Log-Msg $Title
    Log-Msg "========================================================================="
}

Log-Section "SHOP MANAGEMENT SYSTEM v2.0 - BUILD STARTED"
Log-Msg "Project Root: $ProjectRoot"

# ============================================================================
# INITIALIZE BUILD
# ============================================================================

Log-Msg "Creating build directories..."
foreach ($dir in @($BuildDir, $ClassesDir, "$BuildDir\temp", $DistDir)) {
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
    }
}
Log-Msg "Directories initialized"

# ============================================================================
# FIND JAVA
# ============================================================================

Log-Msg "Searching for Java compiler..."
$javaFound = $false

if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
    $javacPath = "$env:JAVA_HOME\bin\javac.exe"
    $javaPath = "$env:JAVA_HOME\bin\java.exe"
    $jarPath = "$env:JAVA_HOME\bin\jar.exe"
    $javaFound = $true
    Log-Msg "Found Java in JAVA_HOME"
}

if (-not $javaFound) {
    $javac = Get-Command javac -ErrorAction SilentlyContinue
    if ($javac) {
        $javacPath = $javac.Source
        $javaPath = (Get-Command java -ErrorAction SilentlyContinue).Source
        $jarPath = (Get-Command jar -ErrorAction SilentlyContinue).Source
        $javaFound = $true
        Log-Msg "Found Java in PATH"
    }
}

if (-not $javaFound) {
    Log-Msg "ERROR: Java compiler not found!"
    Log-Msg "Please install JDK from: https://adoptium.net/"
    exit 1
}

Log-Msg "JavaC: $javacPath"

# ============================================================================
# BUILD CLASSPATH
# ============================================================================

Log-Msg "Building classpath..."
$classpathArray = @()

foreach ($depDir in @($LibDir, $DistLibDir)) {
    if (Test-Path $depDir) {
        $jars = Get-ChildItem -Path $depDir -Filter "*.jar" -ErrorAction SilentlyContinue
        if ($jars) {
            foreach ($jar in $jars) {
                $classpathArray += $jar.FullName
            }
            Log-Msg "Added $($jars.Length) JARs to classpath from $depDir"
        }
    }
}

if ($classpathArray.Count -eq 0) {
    Log-Msg "ERROR: No dependency JARs found in $LibDir or $DistLibDir"
    Log-Msg "Expected Hibernate/JPA/MySQL/FlatLaf libraries before compilation"
    Log-Msg "Hint: restore dependencies into lib/ or build with Maven on a machine with mvn installed"
    exit 1
}

$classpath = $classpathArray -join ";"
Log-Msg "Classpath ready"

# ============================================================================
# COMPILE SOURCE CODE
# ============================================================================

Log-Section "COMPILING SOURCE CODE"

if (-not (Test-Path $SrcDir)) {
    Log-Msg "ERROR: Source directory not found: $SrcDir"
    exit 1
}

# Ensure no stale classes survive when source list changes.
if (Test-Path $ClassesDir) {
    Remove-Item -Path "$ClassesDir\*" -Recurse -Force -ErrorAction SilentlyContinue
}

$javaFiles = @(Get-ChildItem -Path $SrcDir -Filter "*.java" -Recurse)
if ($javaFiles.Length -eq 0) {
    Log-Msg "ERROR: No Java files found in $SrcDir"
    exit 1
}

Log-Msg "Compiling..."

$sourceListFile = "$BuildDir\temp\sources-compile.txt"
$javaFiles | ForEach-Object {
    '"' + ($_.FullName -replace '\\', '/') + '"'
} | Out-File -FilePath $sourceListFile -Encoding ASCII -Force

# Create array of arguments for javac
$javacArgs = @(
    "-d", $ClassesDir,
    "-cp", ($classpathArray -join ";"),
    "@$sourceListFile"
)

# Run compiler with proper argument passing
& $javacPath $javacArgs 2>&1 | Tee-Object -FilePath $logFile -Append
$compileResult = $LASTEXITCODE

if ($compileResult -ne 0) {
    Log-Msg "ERROR: Compilation failed with code $compileResult"
    exit 1
}

Log-Msg "Compilation completed successfully"

# ============================================================================
# COPY RESOURCES
# ============================================================================

Log-Section "COPYING CONFIG FILES"

$resourceFiles = @(Get-ChildItem -Path $SrcDir -Recurse -File | Where-Object {
    $_.Extension -ne ".java" -and $_.Extension -ne ".class"
})
Log-Msg "Copying $($resourceFiles.Length) resource files..."

foreach ($file in $resourceFiles) {
    $relativePath = $file.FullName.Substring($SrcDir.Length + 1)
    $targetPath = "$ClassesDir\$relativePath"
    $targetDir = Split-Path -Parent $targetPath
    
    if (-not (Test-Path $targetDir)) {
        New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
    }
    
    Copy-Item -Path $file.FullName -Destination $targetPath -Force
}

Log-Msg "Resource files copied"

# ============================================================================
# CREATE JAR
# ============================================================================

Log-Section "CREATING JAR ARCHIVE"

$manifestDir = "$ClassesDir\META-INF"
if (-not (Test-Path $manifestDir)) {
    New-Item -ItemType Directory -Path $manifestDir -Force | Out-Null
}

$manifestPath = "$manifestDir\MANIFEST.MF"
$buildDate = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

@"
Manifest-Version: 1.0
Main-Class: $MainClass
Implementation-Title: Shop Management System
Implementation-Version: 2.0
Created-By: Build Script
Build-Date: $buildDate
"@ | Out-File -FilePath $manifestPath -Encoding ASCII -Force

Log-Msg "Creating JAR file..."

if (Test-Path $DistDir\shop-management.jar) {
    Copy-Item $DistDir\shop-management.jar $DistDir\shop-management.jar.backup -Force
    Log-Msg "Backed up existing JAR"
}

& $jarPath cfm "$DistDir\shop-management.jar" "$manifestPath" -C "$ClassesDir" . 2>&1 | Tee-Object -FilePath $logFile -Append

if ($LASTEXITCODE -ne 0) {
    Log-Msg "ERROR: JAR creation failed"
    exit 1
}

if (Test-Path "$DistDir\shop-management.jar") {
    $jarSize = (Get-Item "$DistDir\shop-management.jar").Length / 1MB
    Log-Msg "JAR created successfully: $($jarSize.ToString('F2')) MB"
} else {
    Log-Msg "ERROR: JAR file was not created"
    exit 1
}

# ============================================================================
# PREPARE DISTRIBUTION
# ============================================================================

Log-Section "PREPARING DISTRIBUTION"

Log-Msg "Copying config files to dist directory..."

if (Test-Path "$ProjectRoot\hibernate.cfg.xml") {
    Copy-Item "$ProjectRoot\hibernate.cfg.xml" "$DistDir\" -Force
    Log-Msg "Copied hibernate.cfg.xml"
} else {
    Log-Msg "Warning: hibernate.cfg.xml not found"
}

if (-not (Test-Path "$DistDir\lib")) {
    if (Test-Path $LibDir) {
        New-Item -ItemType Junction -Path "$DistDir\lib" -Target $LibDir -Force -ErrorAction SilentlyContinue | Out-Null
        if (-not (Test-Path "$DistDir\lib")) {
            Copy-Item -Path $LibDir -Destination "$DistDir\lib" -Recurse -Force
        }
        Log-Msg "Lib directory set up"
    }
}

Log-Msg "Distribution prepared"

# ============================================================================
# BUILD INSTALLER
# ============================================================================

if (-not $SkipInstaller) {
    Log-Section "BUILDING INSTALLER"
    
    $innoScript = "$ProjectRoot\installer_setup_enterprise.iss"
    
    if (Test-Path $innoScript) {
        $innoSetup = Get-Command iscc.exe -ErrorAction SilentlyContinue
        
        if ($innoSetup) {
            Log-Msg "Building installer..."
            & iscc.exe "$innoScript" 2>&1 | Tee-Object -FilePath $logFile -Append
            
            if ($LASTEXITCODE -eq 0) {
                Log-Msg "Installer built successfully"
                if (Test-Path "$DistDir\ShopManager_Installer_v2.0.exe") {
                    $exeSize = (Get-Item "$DistDir\ShopManager_Installer_v2.0.exe").Length / 1MB
                    Log-Msg "Installer size: $($exeSize.ToString('F2')) MB"
                }
            } else {
                Log-Msg "WARNING: Installer build failed"
            }
        } else {
            Log-Msg "Inno Setup not found - skipping installer build"
            Log-Msg "Install from: https://jrsoftware.org/isdl.php"
        }
    } else {
        Log-Msg "Installer script not found: $innoScript"
    }
}

# ============================================================================
# VERIFY BUILD
# ============================================================================

Log-Section "VERIFYING BUILD"

$buildOK = $true

if (Test-Path "$DistDir\shop-management.jar") {
    Log-Msg "JAR file exists"
} else {
    Log-Msg "ERROR: JAR file not found"
    $buildOK = $false
}

if (Test-Path "$DistDir\hibernate.cfg.xml") {
    Log-Msg "Config file present"
} else {
    Log-Msg "WARNING: Config file missing"
}

if (Test-Path "$DistDir\lib") {
    $jarCount = @(Get-ChildItem "$DistDir\lib" -Filter "*.jar" -ErrorAction SilentlyContinue).Length
    Log-Msg "Lib directory has $jarCount JARs"
} else {
    Log-Msg "ERROR: Lib directory not found"
    $buildOK = $false
}

# ============================================================================
# COMPLETION
# ============================================================================

if ($buildOK) {
    Log-Section "BUILD COMPLETED SUCCESSFULLY"
    Log-Msg "Next steps:"
    Log-Msg "  1. Test: java -jar dist\shop-management.jar"
    Log-Msg "  2. Install: dist\ShopManager_Installer_v2.0.exe"
    Log-Msg "  3. Run and verify database connection"
    exit 0
} else {
    Log-Section "BUILD FAILED - CHECK ERRORS ABOVE"
    exit 1
}
