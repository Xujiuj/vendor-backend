param(
    [string] $EnvFile = (Join-Path $PSScriptRoot ".env"),
    [switch] $Fresh,
    [switch] $IncludeSourceA,
    [switch] $SkipBuild,
    [switch] $SkipDbInit,
    [switch] $SkipDbMigration
)

$ErrorActionPreference = "Stop"
$ComposeFile = Join-Path $PSScriptRoot "docker-compose.yml"

if (-not (Test-Path -LiteralPath $EnvFile)) {
    throw "Missing env file: $EnvFile. Copy deploy\.env.example to deploy\.env and configure it first."
}
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "Missing required command: docker"
}

if (-not $SkipBuild) {
    & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot "build-image.ps1") -EnvFile $EnvFile
    if ($LASTEXITCODE -ne 0) { throw "build-image.ps1 failed" }
}

if ($Fresh -and -not $SkipDbInit) {
    $initArgs = @("-NoProfile", "-ExecutionPolicy", "Bypass", "-File", (Join-Path $PSScriptRoot "init-sqlserver.ps1"), "-EnvFile", $EnvFile)
    if ($IncludeSourceA) { $initArgs += "-IncludeSourceA" }
    & powershell @initArgs
    if ($LASTEXITCODE -ne 0) { throw "init-sqlserver.ps1 failed" }
} elseif (-not $SkipDbMigration) {
    & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot "migrate-sqlserver.ps1") -EnvFile $EnvFile
    if ($LASTEXITCODE -ne 0) { throw "migrate-sqlserver.ps1 failed" }
}

Write-Host "==> Starting vendor backend" -ForegroundColor Cyan
docker compose --env-file $EnvFile -f $ComposeFile up -d
if ($LASTEXITCODE -ne 0) { throw "docker compose up failed" }

docker compose --env-file $EnvFile -f $ComposeFile ps
Write-Host "Vendor backend deployment complete."
