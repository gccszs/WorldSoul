/** HTTP adapter for the version-independent WorldSoul protocol. */

import type { IncomingMessage, ServerResponse } from 'node:http'
import type { WebRoute } from '@deepseek-ai/dsh-host-webserver'
import { dashboardHtml } from './dashboard.ts'
import { WorldSoulHttpError, type WorldSoulRuntime } from './runtime.ts'
import type { PlayerSnapshot, WorldSoulMode } from './types.ts'

interface HandlerConfig {
  readonly pathPrefix: string
  readonly maxBodyBytes: number
  readonly maxAudioBytes: number
  readonly onInternalError?: (error: unknown) => void
}

function json(response: ServerResponse, status: number, value: unknown): void {
  response.writeHead(status, {
    'content-type': 'application/json; charset=utf-8',
    'cache-control': 'no-store',
  })
  response.end(JSON.stringify(value))
}

async function readBody(request: IncomingMessage, maxBytes: number): Promise<Record<string, unknown>> {
  const contentType = request.headers['content-type']?.split(';', 1)[0]?.trim().toLowerCase()
  if (contentType !== 'application/json') throw new WorldSoulHttpError(415, 'content type must be application/json')
  const chunks: Buffer[] = []
  let size = 0
  for await (const raw of request) {
    const chunk = Buffer.isBuffer(raw) ? raw : Buffer.from(raw)
    size += chunk.length
    if (size > maxBytes) throw new WorldSoulHttpError(413, 'request body too large')
    chunks.push(chunk)
  }
  let value: unknown
  try {
    value = JSON.parse(Buffer.concat(chunks).toString('utf8'))
  } catch {
    throw new WorldSoulHttpError(400, 'request body is not valid JSON')
  }
  if (value === null || typeof value !== 'object' || Array.isArray(value)) {
    throw new WorldSoulHttpError(400, 'request body must be a JSON object')
  }
  return value as Record<string, unknown>
}

async function readBinary(request: IncomingMessage, maxBytes: number): Promise<Uint8Array> {
  const chunks: Buffer[] = []
  let size = 0
  for await (const raw of request) {
    const chunk = Buffer.isBuffer(raw) ? raw : Buffer.from(raw)
    size += chunk.length
    if (size > maxBytes) throw new WorldSoulHttpError(413, 'audio body too large')
    chunks.push(chunk)
  }
  if (size === 0) throw new WorldSoulHttpError(400, 'audio body must not be empty')
  return Buffer.concat(chunks)
}

async function readVoiceAudio(request: IncomingMessage, maxBytes: number): Promise<{ audio: Uint8Array, mimeType: string }> {
  const mimeType = request.headers['content-type']?.split(';', 1)[0]?.trim().toLowerCase()
  if (mimeType !== 'audio/wav' && mimeType !== 'audio/webm' && mimeType !== 'audio/ogg'
    && mimeType !== 'application/octet-stream') {
    throw new WorldSoulHttpError(415, 'unsupported audio content type')
  }
  const audio = await readBinary(request, maxBytes)
  if (mimeType === 'audio/wav' && (audio.length < 44
    || String.fromCharCode(...audio.slice(0, 4)) !== 'RIFF'
    || String.fromCharCode(...audio.slice(8, 12)) !== 'WAVE')) {
    throw new WorldSoulHttpError(400, 'audio/wav body must contain a valid RIFF/WAVE header')
  }
  return { audio, mimeType }
}

function stringField(body: Record<string, unknown>, name: string): string {
  const value = body[name]
  if (typeof value !== 'string') throw new WorldSoulHttpError(400, `${name} must be a string`)
  return value
}

function modeField(body: Record<string, unknown>): WorldSoulMode {
  const value = stringField(body, 'mode')
  if (value !== 'guide' && value !== 'deity' && value !== 'troll') {
    throw new WorldSoulHttpError(400, 'mode must be guide, deity, or troll')
  }
  return value
}

