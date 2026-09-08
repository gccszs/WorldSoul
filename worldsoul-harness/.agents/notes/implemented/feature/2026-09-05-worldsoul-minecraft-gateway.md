# Agent Note: WorldSoul Minecraft gateway and player Agents

Status: implemented

English | [中文](2026-09-05-worldsoul-minecraft-gateway.zh.md)

## Problem

WorldSoul needs a locally deployable Agent service that does not depend on a Minecraft protocol version. Chat and voice requests must share player context, while model-proposed commands remain subject to the game's command parser and authorization. A multiplayer save also needs isolation between players, lifecycle cleanup on disconnect, and enforceable token budgets with warnings visible in both the game and the service dashboard.

## Decision

The `worldsoul` profile composes a loopback Web server with `@deepseek-ai/dsh-worldsoul-gateway`. Its HTTP and JSON protocol identifies a save and player without importing Minecraft classes, so the same harness deployment serves supported mod versions. A save owns one durable global Agent identity, and each online player owns a durable child Agent identity and isolated session; disconnect disposes the live child runtime, while reconnect resumes its session.

Agent creation and resume read the shared `agentDefaultModel` selection and pass it as explicit Agent options. This keeps the long-lived Minecraft entry point aligned with profile model settings and prevents a request from entering a model-less turn. The gateway converts a failed model turn into a non-success HTTP response and classifies authentication failures without exposing provider details.

Text and in-memory multipart audio enter the same player turn path. Audio is transcribed through a configured OpenAI-compatible endpoint, and the response returns both the transcript and the Agent's plain-text speech. Guide mode gives concise survival and progression guidance, while deity mode exposes a command-oriented persona. Prompts prohibit Markdown and filler.

The model cannot execute Minecraft commands. Its scoped `minecraft_command` tool emits a proposal, the mod applies its allowlist and the Minecraft Brigadier parser, and only a fully parsed command runs at the configured permission level. Rejections return to the same player Agent as private validation feedback for bounded regeneration; the game-facing speech omits the rejection detail.

The gateway records token usage separately for every global and player Agent. Operators set hard limits in a console-style dashboard with service metrics, save navigation, Agent tables, and quota progress. Crossing the configured warning ratio returns a warning to the mod and dashboard; reaching the limit marks that Agent disabled and rejects later turns until an operator raises its limit. The dashboard exposes save and player state without depending on Minecraft rendering APIs.

The dashboard projects each player's durable session into a live conversation room with a monotonic sequence cursor. The projection includes direct player messages and plain-text Agent replies, but filters plugin-sourced validation feedback and tool traffic so private recovery details remain internal.

The same loopback dashboard provides an Agent configuration surface for the deployed DeepSeek route. It persists the default model and reasoning effort through the shared settings service and rotates `DEEPSEEK_API_KEY` through the credential service. Reads expose only presence, source, and writability; the key itself is never serialized into an HTTP response. Credential updates are request-live, while model updates apply when an Agent is next created or reconnected.

Player mode and game state are prompt-time inputs. A mode endpoint and the dashboard switch guide, deity, or troll behavior without recreating the session; the Fabric command `/worldsoul mode <mode>` uses the same route. The mod supplies a version-neutral snapshot containing dimension, coordinates, orientation, health, food, game mode, and ground state before each text or voice turn. The dynamic persona reads that snapshot without adding private state messages to the visible conversation.

The snapshot also carries the selected hotbar slot, occupied stacks from the 36-slot main inventory, and occupied head, chest, legs, feet, main-hand, and off-hand equipment slots. Each stack is reduced to a registry id, count, and optional durability. Player-authored display names and item text are deliberately excluded so inventory metadata cannot become prompt instructions.

Speech provider settings have their own persisted dashboard surface. The gateway resolves its endpoint, model, timeout, and credential reference for each upload, accepts credential rotation without returning the value, and validates RIFF/WAVE framing before making a provider call. The shipped profile defaults to a loopback OpenAI-compatible adapter backed by the official FireRedASR-AED-L source and weights. A project-local Python 3.10 environment isolates CUDA dependencies, the launcher starts the adapter before Harness, and the adapter validates bounded 16 kHz mono PCM WAV input before serial GPU inference. External providers remain selectable through the same settings. A split transcription route completes without starting an Agent turn; the mod echoes that result and its thinking indicator before it submits the transcript through the ordinary chat route. The original combined voice route remains additive compatibility for older clients.

Coverage combines prompt, request, speech-provider, lifecycle, isolation, validation-regeneration, and quota unit tests with a built-profile smoke test. The Fabric modules test the command policy, WAV encoding, chat routing, and supported-version metadata; Minecraft 1.20.1 is the primary build and 1.20.4 shares the version-neutral client code.

## Alternatives considered

- **Execute commands directly in the harness.** Rejected because the harness does not own the server's live registry, permission source, or version-specific parser, and would become an authority outside Minecraft.
- **Use one Agent session for every player in a save.** Rejected because conversation, private validation failures, and token accounting would leak across players.
- **Embed speech recognition in the mod.** Rejected because bundling a model increases client size and couples provider configuration to Minecraft; the mod records bounded WAV audio and the harness owns transcription.
- **Return command validation errors in ordinary chat.** Rejected because parser and authorization detail is recovery input for the Agent, not player-facing conversation.

## Consequences

- A new Minecraft version implements the same HTTP protocol and keeps command validation inside that version's server module.
- First-time speech setup downloads the pinned FireRedASR source, isolated CUDA runtime, and model weights; text chat and the dashboard remain available when the speech service is absent.
- Disconnect frees live Agent resources but retains durable player context. Deleting a world through the gateway disposes its global and player runtimes and removes their in-memory usage records.
- The current dashboard is loopback-only. A remote binding requires a deployment-specific authenticated transport rather than weakening the shipped profile.
