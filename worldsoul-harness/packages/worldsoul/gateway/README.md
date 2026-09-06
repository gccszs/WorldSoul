---
description: "Reference for the version-independent WorldSoul Minecraft HTTP gateway, Agent lifecycle, command validation feedback, speech, and token quotas."
kind: "package-reference"
---

# `@deepseek-ai/dsh-worldsoul-gateway`

English | [中文](README.zh.md)

## Summary

This package exposes a Minecraft-version-independent HTTP protocol from a DeepSeek Harness profile. One save owns a global Agent. Each online player owns an isolated child Agent under that global Agent and is disposed when the player disconnects. The gateway also owns voice transcription routing, token quotas, and a loopback monitoring dashboard.

## Table of Contents

- [Protocol and authority](#protocol-and-authority)
- [Configuration](#configuration)
- [Model Experience](#model-experience)
- [Known Limitations and Deferred Work](#known-limitations-and-deferred-work)
- [Dev Note](#dev-note)

-----

<a id="protocol-and-authority"></a>
## Protocol and authority

Minecraft remains the command authority. The `minecraft_command` tool records proposals only; the mod validates and executes a proposal, then reports acceptance or rejection. Rejection detail enters the Agent's private next turn and is never included in the Minecraft-facing `speech` field.

The gateway reads `agentDefaultModel` whenever it creates or resumes a save or player Agent and pins that provider and model in the Agent options. The profile therefore fails to activate without the shared default-model service instead of accepting chat turns that cannot reach a model. A provider authentication failure returns HTTP 503 with a safe configuration diagnostic instead of an empty successful reply.

The dashboard at the configured `pathPrefix` provides an operator console with service metrics, save navigation, global and player Agent state, token usage, threshold controls, and disabled state. It refreshes from `GET <pathPrefix>/v1/status` without adding a Minecraft-version dependency.

The console also exposes an Agent configuration panel backed by `GET` and `PUT <pathPrefix>/v1/agent-config`. It persists the default DeepSeek model and reasoning effort through `agentDefaultModel` and can rotate `DEEPSEEK_API_KEY` through the credential provider. Reads expose only credential presence, source, and writability; secret values never leave the provider. Credential changes affect the next request, while model changes affect Agents created or reconnected after the update.

Player personas are live selections rather than creation-time constants. `PUT <pathPrefix>/v1/worlds/{world}/players/{player}/mode` switches guide, deity, or troll mode without clearing the session. The online and chat bodies accept a version-neutral `playerState`; the separate `/state` endpoint refreshes the same snapshot before binary voice uploads. Dimension, coordinates, yaw, pitch, facing, health, food, game mode, ground state, selected hotbar slot, 36-slot main inventory, and six equipment slots are rendered into the next prompt without entering the public conversation projection. Item facts contain only registry ids, counts, and durability; player-authored custom names are excluded from the prompt boundary.

`GET` and `PUT <pathPrefix>/v1/speech-config` manage the OpenAI-compatible transcription endpoint, model, timeout, credential reference, and optional credential rotation. The shipped profile defaults to the loopback FireRedASR-AED-L service, which requires no API credential. Settings persist through the shared settings service and apply to the next voice request. Credential reads remain value-free, and malformed WAV data is rejected before calling the provider. `POST <player>/voice/transcribe` returns the transcript without starting an Agent turn so the mod can echo it, show its thinking indicator, and only then submit the transcript to `<player>/chat`; the combined `<player>/voice` endpoint remains available for older clients.

Each player row opens a live conversation room. The room polls `GET <pathPrefix>/v1/worlds/{world}/players/{player}/conversation?after={revision}` and appends plain-text messages after the returned cursor. The projection reads the durable player session, exposes direct player messages and Agent replies, and excludes plugin feedback, tool traffic, and command-validation details.

<a id="configuration"></a>
## Configuration

`pathPrefix` selects the route prefix. `maxBodyBytes` and `maxAudioBytes` bound requests. `defaultTokenLimit` is the initial per-Agent hard limit. `warningRatio` selects the near-limit state. `maxCommandChars` bounds one command proposal before Minecraft validation. `speechEndpoint`, `speechModel`, `speechCredentialEnv`, and `speechTimeoutMs` configure an OpenAI-compatible transcription provider. The default endpoint is `http://127.0.0.1:9001/v1/audio/transcriptions`, the default model field is `FireRedASR-AED-L`, and the default credential reference is empty.

<a id="model-experience"></a>
## Model Experience

### Player persona and command tool

#### What the model sees

The player's selected plain-text mode persona and the `minecraft_command` tool. The tool can only create a proposal awaiting game-side validation.

#### Token effect

The complete persona replaces other prompt sections for the player's Agent. Tool calls and private validation feedback are retained in that player's session only.

#### KV Cache effect

Each player's prompt prefix is stable for the lifetime of that online Agent. Changing mode currently requires reconnecting that player Agent.

## Known Limitations and Deferred Work

<a id="known-limitations-and-deferred-work"></a>

- **Mode changes recreate context** — The online endpoint preserves an existing Agent, so a different mode takes effect after that player disconnects and reconnects.
- **First speech setup downloads large artifacts** — The local FireRedASR deployment uses an isolated Python environment, CUDA PyTorch, and approximately 4.7 GB of model weights. Text chat remains available while the model is absent or stopped.

No runtime invariant companion is published because the runtime owns each world and player entry in one map and applies lifecycle and quota transitions synchronously; its behavioral tests cover those relationships.

<a id="dev-note"></a>
### Dev Note

<details>
<summary>Working context for maintainers — click to expand</summary>

None.

</details>
