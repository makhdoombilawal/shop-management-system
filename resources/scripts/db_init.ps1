# ============================================================================
# Shop Management System - Database Initialization Script
# ============================================================================
# Initializes database (MySQL or SQLite) and creates default users
# Called automatically during installation
# ============================================================================

param(
    [string]$InstallPath = "C:\Program Files\ShopManagement",
    [string]$DBType = "auto",  # auto, mysql, sqlite
    [string]$MySQLHost = "127.0.0.1",
    [string]$MySQLPort = "3306",
    [string]$MySQLUser = "root",
    [string]$MySQLPassword = "root",
    [string]$MySQLDatabase = "shopdb"
)

$ErrorActionPreference = "Continue"
$LogFile = "$InstallPath\installer.log"

function Log {
    param([string]$Message)
    Write-Host $Message
    Add-Content -Path $LogFile -Value "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - $Message"
}

function Test-MySQLConnectivity {
    param([string]$Host, [string]$Port, [string]$User, [string]$Password)
    
    try {
        Log "Testing MySQL connectivity to ${Host}:${Port}..."
        
        # Test using mysql command line tool
        $mysql_cmd = "mysql -h $Host -P $Port -u $User"
        if ($Password) {
            $mysql_cmd += " -p$Password"
        }
        $mysql_cmd += " -e 'SELECT 1;' 2>&1"
        
        $result = (& cmd /c "$mysql_cmd") 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Log "MySQL connection successful"
            return $true
        } else {
            Log "MySQL connection failed: $result"
            return $false
        }
    } catch {
        Log "MySQL test error: $_"
        return $false
    }
}

function Initialize-Database {
    Log "=================================="
    Log "Database Initialization Starting"
    Log "=================================="
    
    # Determine database type
    $UseMySQL = $false
    
    if ($DBType -eq "mysql") {
        $UseMySQL = $true
    } elseif ($DBType -eq "sqlite") {
        $UseMySQL = $false
    } else {
        # Auto-detect: try MySQL first
        if (Get-Command mysql -ErrorAction SilentlyContinue) {
            if (Test-MySQLConnectivity -Host $MySQLHost -Port $MySQLPort -User $MySQLUser -Password $MySQLPassword) {
                $UseMySQL = $true
            }
        }
    }
    
    if ($UseMySQL) {
        Log "Using MySQL for database initialization"
        Initialize-MySQL
    } else {
        Log "Using SQLite for database initialization"
        Initialize-SQLite
    }
}

function Initialize-MySQL {
    try {
        Log "Creating MySQL database and tables..."
        
        # Check if mysql CLI is available
        if (-not (Get-Command mysql -ErrorAction SilentlyContinue)) {
            Log "MySQL command-line tool not found. Falling back to SQLite."
            Initialize-SQLite
            return
        }
        
        # Create database
        $create_db = @"
CREATE DATABASE IF NOT EXISTS $MySQLDatabase;
USE $MySQLDatabase;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'cashier',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    quantity INT DEFAULT 0,
    cost_price DECIMAL(10,2),
    selling_price DECIMAL(10,2),
    barcode VARCHAR(50) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sales transactions
CREATE TABLE IF NOT EXISTS sales (
    sale_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    total_amount DECIMAL(10,2),
    payment_method VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Sale details
CREATE TABLE IF NOT EXISTS sale_details (
    detail_id INT PRIMARY KEY AUTO_INCREMENT,
    sale_id INT,
    product_id INT,
    quantity INT,
    unit_price DECIMAL(10,2),
    total_price DECIMAL(10,2),
    FOREIGN KEY (sale_id) REFERENCES sales(sale_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Purchases
CREATE TABLE IF NOT EXISTS purchases (
    purchase_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    total_amount DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Purchase details
CREATE TABLE IF NOT EXISTS purchase_details (
    detail_id INT PRIMARY KEY AUTO_INCREMENT,
    purchase_id INT,
    product_id INT,
    quantity INT,
    unit_cost DECIMAL(10,2),
    total_cost DECIMAL(10,2),
    FOREIGN KEY (purchase_id) REFERENCES purchases(purchase_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Insert default users (passwords hashed with bcrypt in real implementation)
-- Admin: Bilawal / breakthewall
-- Manager: manager / manager123
-- Cashier: cashier / cashier123
INSERT IGNORE INTO users (user_id, username, password, full_name, role) VALUES
(1, 'admin', 'hashed_breakthewall_value', 'Bilawal Admin', 'admin'),
(2, 'manager', 'hashed_manager123_value', 'Manager Account', 'manager'),
(3, 'cashier', 'hashed_cashier123_value', 'Cashier Account', 'cashier');
"@
        
        # Execute SQL
        $create_db | mysql -h $MySQLHost -P $MySQLPort -u $MySQLUser -p$MySQLPassword 2>&1 | ForEach-Object { Log $_ }
        
        Log "MySQL database initialized successfully"
        
        # Update application.properties
        $propsFile = "$InstallPath\config\application.properties"
        if (Test-Path $propsFile) {
            $content = Get-Content $propsFile
            $content = $content -replace "db\.type=.*", "db.type=mysql"
            $content = $content -replace "db\.host=.*", "db.host=$MySQLHost"
            $content = $content -replace "db\.port=.*", "db.port=$MySQLPort"
            $content = $content -replace "db\.name=.*", "db.name=$MySQLDatabase"
            $content = $content -replace "db\.user=.*", "db.user=$MySQLUser"
            Set-Content -Path $propsFile -Value $content
            Log "Updated application.properties for MySQL"
        }
        
    } catch {
        Log "Error initializing MySQL: $_"
        Log "Falling back to SQLite"
        Initialize-SQLite
    }
}

function Initialize-SQLite {
    try {
        Log "Setting up SQLite database..."
        
        $dbPath = "$InstallPath\data\shopdb.db"
        $dataDir = Split-Path $dbPath
        
        if (-not (Test-Path $dataDir)) {
            New-Item -ItemType Directory -Path $dataDir -Force | Out-Null
        }
        
        # Create SQLite configuration
        $sqliteConfig = @"
# Shop Management System - SQLite Configuration
db.type=sqlite
db.path=$dbPath
db.driver=org.sqlite.JDBC
db.url=jdbc:sqlite:$dbPath
db.user=
db.password=
"@
        
        $propsFile = "$InstallPath\config\application.properties"
        Set-Content -Path $propsFile -Value $sqliteConfig
        Log "SQLite configuration created at $dbPath"
        
        # Note: SQLite database creation and table creation will be handled by the application on first run
        
    } catch {
        Log "Error initializing SQLite: $_"
    }
}

# Main execution
try {
    Log "Installation completed. Database initialization script executed."
    Initialize-Database
    Log "Database setup completed successfully"
} catch {
    Log "FATAL ERROR: $_"
    exit 1
}
