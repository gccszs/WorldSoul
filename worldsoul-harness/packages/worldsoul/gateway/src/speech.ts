/** OpenAI-compatible speech-to-text adapter for in-memory Minecraft audio. */

import type { Context } from '@deepseek-ai/cordis'
import { credentialRef } from '@deepseek-ai/dsh-credentials'
import { WorldSoulHttpError } from './runtime.ts'

/** Resolved speech provider settings. */
export interface SpeechConfig {
  readonly speechEndpoint: string
  readonly speechModel: string
  readonly speechCredentialEnv: string
  readonly speechTimeoutMs: number
}

/**
 * Transcribe audio without writing it to disk.
 * @param ctx - Cordis context providing credential resolution.
 * @param config - Resolved speech provider settings.
 * @param audio - Complete bounded audio upload.
 * @param mimeType - Validated audio media type.
 * @returns Non-empty trimmed transcript.
 */
export async function transcribeAudio(
  ctx: Context,
  config: SpeechConfig,
  audio: Uint8Array,
  mimeType: string,
): Promise<string> {
  if (config.speechEndpoint === '') throw new WorldSoulHttpError(503, 'speech recognition is not configured')
  const form = new FormData()
  form.append('model', config.speechModel)
  const bytes = Uint8Array.from(audio)
  form.append('file', new Blob([bytes.buffer], { type: mimeType }), mimeType === 'audio/wav' ? 'voice.wav' : 'voice.bin')
  const headers = new Headers()
  if (config.speechCredentialEnv !== '') {
    const credential = await ctx.credentials.resolve(credentialRef(config.speechCredentialEnv))
    if (credential === undefined || credential.value === '') {
      throw new WorldSoulHttpError(503, 'speech recognition credential is unavailable')
    }
    headers.set('authorization', `Bearer ${credential.value}`)
  }
  let response: Response
  try {
    response = await fetch(config.speechEndpoint, {
      method: 'POST', headers, body: form, signal: AbortSignal.timeout(config.speechTimeoutMs),
    })
  } catch {
    throw new WorldSoulHttpError(503, 'speech recognition provider is unavailable')
  }
  if (!response.ok) throw new WorldSoulHttpError(502, 'speech recognition provider rejected the request')
  let value: unknown
  try {
    value = await response.json()
  } catch {
    throw new WorldSoulHttpError(502, 'speech recognition provider returned invalid JSON')
  }
  const text = value !== null && typeof value === 'object' && 'text' in value
    ? (value as { text?: unknown }).text
    : undefined
  if (typeof text !== 'string' || text.trim() === '') {
    throw new WorldSoulHttpError(502, 'speech recognition provider returned no transcript')
  }
  return text.trim()
}
