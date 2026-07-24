param(
    [string] $EnvFile = (Join-Path $PSScriptRoot ".env"),
    [string] $SqlServer = $env:FX_SQLSERVER_HOST,
    [string] $SqlUser = $(if ($env:FX_SQLSERVER_USER) { $env:FX_SQLSERVER_USER } else { "sa" }),
    [string] $SqlPassword = $env:FX_SQLSERVER_PASSWORD,
    [string] $Database = $(if ($env:FX_VENDOR_DATABASE) { $env:FX_VENDOR_DATABASE } else { "vendor" })
)

$ErrorActionPreference = "Stop"

function Get-EnvValue {
    param([string] $Name, [string] $Default = "")
    if (Test-Path -LiteralPath $EnvFile) {
        foreach ($line in Get-Content -LiteralPath $EnvFile -Encoding UTF8) {
            $trimmed = $line.Trim()
            if (-not $trimmed -or $trimmed.StartsWith("#")) { continue }
            $idx = $trimmed.IndexOf("=")
            if ($idx -ge 1 -and $trimmed.Substring(0, $idx).Trim() -eq $Name) {
                return $trimmed.Substring($idx + 1).Trim()
            }
        }
    }
    return $Default
}

if (-not (Get-Command sqlcmd -ErrorAction SilentlyContinue)) { throw "Missing required command: sqlcmd" }
if (-not $SqlServer) { $SqlServer = Get-EnvValue "FX_SQLSERVER_HOST" "" }
if (-not $SqlPassword) { $SqlPassword = Get-EnvValue "FX_SQLSERVER_PASSWORD" "" }
if (-not $Database) { $Database = Get-EnvValue "FX_VENDOR_DATABASE" "vendor" }
if (-not $SqlServer -or -not $SqlPassword) { throw "FX_SQLSERVER_HOST and FX_SQLSERVER_PASSWORD are required." }

$sqlDir = Join-Path (Split-Path -Parent $PSScriptRoot) "script\sql\sqlserver"
$migrationFiles = @(
    (Join-Path $sqlDir "carbon_vendor_103_publication_20260724.sql"),
    (Join-Path $sqlDir "vendor_dimension_source_a_alignment.sql")
)
Write-Host "==> Applying non-destructive vendor database migrations" -ForegroundColor Cyan
foreach ($migrationFile in $migrationFiles) {
    sqlcmd -S $SqlServer -U $SqlUser -P $SqlPassword -C -b -d $Database -i $migrationFile
    if ($LASTEXITCODE -ne 0) { throw "Vendor database migration failed: $migrationFile" }
}