function playerSnapshotField(body: Record<string, unknown>): PlayerSnapshot | undefined {
  const raw = body.playerState
  if (raw === undefined) return undefined
  if (raw === null || typeof raw !== 'object' || Array.isArray(raw)) {
    throw new WorldSoulHttpError(400, 'playerState must be a JSON object')
  }
  const state = raw as Record<string, unknown>
  const text = (name: string): string => {
    const value = state[name]
    if (typeof value !== 'string' || value.trim() === '' || value.length > 200) {
      throw new WorldSoulHttpError(400, `playerState.${name} must be a non-empty string of at most 200 characters`)
    }
    return value
  }
  const number = (name: string): number => {
    const value = state[name]
    if (typeof value !== 'number' || !Number.isFinite(value)) {
      throw new WorldSoulHttpError(400, `playerState.${name} must be a finite number`)
    }
    return value
  }
  const facing = text('facing')
  if (facing !== 'north' && facing !== 'south' && facing !== 'east' && facing !== 'west') {
    throw new WorldSoulHttpError(400, 'playerState.facing must be north, south, east, or west')
  }
  const foodLevel = number('foodLevel')
  if (!Number.isInteger(foodLevel) || foodLevel < 0 || foodLevel > 20) {
    throw new WorldSoulHttpError(400, 'playerState.foodLevel must be an integer from 0 through 20')
  }
  if (typeof state.onGround !== 'boolean') {
    throw new WorldSoulHttpError(400, 'playerState.onGround must be a boolean')
  }
  const stack = (rawStack: unknown, label: string): { item: string, count: number, damage?: number, maxDamage?: number } => {
    if (rawStack === null || typeof rawStack !== 'object' || Array.isArray(rawStack)) {
      throw new WorldSoulHttpError(400, `${label} must be a JSON object`)
    }
    const value = rawStack as Record<string, unknown>
    const item = value.item
    const count = value.count
    if (typeof item !== 'string' || !/^[a-z0-9_.-]+:[a-z0-9/._-]+$/.test(item) || item.length > 200) {
      throw new WorldSoulHttpError(400, `${label}.item must be a namespaced registry id`)
    }
    if (!Number.isSafeInteger(count) || (count as number) < 1 || (count as number) > 32767) {
      throw new WorldSoulHttpError(400, `${label}.count must be an integer from 1 through 32767`)
    }
    const damage = value.damage
    const maxDamage = value.maxDamage
    if ((damage === undefined) !== (maxDamage === undefined)) {
      throw new WorldSoulHttpError(400, `${label}.damage and maxDamage must be supplied together`)
    }
    if (damage !== undefined && (!Number.isSafeInteger(damage) || (damage as number) < 0
      || !Number.isSafeInteger(maxDamage) || (maxDamage as number) < 1)) {
      throw new WorldSoulHttpError(400, `${label} durability must contain non-negative integer damage and positive maxDamage`)
    }
    return { item, count: count as number, ...damage === undefined ? {} : { damage: damage as number, maxDamage: maxDamage as number } }
  }
  const inventoryRaw = state.inventory ?? []
  if (!Array.isArray(inventoryRaw) || inventoryRaw.length > 36) {
    throw new WorldSoulHttpError(400, 'playerState.inventory must contain at most 36 stacks')
  }
  const inventory = inventoryRaw.map((rawStack, index) => {
    const parsed = stack(rawStack, `playerState.inventory[${index}]`)
    const slot = (rawStack as Record<string, unknown>).slot
    if (!Number.isSafeInteger(slot) || (slot as number) < 0 || (slot as number) > 35) {
      throw new WorldSoulHttpError(400, `playerState.inventory[${index}].slot must be an integer from 0 through 35`)
    }
    return { slot: slot as number, ...parsed }
  })
  if (new Set(inventory.map(value => value.slot)).size !== inventory.length) {
    throw new WorldSoulHttpError(400, 'playerState.inventory slots must be unique')
  }
  const equipmentRaw = state.equipment ?? []
  if (!Array.isArray(equipmentRaw) || equipmentRaw.length > 6) {
    throw new WorldSoulHttpError(400, 'playerState.equipment must contain at most 6 stacks')
  }
  const equipmentSlots = ['head', 'chest', 'legs', 'feet', 'mainhand', 'offhand'] as const
  const equipment = equipmentRaw.map((rawStack, index) => {
    const parsed = stack(rawStack, `playerState.equipment[${index}]`)
    const slot = (rawStack as Record<string, unknown>).slot
    if (typeof slot !== 'string' || !equipmentSlots.includes(slot as typeof equipmentSlots[number])) {
      throw new WorldSoulHttpError(400, `playerState.equipment[${index}].slot is not supported`)
    }
    return { slot: slot as typeof equipmentSlots[number], ...parsed }
  })
  if (new Set(equipment.map(value => value.slot)).size !== equipment.length) {
    throw new WorldSoulHttpError(400, 'playerState.equipment slots must be unique')
  }
  const selectedHotbarSlot = state.selectedHotbarSlot ?? 0
  if (!Number.isSafeInteger(selectedHotbarSlot) || (selectedHotbarSlot as number) < 0 || (selectedHotbarSlot as number) > 8) {
    throw new WorldSoulHttpError(400, 'playerState.selectedHotbarSlot must be an integer from 0 through 8')
  }
  return {
    dimension: text('dimension'), x: number('x'), y: number('y'), z: number('z'),
    yaw: number('yaw'), pitch: number('pitch'), facing,
    health: number('health'), maxHealth: number('maxHealth'), foodLevel,
    gameMode: text('gameMode'), onGround: state.onGround,
    selectedHotbarSlot: selectedHotbarSlot as number, inventory, equipment,
  }
}

