param(
    [string] $EnvFile = (Join-Path $PSScriptRoot ".env"),
    [string] $SqlServer = $env:FX_SQLSERVER_HOST,
    [string] $SqlUser = $(if ($env:FX_SQLSERVER_USER) { $env:FX_SQLSERVER_USER } else { "sa" }),
    [string] $SqlPassword = $env:FX_SQLSERVER_PASSWORD,
    [string] $Database = $(if ($env:FX_VENDOR_DATABASE) { $env:FX_VENDOR_DATABASE } else { "vendor" }),
    [switch] $IncludeSourceA
)

$ErrorActionPreference = "Stop"
$WorkspaceRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$SqlDir = Join-Path $WorkspaceRoot "deploy\sqlserver"

function Get-EnvValue {
    param([string] $Name, [string] $Default = "")
    if (Test-Path -LiteralPath $EnvFile) {
        foreach ($line in Get-Content -LiteralPath $EnvFile -Encoding UTF8) {
            $trimmed = $line.Trim()
            if (-not $trimmed -or $trimmed.StartsWith("#")) { continue }
            $idx = $trimmed.IndexOf("=")
            if ($idx -lt 1) { continue }
            if ($trimmed.Substring(0, $idx).Trim() -eq $Name) {
                return $trimmed.Substring($idx + 1).Trim()
            }
        }
    }
    return $Default
}

if (-not (Get-Command sqlcmd -ErrorAction SilentlyContinue)) {
    throw "Missing required command: sqlcmd"
}
if (-not $SqlServer) { $SqlServer = Get-EnvValue "FX_SQLSERVER_HOST" "" }
if (-not $SqlPassword) { $SqlPassword = Get-EnvValue "FX_SQLSERVER_PASSWORD" "" }
if (-not $Database) { $Database = Get-EnvValue "FX_VENDOR_DATABASE" "vendor" }
if (-not $SqlServer -or -not $SqlPassword) {
    throw "FX_SQLSERVER_HOST and FX_SQLSERVER_PASSWORD are required."
}

Write-Host "==> Ensuring database $Database exists" -ForegroundColor Cyan
$ensureDbQuery = "IF DB_ID(N'$Database') IS NULL EXEC(N'CREATE DATABASE [$Database] COLLATE Chinese_PRC_CI_AS');"
sqlcmd -S $SqlServer -U $SqlUser -P $SqlPassword -C -b -d master -Q $ensureDbQuery
if ($LASTEXITCODE -ne 0) { throw "Failed to ensure database exists" }

Write-Host "==> Running vendor initialization data" -ForegroundColor Cyan
Push-Location $SqlDir
sqlcmd -S $SqlServer -U $SqlUser -P $SqlPassword -C -b -d $Database -i "vendor-init.sql"
if ($LASTEXITCODE -ne 0) { throw "vendor initialization failed" }
$publicationMigration = Join-Path $PSScriptRoot "..\script\sql\sqlserver\carbon_vendor_103_publication_20260724.sql"
sqlcmd -S $SqlServer -U $SqlUser -P $SqlPassword -C -b -d $Database -i $publicationMigration
if ($LASTEXITCODE -ne 0) { throw "103 publication initialization failed" }
sqlcmd -S $SqlServer -U $SqlUser -P $SqlPassword -C -b -d $Database -i "session-timeout-1h.sql"
if ($LASTEXITCODE -ne 0) { throw "session timeout initialization failed" }
Pop-Location

if ($IncludeSourceA) {
    Write-Host "==> Initializing source(A) bridge sample database" -ForegroundColor Cyan
    $sourceSql = Join-Path $SqlDir "source_a_bridge\source_a_bridge_init.sql"
    sqlcmd -S $SqlServer -U $SqlUser -P $SqlPassword -C -b -i $sourceSql
    if ($LASTEXITCODE -ne 0) { throw "source(A) bridge initialization failed" }
}

Write-Host "Vendor SQL Server initialization complete."
