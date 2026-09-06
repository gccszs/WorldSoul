/** WorldSoul Minecraft gateway plugin for DeepSeek Harness. */

import type { Context } from '@deepseek-ai/cordis'
import type {} from '@deepseek-ai/dsh-agent'
import type {} from '@deepseek-ai/dsh-agent-default-model'
import type {} from '@deepseek-ai/dsh-host-webserver'
import z from '@deepseek-ai/schemastery'
import type {} from '@deepseek-ai/dsh-credentials'
import { credentialRef } from '@deepseek-ai/dsh-credentials'
import type {} from '@deepseek-ai/dsh-settings'
import { createWorldSoulHandler } from './handler.ts'
import { WorldSoulHttpError, WorldSoulRuntime, type SpeechConfiguration, type SpeechConfigurationUpdate } from './runtime.ts'
import { transcribeAudio } from './speech.ts'

export type * from './types.ts'
export { promptForMode, WORLD_AGENT_PROMPT } from './prompts.ts'
export { usageStatus, usedTokens } from './usage.ts'
export { WorldSoulRuntime, WorldSoulHttpError } from './runtime.ts'
export { createWorldSoulHandler } from './handler.ts'

/** Cordis function-plugin name. */
export const name = 'worldsoul-gateway'
/** Services required by the HTTP and Agent lifecycle adapter. */
export const inject = ['agentDefaultModel', 'agents', 'credentials', 'settings', 'webServer', 'systemPrompt', 'tools']

interface StoredSpeechConfiguration {
  readonly endpoint: string
  readonly model: string
  readonly credentialRef: string
  readonly timeoutMs: number
}

const SPEECH_SETTINGS_NAMESPACE = 'worldsoul-speech'
const SpeechSettingsSchema: z<StoredSpeechConfiguration> = z.object({
  endpoint: z.string().default(''),
  model: z.string().required(),
  credentialRef: z.string().default(''),
  timeoutMs: z.number().step(1).min(1).max(300_000),
})

/** WorldSoul gateway deployment configuration. */
export interface Config {
  /** Absolute non-root HTTP route prefix without a trailing slash. */
  readonly pathPrefix: string
  /** Maximum JSON request body size in bytes. */
  readonly maxBodyBytes: number
  /** Initial hard token limit for each global or player Agent. */
  readonly defaultTokenLimit: number
  /** Used-to-limit ratio at which near-limit warnings begin. */
  readonly warningRatio: number
  /** Maximum characters in one command proposal. */
  readonly maxCommandChars: number
  /** Maximum voice request body size in bytes. */
  readonly maxAudioBytes: number
  /** OpenAI-compatible audio transcription endpoint, or an empty string to disable speech. */
  readonly speechEndpoint: string
  /** Model field sent to the transcription endpoint. */
  readonly speechModel: string
  /** Credential reference resolved for the transcription bearer token, or empty for no authorization header. */
  readonly speechCredentialEnv: string
  /** Speech provider request timeout in milliseconds. */
  readonly speechTimeoutMs: number
}

/** Runtime configuration schema. */
export const Config: z<Config> = z.object({
  pathPrefix: z.string().default('/worldsoul'),
  maxBodyBytes: z.number().step(1).min(1).max(Number.MAX_SAFE_INTEGER).default(1_048_576),
  defaultTokenLimit: z.number().step(1).min(1).max(Number.MAX_SAFE_INTEGER).default(1_000_000),
  warningRatio: z.number().min(0.01).max(1).default(0.8),
  maxCommandChars: z.number().step(1).min(1).max(32_767).default(512),
  maxAudioBytes: z.number().step(1).min(1).max(Number.MAX_SAFE_INTEGER).default(1_048_576),
  speechEndpoint: z.string().default('http://127.0.0.1:9001/v1/audio/transcriptions'),
  speechModel: z.string().default('FireRedASR-AED-L'),
  speechCredentialEnv: z.string().default(''),
  speechTimeoutMs: z.number().step(1).min(1).max(Number.MAX_SAFE_INTEGER).default(60_000),
})

function assertConfig(config: Config): void {
  if (!config.pathPrefix.startsWith('/') || config.pathPrefix === '/' || config.pathPrefix.endsWith('/')
    || config.pathPrefix.includes('?') || config.pathPrefix.includes('#')) {
    throw new Error('worldsoul pathPrefix must be an absolute non-root pathname without a trailing slash, query, or fragment')
  }
}

