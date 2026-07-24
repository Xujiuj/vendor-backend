#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
SQL_DIR="$WORKSPACE_ROOT/deploy/sqlserver"
ENV_FILE="${ENV_FILE:-$SCRIPT_DIR/.env}"
INCLUDE_SOURCE_A="${INCLUDE_SOURCE_A:-false}"

env_value() {
  local name="$1"
  local default="${2:-}"
  local file_value=""
  if [[ -f "$ENV_FILE" ]]; then
    file_value="$(grep -E "^${name}=" "$ENV_FILE" | tail -n 1 | cut -d '=' -f 2- || true)"
  fi
  if [[ -n "${!name:-}" ]]; then
    printf '%s' "${!name}"
  elif [[ -n "$file_value" ]]; then
    printf '%s' "$file_value"
  else
    printf '%s' "$default"
  fi
}

command -v sqlcmd >/dev/null 2>&1 || { echo "Missing required command: sqlcmd" >&2; exit 1; }

SQL_SERVER="$(env_value FX_SQLSERVER_HOST "")"
SQL_USER="$(env_value FX_SQLSERVER_USER sa)"
SQL_PASSWORD="$(env_value FX_SQLSERVER_PASSWORD "")"
DATABASE="$(env_value FX_VENDOR_DATABASE vendor)"

if [[ -z "$SQL_SERVER" || -z "$SQL_PASSWORD" ]]; then
  echo "FX_SQLSERVER_HOST and FX_SQLSERVER_PASSWORD are required." >&2
  exit 1
fi

echo "==> Ensuring database $DATABASE exists"
sqlcmd -S "$SQL_SERVER" -U "$SQL_USER" -P "$SQL_PASSWORD" -C -b -d master \
  -Q "IF DB_ID(N'$DATABASE') IS NULL EXEC(N'CREATE DATABASE [$DATABASE] COLLATE Chinese_PRC_CI_AS');"

echo "==> Running vendor initialization data"
(cd "$SQL_DIR" && sqlcmd -S "$SQL_SERVER" -U "$SQL_USER" -P "$SQL_PASSWORD" -C -b -d "$DATABASE" -i "vendor-init.sql")
sqlcmd -S "$SQL_SERVER" -U "$SQL_USER" -P "$SQL_PASSWORD" -C -b -d "$DATABASE" \
  -i "$SCRIPT_DIR/../script/sql/sqlserver/carbon_vendor_103_publication_20260724.sql"
(cd "$SQL_DIR" && sqlcmd -S "$SQL_SERVER" -U "$SQL_USER" -P "$SQL_PASSWORD" -C -b -d "$DATABASE" -i "session-timeout-1h.sql")

if [[ "$INCLUDE_SOURCE_A" == "true" ]]; then
  echo "==> Initializing source(A) bridge sample database"
  sqlcmd -S "$SQL_SERVER" -U "$SQL_USER" -P "$SQL_PASSWORD" -C -b -i "$SQL_DIR/source_a_bridge/source_a_bridge_init.sql"
fi

echo "Vendor SQL Server initialization complete."
