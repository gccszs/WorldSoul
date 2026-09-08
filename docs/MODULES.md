# WorldSoul Modules

This repository contains a Gradle multi-project for per-Minecraft-version Fabric mods and a separate Node.js WorldSoul Harness. See [VERSIONING.md](VERSIONING.md) for the versioning model and [WORLDSOUL_HARNESS.md](WORLDSOUL_HARNESS.md) for Harness deployment.

| Module | Path | Role |
|--------|------|------|
| `agent-core` | `agent-core/` | Shared Java-side types retained by the Fabric modules. |
| WorldSoul Harness | `worldsoul-harness/` | DeepSeek Harness-based, Minecraft-version-independent Agent service and dashboard. |
| `mod-common` | `mod-common/` | Shared Mod-to-Harness HTTP client, command policy, and audio encoding. |
| `mod-1.20.1` | `mod-1.20.1/` | Primary Fabric mod for Minecraft 1.20.1. |
| `mod-1.20.4` | `mod-1.20.4/` | Fabric mod for Minecraft 1.20.4. |

## Build

```powershell
.\gradlew.bat buildAll
cd .\worldsoul-harness
pnpm exec vitest run packages/worldsoul/gateway/tests
pnpm run build:lib
```

## Run WorldSoul Harness

```powershell
.\scripts\start-worldsoul-harness.ps1 -Install -Build
```

Open `http://127.0.0.1:8787/worldsoul`. Stop the foreground service with Ctrl+C. The Mod uses the same URL by default; override it with `WORLDSOUL_HARNESS_URL` or disable the connection with `WORLDSOUL_HARNESS_ENABLED=false`.

## Run Minecraft clients

```powershell
.\gradlew.bat :mod-1.20.1:runClient
.\gradlew.bat :mod-1.20.4:runClient
```
