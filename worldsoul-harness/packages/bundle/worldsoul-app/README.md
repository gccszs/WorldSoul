---
description: "Standalone loopback WorldSoul profile bundle for Minecraft text, voice, command proposals, and Agent quota monitoring."
kind: "package-bundle"
---

# `@deepseek-ai/dsh-worldsoul-app`

English | [中文](README.zh.md)

## Summary

This bundle adds the loopback Web server and WorldSoul gateway to `dsh-base`. The shipped `worldsoul` profile starts it with `dsh --profile worldsoul`; `WORLDSOUL_PORT` selects the listen port and defaults to `8787`. It is the independently deployable local service used by supported Minecraft mods.

## Table of Contents

- [Use this package](#use-this-package)
- [Understand the implementation](#understand-the-implementation)
- [Model Experience](#model-experience)
- [Known Limitations and Deferred Work](#known-limitations-and-deferred-work)
- [Dev Note](#dev-note)

-----

<a id="use-this-package"></a>
## Use this package

Launch `dsh --profile worldsoul` with a configured DeepSeek credential. Set `WORLDSOUL_PORT` when port 8787 is unavailable, and point the mod's Harness URL at the matching `/worldsoul` prefix. Speech settings are optional and independent from text chat.

-----

<a id="understand-the-implementation"></a>
## Understand the implementation

<details>
<summary>Implementation internals — click to expand</summary>

The bundle inserts the Host Web server and `@deepseek-ai/dsh-worldsoul-gateway` above the shared base tree. The gateway package owns runtime behavior; this package only selects and configures the plugin rows.

### Source map

| File | Role |
|---|---|
| [`cordis.patch.yml`](cordis.patch.yml) | Loopback server and environment-backed WorldSoul gateway configuration |
| [`src/index.ts`](src/index.ts) | Bundle package entry |
| — | No runtime invariant companion is published because the bundle is a static patch-list carrier whose inserted plugins own their runtime relationships and checks. |

</details>

-----

<a id="model-experience"></a>
## Model Experience

### Mounted WorldSoul gateway

#### What the model sees

The bundle mounts `@deepseek-ai/dsh-worldsoul-gateway`, which owns the player persona and command tool visible to the model.

#### Token effect

The bundle adds no tokens itself; the mounted gateway owns prompt, tool, and validation-feedback tokens.

#### KV Cache effect

The bundle adds no request prefix of its own; the mounted gateway owns each player's stable prompt prefix.

## Known Limitations and Deferred Work

<a id="known-limitations-and-deferred-work"></a>

- **Loopback only** — Remote Minecraft servers require an explicit profile patch with an authenticated transport before binding to all interfaces.

<a id="dev-note"></a>
### Dev Note

<details>
<summary>Working context for maintainers — click to expand</summary>

None.

</details>
