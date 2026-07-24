#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${ENV_FILE:-$SCRIPT_DIR/.env}"
SQL_DIR="$SCRIPT_DIR/../script/sql/sqlserver"
MIGRATION_FILES=(
  "$SQL_DIR/carbon_vendor_103_publication_20260724.sql"
  "$SQL_DIR/vendor_dimension_source_a_alignment.sql"
)

env_value() {
  local name="$1"
  local default="${2:-}"
  local file_value=""
  if [[ -f "$ENV_FILE" ]]; then
    file_value="$(grep -E "^${name}=" "$ENV_FILE" | tail -n 1 | cut -d '=' -f 2- || true)"
  fi
  if [[ -n "${!name:-}" ]]; then printf '%s' "${!name}"
  elif [[ -n "$file_value" ]]; then printf '%s' "$file_value"
  else printf '%s' "$default"
  fi
}

command -v sqlcmd >/dev/null 2>&1 || { echo "Missing required command: sqlcmd" >&2; exit 1; }
SQL_SERVER="$(env_value FX_SQLSERVER_HOST "")"
SQL_USER="$(env_value FX_SQLSERVER_USER sa)"
SQL_PASSWORD="$(env_value FX_SQLSERVER_PASSWORD "")"
DATABASE="$(env_value FX_VENDOR_DATABASE vendor)"
[[ -n "$SQL_SERVER" && -n "$SQL_PASSWORD" ]] || { echo "FX_SQLSERVER_HOST and FX_SQLSERVER_PASSWORD are required." >&2; exit 1; }

echo "==> Applying non-destructive vendor database migrations"
for migration_file in "${MIGRATION_FILES[@]}"; do
  sqlcmd -S "$SQL_SERVER" -U "$SQL_USER" -P "$SQL_PASSWORD" -C -b -d "$DATABASE" -i "$migration_file"
done
