[CmdletBinding()]
param(
	[string]$ProjectRoot = "",
	[bool]$CleanupGenerated = $false,
	[bool]$RunDbChecks = $false,
	[bool]$SkipInstaller = $false
)

if (-not $ProjectRoot -or $ProjectRoot -eq "") {
	if ($PSScriptRoot -and $PSScriptRoot.Trim().Length -gt 0) {
		$ProjectRoot = $PSScriptRoot
	} else {
		$ProjectRoot = (Get-Location).Path
	}
}

function Read-ConfigValue {
	param(
		[string]$FilePath,
		[string]$Key,
		[string]$DefaultValue
	)

	if (-not (Test-Path $FilePath)) {
		return $DefaultValue
	}

	$line = Select-String -Path $FilePath -Pattern ("^" + [regex]::Escape($Key) + "=(.*)$") -CaseSensitive | Select-Object -First 1
	if ($line) {
		return $line.Matches[0].Groups[1].Value.Trim()
	}
	return $DefaultValue
}

function Remove-SafeGeneratedArtifacts {
	param([string]$Root)

	Write-Host "[INFO] Cleaning generated artifacts..."
	$paths = @(
		(Join-Path $Root "build"),
		(Join-Path $Root "temp_build"),
		(Join-Path $Root "runtime_analysis.log"),
		(Join-Path $Root "build.log")
	)

	foreach ($p in $paths) {
		if (Test-Path $p) {
			Remove-Item -Path $p -Recurse -Force -ErrorAction SilentlyContinue
			Write-Host "[INFO] Removed: $p"
		}
	}

	Get-ChildItem -Path $Root -Filter "comprehensive_test_results_*.log" -File -ErrorAction SilentlyContinue |
		ForEach-Object {
			Remove-Item -Path $_.FullName -Force -ErrorAction SilentlyContinue
			Write-Host "[INFO] Removed: $($_.FullName)"
		}
}

function Invoke-DbChecks {
	param([string]$Root)

	$mysqlCmd = Get-Command mysql -ErrorAction SilentlyContinue
	if (-not $mysqlCmd) {
		Write-Host "[WARN] MySQL CLI not found. Skipping automatic DB checks."
		return
	}

	$cfg = Join-Path $Root "config.properties"
	$databaseHost = Read-ConfigValue -FilePath $cfg -Key "db.mysql.host" -DefaultValue "127.0.0.1"
	$port = Read-ConfigValue -FilePath $cfg -Key "db.mysql.port" -DefaultValue "3306"
	$db = Read-ConfigValue -FilePath $cfg -Key "db.mysql.database" -DefaultValue "shop2"
	$user = Read-ConfigValue -FilePath $cfg -Key "db.mysql.username" -DefaultValue "root"
	$pass = Read-ConfigValue -FilePath $cfg -Key "db.mysql.password" -DefaultValue ""

	Write-Host "[INFO] Running DB schema and index checks on $db@${databaseHost}:$port ..."

	$schemaCheck = @"
SELECT 'missing_table:suppliers' AS issue
WHERE NOT EXISTS (
  SELECT 1 FROM information_schema.tables
  WHERE table_schema = '$db' AND table_name = 'suppliers'
)
UNION ALL
SELECT 'missing_column:transactions.supplier_id'
WHERE NOT EXISTS (
  SELECT 1 FROM information_schema.columns
  WHERE table_schema = '$db' AND table_name = 'transactions' AND column_name = 'supplier_id'
)
UNION ALL
SELECT 'missing_column:barcode.is_active'
WHERE NOT EXISTS (
  SELECT 1 FROM information_schema.columns
  WHERE table_schema = '$db' AND table_name = 'barcode' AND column_name = 'is_active'
)
UNION ALL
SELECT 'missing_index:transactions.idx_type_date'
WHERE NOT EXISTS (
  SELECT 1 FROM information_schema.statistics
  WHERE table_schema = '$db' AND table_name = 'transactions' AND index_name = 'idx_type_date'
)
UNION ALL
SELECT 'missing_index:barcode.idx_product_active'
WHERE NOT EXISTS (
  SELECT 1 FROM information_schema.statistics
  WHERE table_schema = '$db' AND table_name = 'barcode' AND index_name = 'idx_product_active'
);
"@

	& $mysqlCmd.Source --protocol=TCP -h $dbHost -P $port -u $user ("-p" + $pass) -D $db -N -e $schemaCheck
	if ($LASTEXITCODE -ne 0) {
		Write-Host "[WARN] Could not execute schema checks. Verify MySQL server and credentials."
		return
	}

	Write-Host "[INFO] Running query plan check (transactions daily totals)..."
	$explain = "EXPLAIN SELECT COALESCE(SUM(total_amount),0.0) FROM transactions WHERE transaction_type='SALE' AND transaction_date >= CURDATE() AND transaction_date < DATE_ADD(CURDATE(), INTERVAL 1 DAY);"
	& $mysqlCmd.Source --protocol=TCP -h $dbHost -P $port -u $user ("-p" + $pass) -D $db -e $explain

	Write-Host "[INFO] DB checks complete."
}

function Test-ConfigurationConsistency {
	param([string]$Root)

	Write-Host "[INFO] Checking configuration consistency..."

	$coreCfg = Join-Path $Root "config.properties"
	$appCfg = Join-Path $Root "dist\application.properties"

	if (Test-Path $coreCfg) {
		$mode = Read-ConfigValue -FilePath $coreCfg -Key "db.mode" -DefaultValue "AUTO"
		$sqliteFile = Read-ConfigValue -FilePath $coreCfg -Key "db.sqlite.file" -DefaultValue "shop_local.db"
		Write-Host "[INFO] Core settings: db.mode=$mode, db.sqlite.file=$sqliteFile"
	} else {
		Write-Host "[WARN] Missing core config: $coreCfg"
	}

	if (Test-Path $appCfg) {
		$legacyDbType = Read-ConfigValue -FilePath $appCfg -Key "db.type" -DefaultValue ""
		$legacySqlite = Read-ConfigValue -FilePath $appCfg -Key "db.sqlite.filename" -DefaultValue ""
		if ($legacyDbType -ne "") {
			Write-Host "[WARN] Legacy key detected in application settings: db.type=$legacyDbType (preferred: db.mode)"
		}
		if ($legacySqlite -ne "") {
			Write-Host "[WARN] Legacy key detected in application settings: db.sqlite.filename=$legacySqlite (preferred: db.sqlite.file)"
		}
	} else {
		Write-Host "[WARN] Missing dist application settings: $appCfg"
	}
}

Write-Host "[INFO] Project root: $ProjectRoot"

if ($CleanupGenerated) {
	Remove-SafeGeneratedArtifacts -Root $ProjectRoot
}

Test-ConfigurationConsistency -Root $ProjectRoot

if ($RunDbChecks) {
	Invoke-DbChecks -Root $ProjectRoot
}

Write-Host "[INFO] Running build pipeline..."
& (Join-Path $ProjectRoot "build-automated.ps1") -ProjectRoot $ProjectRoot -SkipInstaller:$SkipInstaller
if ($LASTEXITCODE -ne 0) {
	Write-Host "[ERROR] Build failed. Stopping automation."
	exit $LASTEXITCODE
}

Write-Host "[INFO] cleanup_and_build.ps1 completed."
