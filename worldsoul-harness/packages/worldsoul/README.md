---
description: "Package map for the WorldSoul Minecraft integration: its version-independent HTTP gateway, Agent lifecycle, command proposals, speech input, and token limits."
kind: "package-group"
---

# worldsoul/ — Minecraft integration

English | [中文](README.zh.md)

## Summary

The `worldsoul/` group connects Minecraft mods to DeepSeek Harness through a version-independent HTTP protocol. The gateway owns save-global and isolated player Agents, text and voice turns, model command proposals, private validation feedback, and per-Agent token limits. Minecraft-version modules remain responsible for command parsing, authorization, execution, and game-facing presentation.

## Table of Contents

- [Packages](#packages)
- [Related documentation](#related-documentation)
- [Dev Note](#dev-note)

-----

<a id="packages"></a>
## Packages

| Package | Role |
|---|---|
| [`gateway/`](gateway/README.md) | Serves the WorldSoul protocol, manages Agent lifecycles and quotas, and exposes the monitoring dashboard |

-----

<a id="related-documentation"></a>
## Related documentation

- [WorldSoul gateway decision](../../.agents/notes/implemented/feature/2026-09-05-worldsoul-minecraft-gateway.md) — protocol authority, isolation, speech, validation feedback, and quota rationale.

<a id="dev-note"></a>
## Dev Note

<details>
<summary>Working context for maintainers — click to expand</summary>

None.

</details>