/** Mount the WorldSoul route and drain every owned Agent during teardown. */
export function apply(ctx: Context, config: Config): void {
  assertConfig(config)
  const speechEntry: StoredSpeechConfiguration = {
    endpoint: config.speechEndpoint,
    model: config.speechModel,
    credentialRef: config.speechCredentialEnv,
    timeoutMs: config.speechTimeoutMs,
  }
  let speechSource = () => speechEntry
  ctx.settings.installSection(ctx, SPEECH_SETTINGS_NAMESPACE, SpeechSettingsSchema, speechEntry, {
    setSource: current => { speechSource = current },
    onChange: () => {},
    validate: validateSpeechConfiguration,
  })
  const speechConfiguration = async (): Promise<SpeechConfiguration> => {
    const current = speechSource()
    if (current.credentialRef === '') {
      return { ...current, credential: { configured: false, writable: false } }
    }
    const credential = await ctx.credentials.describe(credentialRef(current.credentialRef))
    return {
      ...current,
      credential: {
        configured: credential.configured,
        ...credential.source === undefined ? {} : { source: credential.source },
        writable: credential.writable,
      },
    }
  }
  const configureSpeech = async (update: SpeechConfigurationUpdate): Promise<SpeechConfiguration> => {
    validateSpeechConfiguration(update)
    if (update.apiKey !== undefined) {
      const key = update.apiKey.trim()
      if (update.credentialRef === '' || key.length === 0 || key.length > 8192) {
        throw new WorldSoulHttpError(400, 'speech apiKey requires a credentialRef and 1 to 8192 characters')
      }
      try {
        await ctx.credentials.set(credentialRef(update.credentialRef), key)
      } catch {
        throw new WorldSoulHttpError(409, 'speech credential cannot be updated from Harness')
      }
    }
    await ctx.settings.replace(SPEECH_SETTINGS_NAMESPACE, {
      endpoint: update.endpoint,
      model: update.model,
      credentialRef: update.credentialRef,
      timeoutMs: update.timeoutMs,
    })
    return speechConfiguration()
  }
  const runtime = new WorldSoulRuntime(ctx, {
    ...config,
    transcribe: (audio, mimeType) => {
      const speech = speechSource()
      return transcribeAudio(ctx, {
        speechEndpoint: speech.endpoint,
        speechModel: speech.model,
        speechCredentialEnv: speech.credentialRef,
        speechTimeoutMs: speech.timeoutMs,
      }, audio, mimeType)
    },
    speechConfiguration,
    configureSpeech,
  })
  ctx.effect(() => {
    const unregister = ctx.webServer.register({
      kind: 'prefix',
      path: config.pathPrefix,
      handler: createWorldSoulHandler(runtime, {
        ...config,
        onInternalError: error => {
          ctx.logger.error('worldsoul-gateway: request failed', error)
          console.error('worldsoul-gateway: request failed', error)
        },
      }),
    })
    return async () => {
      unregister()
      await runtime.dispose()
    }
  }, `worldsoul-gateway: ${config.pathPrefix}`)
}

function validateSpeechConfiguration(config: StoredSpeechConfiguration): void {
  if (config.endpoint !== '') {
    let url: URL
    try { url = new URL(config.endpoint) } catch { throw new WorldSoulHttpError(400, 'speech endpoint must be an absolute HTTP URL or empty') }
    if (url.protocol !== 'http:' && url.protocol !== 'https:') {
      throw new WorldSoulHttpError(400, 'speech endpoint must use http or https')
    }
  }
  if (config.model.trim() !== config.model || config.model === '' || config.model.length > 200) {
    throw new WorldSoulHttpError(400, 'speech model must be a non-empty trimmed string of at most 200 characters')
  }
  if (config.credentialRef !== '' && !/^[A-Za-z_][A-Za-z0-9_]*$/.test(config.credentialRef)) {
    throw new WorldSoulHttpError(400, 'speech credentialRef must be empty or an environment-style name')
  }
  if (!Number.isSafeInteger(config.timeoutMs) || config.timeoutMs < 1 || config.timeoutMs > 300_000) {
    throw new WorldSoulHttpError(400, 'speech timeoutMs must be an integer from 1 through 300000')
  }
}
