# Vendor backend deployment

This directory contains the standalone delivery scripts for the vendor backend only.

## Files

- `.env.example`: copy to `.env` and fill database, Redis, public URL, payment, and license secret settings.
- `build-image.ps1` / `build-image.sh`: build the backend JAR and Docker image.
- `init-sqlserver.ps1` / `init-sqlserver.sh`: create the vendor database if needed and load maintained initialization data.
- `deploy.ps1` / `deploy.sh`: build, optionally initialize SQL Server, and start the vendor backend compose service.
- `docker-compose.yml`: vendor backend plus its Redis sidecar.

## Windows

```powershell
Copy-Item .env.example .env
.\deploy.ps1 -Fresh
```

## Linux

```bash
cp .env.example .env
chmod +x ./*.sh
FRESH=true ./deploy.sh
```

Set `SKIP_BUILD=true` when the image has already been built. Set `INCLUDE_SOURCE_A=true` with `FRESH=true` to initialize the source(A) bridge sample database.