function decodeSegment(value: string): string {
  try {
    return decodeURIComponent(value)
  } catch {
    throw new WorldSoulHttpError(400, 'path contains invalid percent encoding')
  }
}

/**
 * Create the single prefix route mounted by the WorldSoul profile.
 * @param runtime - World and player Agent runtime serving the route.
 * @param config - Resolved HTTP limits, route prefix, and error sink.
 * @returns Web-server handler for the complete WorldSoul protocol.
 */
export function createWorldSoulHandler(runtime: WorldSoulRuntime, config: HandlerConfig): WebRoute['handler'] {
  return async (request, response) => {
    try {
      const url = new URL(request.url ?? '/', 'http://worldsoul.local')
      const relative = url.pathname.slice(config.pathPrefix.length)
      if ((relative === '' || relative === '/') && request.method === 'GET') {
        response.writeHead(200, {
          'content-type': 'text/html; charset=utf-8',
          'content-security-policy': "default-src 'none'; style-src 'unsafe-inline'; script-src 'unsafe-inline'; connect-src 'self'",
          'cache-control': 'no-store',
        })
        response.end(dashboardHtml(config.pathPrefix))
        return
      }
      if (relative === '/v1/health' && request.method === 'GET') {
        json(response, 200, { ok: true, service: 'worldsoul-harness', protocolVersion: 1 })
        return
      }
      if (relative === '/v1/status' && request.method === 'GET') {
        json(response, 200, runtime.snapshot())
        return
      }
      if (relative === '/v1/agent-config' && request.method === 'GET') {
        json(response, 200, await runtime.agentConfiguration())
        return
      }
      if (relative === '/v1/agent-config' && request.method === 'PUT') {
        const body = await readBody(request, config.maxBodyBytes)
        const reasoningEffort = body.reasoningEffort
        const apiKey = body.apiKey
        if (reasoningEffort !== undefined && typeof reasoningEffort !== 'string') {
          throw new WorldSoulHttpError(400, 'reasoningEffort must be a string')
        }
        if (apiKey !== undefined && typeof apiKey !== 'string') {
          throw new WorldSoulHttpError(400, 'apiKey must be a string')
        }
        json(response, 200, await runtime.configureAgent({
          provider: stringField(body, 'provider'),
          model: stringField(body, 'model'),
          ...reasoningEffort === undefined ? {} : { reasoningEffort },
          ...apiKey === undefined ? {} : { apiKey },
        }))
        return
      }
      if (relative === '/v1/speech-config' && request.method === 'GET') {
        json(response, 200, await runtime.speechConfiguration())
        return
      }
      if (relative === '/v1/speech-config' && request.method === 'PUT') {
        const body = await readBody(request, config.maxBodyBytes)
        const timeoutMs = body.timeoutMs
        const apiKey = body.apiKey
        if (typeof timeoutMs !== 'number') throw new WorldSoulHttpError(400, 'timeoutMs must be a number')
        if (apiKey !== undefined && typeof apiKey !== 'string') throw new WorldSoulHttpError(400, 'apiKey must be a string')
        json(response, 200, await runtime.configureSpeech({
          endpoint: stringField(body, 'endpoint'),
          model: stringField(body, 'model'),
          credentialRef: stringField(body, 'credentialRef'),
          timeoutMs,
          ...apiKey === undefined ? {} : { apiKey },
        }))
        return
      }
      const segments = relative.split('/').filter(Boolean).map(decodeSegment)
      if (segments[0] !== 'v1' || segments[1] !== 'worlds' || segments[2] === undefined) {
        throw new WorldSoulHttpError(404, 'route not found')
      }
      const worldId = segments[2]
      if (segments.length === 3 && request.method === 'DELETE') {
        await runtime.worldOffline(worldId)
        json(response, 200, { ok: true })
        return
      }
      if (segments.length === 4 && segments[3] === 'limit' && request.method === 'PUT') {
        const body = await readBody(request, config.maxBodyBytes)
        const limit = body.limit
        if (typeof limit !== 'number') throw new WorldSoulHttpError(400, 'limit must be a number')
        json(response, 200, { usage: runtime.setWorldLimit(worldId, limit) })
        return
      }
      if (segments[3] !== 'players' || segments[4] === undefined) {
        throw new WorldSoulHttpError(404, 'route not found')
      }
      const playerId = segments[4]
      if (segments.length === 6 && segments[5] === 'conversation' && request.method === 'GET') {
        const rawAfter = url.searchParams.get('after') ?? '-1'
        const after = Number(rawAfter)
        if (!Number.isSafeInteger(after) || after < -1) throw new WorldSoulHttpError(400, 'after must be an integer of at least -1')
        json(response, 200, runtime.conversation(worldId, playerId, after))
        return
      }
      if (segments.length === 6 && segments[5] === 'online' && request.method === 'PUT') {
        const body = await readBody(request, config.maxBodyBytes)
        const usage = await runtime.playerOnline(worldId, playerId, stringField(body, 'playerName'), modeField(body))
        const snapshot = playerSnapshotField(body)
        if (snapshot !== undefined) runtime.updatePlayerSnapshot(worldId, playerId, snapshot)
        json(response, 200, { ok: true, usage })
        return
      }
      if (segments.length === 6 && segments[5] === 'online' && request.method === 'DELETE') {
        await runtime.playerOffline(worldId, playerId)
        json(response, 200, { ok: true })
        return
      }
      if (segments.length === 6 && segments[5] === 'chat' && request.method === 'POST') {
        const body = await readBody(request, config.maxBodyBytes)
        const snapshot = playerSnapshotField(body)
        if (snapshot !== undefined) runtime.updatePlayerSnapshot(worldId, playerId, snapshot)
        json(response, 200, await runtime.chat(worldId, playerId, stringField(body, 'message')))
        return
      }
      if (segments.length === 6 && segments[5] === 'mode' && request.method === 'PUT') {
        const body = await readBody(request, config.maxBodyBytes)
        const mode = modeField(body)
        json(response, 200, { ok: true, mode, usage: runtime.setMode(worldId, playerId, mode) })
        return
      }
      if (segments.length === 6 && segments[5] === 'state' && request.method === 'PUT') {
        const body = await readBody(request, config.maxBodyBytes)
        const snapshot = playerSnapshotField(body)
        if (snapshot === undefined) throw new WorldSoulHttpError(400, 'playerState is required')
        runtime.updatePlayerSnapshot(worldId, playerId, snapshot)
        json(response, 200, { ok: true })
        return
      }
      if (segments.length === 7 && segments[5] === 'voice' && segments[6] === 'transcribe' && request.method === 'POST') {
        const { audio, mimeType } = await readVoiceAudio(request, config.maxAudioBytes)
        json(response, 200, { transcript: await runtime.transcribeVoice(worldId, playerId, audio, mimeType) })
        return
      }
      if (segments.length === 6 && segments[5] === 'voice' && request.method === 'POST') {
        const { audio, mimeType } = await readVoiceAudio(request, config.maxAudioBytes)
        json(response, 200, await runtime.voice(worldId, playerId, audio, mimeType))
        return
      }
      if (segments.length === 6 && segments[5] === 'limit' && request.method === 'PUT') {
        const body = await readBody(request, config.maxBodyBytes)
        const limit = body.limit
        if (typeof limit !== 'number') throw new WorldSoulHttpError(400, 'limit must be a number')
        json(response, 200, { usage: runtime.setLimit(worldId, playerId, limit) })
        return
      }
      if (segments.length === 8 && segments[5] === 'commands' && segments[7] === 'result' && request.method === 'POST') {
        const body = await readBody(request, config.maxBodyBytes)
        if (typeof body.accepted !== 'boolean') throw new WorldSoulHttpError(400, 'accepted must be a boolean')
        const error = body.error
        if (error !== undefined && typeof error !== 'string') throw new WorldSoulHttpError(400, 'error must be a string')
        json(response, 200, await runtime.commandResult(worldId, playerId, segments[6]!, body.accepted, error))
        return
      }
      throw new WorldSoulHttpError(404, 'route not found')
    } catch (error: unknown) {
      if (error instanceof WorldSoulHttpError) {
        json(response, error.status, { error: { code: `worldsoul/http-${error.status}`, message: error.message } })
        return
      }
      config.onInternalError?.(error)
      json(response, 500, { error: { code: 'worldsoul/internal', message: 'internal harness error' } })
    }
  }
}
