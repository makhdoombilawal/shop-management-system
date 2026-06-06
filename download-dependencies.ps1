# Download Maven Dependencies for Shop Management System
# This script downloads all required dependencies from Maven Central Repository

param(
    [string]$OutputDir = ""
)

if (-not $OutputDir) {
    $OutputDir = "$(Get-Location)\lib"
}

# Create lib directory if it doesn't exist
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

Write-Host "========================================================================"
Write-Host "Downloading Maven Dependencies"
Write-Host "========================================================================"
Write-Host "Output directory: $OutputDir"
Write-Host ""

# Maven Central Repository URL
$MavenCentral = "https://repo1.maven.org/maven2"

# Define dependencies as hashtable of groupId/artifactId/version
# Keys: jar filename, Values: URL path
$dependencies = @{
    # Hibernate ORM
    "hibernate-core-5.6.15.Final.jar" = "$MavenCentral/org/hibernate/hibernate-core/5.6.15.Final/hibernate-core-5.6.15.Final.jar"
    "hibernate-commons-annotations-5.1.2.Final.jar" = "$MavenCentral/org/hibernate/common/hibernate-commons-annotations/5.1.2.Final/hibernate-commons-annotations-5.1.2.Final.jar"
    "jboss-logging-3.4.3.Final.jar" = "$MavenCentral/org/jboss/logging/jboss-logging/3.4.3.Final/jboss-logging-3.4.3.Final.jar"
    "jboss-classfilewriter-1.2.4.Final.jar" = "$MavenCentral/org/jboss/jboss-classfilewriter/1.2.4.Final/jboss-classfilewriter-1.2.4.Final.jar"
    "javax.persistence-api-2.2.jar" = "$MavenCentral/javax/persistence/javax.persistence-api/2.2/javax.persistence-api-2.2.jar"
    "antlr-2.7.7.jar" = "$MavenCentral/antlr/antlr/2.7.7/antlr-2.7.7.jar"
    "byte-buddy-1.12.22.jar" = "$MavenCentral/net/bytebuddy/byte-buddy/1.12.22/byte-buddy-1.12.22.jar"
    "jakarta.xml.bind-api-2.3.3.jar" = "$MavenCentral/jakarta/xml/bind/jakarta.xml.bind-api/2.3.3/jakarta.xml.bind-api-2.3.3.jar"
    "jakarta.activation-api-1.2.2.jar" = "$MavenCentral/jakarta/activation/jakarta.activation-api/1.2.2/jakarta.activation-api-1.2.2.jar"
    
    # MySQL Connector
    "mysql-connector-java-8.0.33.jar" = "$MavenCentral/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar"
    "protobuf-java-3.21.9.jar" = "$MavenCentral/com/google/protobuf/protobuf-java/3.21.9/protobuf-java-3.21.9.jar"
    
    # SQLite JDBC
    "sqlite-jdbc-3.42.0.0.jar" = "$MavenCentral/org/xerial/sqlite-jdbc/3.42.0.0/sqlite-jdbc-3.42.0.0.jar"
    
    # FlatLaf
    "flatlaf-3.2.5.jar" = "$MavenCentral/com/formdev/flatlaf/3.2.5/flatlaf-3.2.5.jar"
    
    # Barcode4J
    "barcode4j-2.1.jar" = "$MavenCentral/net/sf/barcode4j/barcode4j/2.1/barcode4j-2.1.jar"
    "commons-lang-2.6.jar" = "$MavenCentral/commons-lang/commons-lang/2.6/commons-lang-2.6.jar"
    "commons-io-2.11.0.jar" = "$MavenCentral/commons-io/commons-io/2.11.0/commons-io-2.11.0.jar"
    "avalon-framework-impl-4.3.1.jar" = "$MavenCentral/org/apache/avalon/framework/avalon-framework-impl/4.3.1/avalon-framework-impl-4.3.1.jar"
    "avalon-framework-api-4.3.1.jar" = "$MavenCentral/org/apache/avalon/framework/avalon-framework-api/4.3.1/avalon-framework-api-4.3.1.jar"
    "xmlgraphics-commons-2.7.jar" = "$MavenCentral/org/apache/xmlgraphics/xmlgraphics-commons/2.7/xmlgraphics-commons-2.7.jar"
    
    # BCrypt
    "jbcrypt-0.4.jar" = "$MavenCentral/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar"
    
    # SLF4J
    "slf4j-api-2.0.9.jar" = "$MavenCentral/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar"
    
    # Logback
    "logback-classic-1.4.11.jar" = "$MavenCentral/ch/qos/logback/logback-classic/1.4.11/logback-classic-1.4.11.jar"
    "logback-core-1.4.11.jar" = "$MavenCentral/ch/qos/logback/logback-core/1.4.11/logback-core-1.4.11.jar"
    
    # C3P0 Connection Pool
    "c3p0-0.9.5.5.jar" = "$MavenCentral/com/mchange/c3p0/0.9.5.5/c3p0-0.9.5.5.jar"
    "mchange-commons-java-0.2.20.jar" = "$MavenCentral/com/mchange/mchange-commons-java/0.2.20/mchange-commons-java-0.2.20.jar"
    
    # ZXing Barcode
    "core-3.5.2.jar" = "$MavenCentral/com/google/zxing/core/3.5.2/core-3.5.2.jar"
    "javase-3.5.2.jar" = "$MavenCentral/com/google/zxing/javase/3.5.2/javase-3.5.2.jar"
}

$totalCount = $dependencies.Count
$downloadedCount = 0
$failedCount = 0

Write-Host "Total dependencies to download: $totalCount`n"

foreach ($jarName in $dependencies.Keys) {
    $url = $dependencies[$jarName]
    $outputPath = Join-Path $OutputDir $jarName
    
    # Skip if already exists
    if (Test-Path $outputPath) {
        Write-Host "[SKIP] $jarName (already exists)"
        $downloadedCount++
        continue
    }
    
    try {
        Write-Host "[DOWNLOAD] $jarName... " -NoNewline
        $ProgressPreference = 'SilentlyContinue'
        Invoke-WebRequest -Uri $url -OutFile $outputPath -UseBasicParsing
        Write-Host "OK"
        $downloadedCount++
    }
    catch {
        Write-Host "FAILED"
        Write-Host "  URL: $url"
        Write-Host "  Error: $_"
        $failedCount++
    }
}

Write-Host "`n========================================================================"
Write-Host "Download Summary"
Write-Host "========================================================================"
Write-Host "Downloaded: $downloadedCount / $totalCount"
Write-Host "Failed: $failedCount"
Write-Host "Output directory: $OutputDir"
Write-Host ""

if ($failedCount -gt 0) {
    Write-Host "WARNING: Some dependencies failed to download. Please check your internet connection."
    Write-Host "You may need to manually download failed JARs from:"
    Write-Host "  https://repo1.maven.org/maven2/"
}
else {
    Write-Host "SUCCESS: All dependencies downloaded!"
}
