import { createServer, type Server } from 'node:http'
import type { AddressInfo } from 'node:net'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createWorldSoulHandler } from '../src/handler.ts'
import type { WorldSoulRuntime } from '../src/runtime.ts'

let server: Server | undefined

afterEach(async () => {
  if (server === undefined) return
  await new Promise<void>((resolve, reject) => server!.close(error => error === undefined ? resolve() : reject(error)))
  server = undefined
})

async function start(runtime: Record<string, unknown>): Promise<string> {
  server = createServer(createWorldSoulHandler(runtime as unknown as WorldSoulRuntime, {
    pathPrefix: '/worldsoul',
    maxBodyBytes: 1024,
    maxAudioBytes: 1024,
  }))
  await new Promise<void>((resolve, reject) => {
    server!.once('error', reject)
    server!.listen(0, '127.0.0.1', resolve)
  })
  return `http://127.0.0.1:${String((server.address() as AddressInfo).port)}`
}

describe('WorldSoul HTTP protocol', () => {
  it('serves health and dashboard without an Agent call', async () => {
    const base = await start({ snapshot: () => ({ worlds: [] }) })
    const health = await fetch(`${base}/worldsoul/v1/health`)
    expect(await health.json()).toEqual({ ok: true, service: 'worldsoul-harness', protocolVersion: 1 })
    const dashboard = await fetch(`${base}/worldsoul`)
    expect(dashboard.headers.get('content-type')).toContain('text/html')
    const html = await dashboard.text()
    expect(html).toContain('WorldSoul Harness')
    expect(html).toContain('data-testid="dashboard-shell"')
    expect(html).toContain('Agent 管理控制台')
    expect(html).toContain('运行总览')
    expect(html).toContain('/worldsoul/v1/status')
    expect(html).toContain('进入对话室')
    expect(html).toContain('/conversation?after=')
    expect(html).toContain('Agent 配置')
    expect(html).toContain('/agent-config')
    expect(html).toContain('语音识别配置')
    expect(html).toContain('/speech-config')
  })

  it('reads and updates Agent configuration without returning a credential value', async () => {
    const configuration = {
      provider: 'deepseek-official', model: 'deepseek-v4-pro', reasoningEffort: 'high',
      availableModels: ['deepseek-v4-flash', 'deepseek-v4-pro'],
      availableReasoningEfforts: ['off', 'low', 'high', 'max'],
      credential: { ref: 'DEEPSEEK_API_KEY', configured: true, source: 'file', writable: true },
    }
    const runtime = {
      snapshot: () => ({ worlds: [] }),
      agentConfiguration: vi.fn(async () => configuration),
      configureAgent: vi.fn(async () => configuration),
    }
    const base = await start(runtime)
    const target = `${base}/worldsoul/v1/agent-config`
    const read = await fetch(target)
    expect(await read.json()).toEqual(configuration)
    const write = await fetch(target, {
      method: 'PUT', headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ provider: 'deepseek-official', model: 'deepseek-v4-pro', reasoningEffort: 'high', apiKey: 'new-secret' }),
    })
    expect(write.status).toBe(200)
    expect(runtime.configureAgent).toHaveBeenCalledWith({
      provider: 'deepseek-official', model: 'deepseek-v4-pro', reasoningEffort: 'high', apiKey: 'new-secret',
    })
    expect(await write.text()).not.toContain('new-secret')
  })

  it('routes player lifecycle, chat, limits, and private command feedback', async () => {
    const reply = { speech: '已处理', commands: [], usage: { used: 1, limit: 10, ratio: 0.1, state: 'normal', disabled: false } }
    const runtime = {
      playerOnline: vi.fn(async () => reply.usage),
      playerOffline: vi.fn(async () => undefined),
      chat: vi.fn(async () => reply),
      setLimit: vi.fn(() => reply.usage),
      setWorldLimit: vi.fn(() => reply.usage),
      setMode: vi.fn(() => reply.usage),
      updatePlayerSnapshot: vi.fn(() => undefined),
      commandResult: vi.fn(async () => reply),
      conversation: vi.fn(() => ({ revision: 13, messages: [] })),
      snapshot: vi.fn(() => ({ worlds: [] })),
    }
    const base = await start(runtime)
    const player = `${base}/worldsoul/v1/worlds/save%201/players/player%201`
    const playerState = {
      dimension: 'minecraft:overworld', x: 1, y: 64, z: 2, yaw: 90, pitch: 0,
      facing: 'west', health: 20, maxHealth: 20, foodLevel: 20, gameMode: 'survival', onGround: true,
      selectedHotbarSlot: 0,
      inventory: [{ slot: 0, item: 'minecraft:stone', count: 32 }],
      equipment: [{ slot: 'offhand', item: 'minecraft:shield', count: 1, damage: 5, maxDamage: 336 }],
    }
    expect((await fetch(`${player}/online`, {
      method: 'PUT', headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ playerName: 'Alex', mode: 'deity', playerState }),
    })).status).toBe(200)
    expect((await fetch(`${player}/chat`, {
      method: 'POST', headers: { 'content-type': 'application/json' }, body: JSON.stringify({ message: '给我石头', playerState }),
    })).status).toBe(200)
    expect((await fetch(`${player}/mode`, {
      method: 'PUT', headers: { 'content-type': 'application/json' }, body: JSON.stringify({ mode: 'guide' }),
    })).status).toBe(200)
    expect((await fetch(`${player}/state`, {
      method: 'PUT', headers: { 'content-type': 'application/json' }, body: JSON.stringify({ playerState }),
    })).status).toBe(200)
    expect((await fetch(`${player}/limit`, {
      method: 'PUT', headers: { 'content-type': 'application/json' }, body: JSON.stringify({ limit: 20 }),
    })).status).toBe(200)
    expect((await fetch(`${player}/conversation?after=12`)).status).toBe(200)
    expect((await fetch(`${base}/worldsoul/v1/worlds/save%201/limit`, {
      method: 'PUT', headers: { 'content-type': 'application/json' }, body: JSON.stringify({ limit: 30 }),
    })).status).toBe(200)
    expect((await fetch(`${player}/commands/p-1/result`, {
      method: 'POST', headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ accepted: false, error: 'unknown item' }),
    })).status).toBe(200)
    expect(runtime.playerOnline).toHaveBeenCalledWith('save 1', 'player 1', 'Alex', 'deity')
    expect(runtime.setMode).toHaveBeenCalledWith('save 1', 'player 1', 'guide')
    expect(runtime.updatePlayerSnapshot).toHaveBeenCalledWith('save 1', 'player 1', playerState)
    expect(runtime.setWorldLimit).toHaveBeenCalledWith('save 1', 30)
    expect(runtime.commandResult).toHaveBeenCalledWith('save 1', 'player 1', 'p-1', false, 'unknown item')
    expect(runtime.conversation).toHaveBeenCalledWith('save 1', 'player 1', 12)
  })

  it('returns structured errors and never reflects internal failures', async () => {
    const base = await start({
      chat: vi.fn(async () => { throw new Error('secret validation detail') }),
      snapshot: () => ({ worlds: [] }),
    })
    const response = await fetch(`${base}/worldsoul/v1/worlds/w/players/p/chat`, {
      method: 'POST', headers: { 'content-type': 'application/json' }, body: JSON.stringify({ message: 'x' }),
    })
    expect(response.status).toBe(500)
    const body = await response.text()
    expect(body).toContain('internal harness error')
    expect(body).not.toContain('secret validation detail')
  })

  it('accepts bounded audio and returns transcript plus reply', async () => {
    const voice = vi.fn(async (_worldId: string, _playerId: string, _audio: Uint8Array, _mimeType: string) => (
      { transcript: '你好', reply: { speech: '你好', commands: [], usage: null } }
    ))
    const base = await start({ voice, snapshot: () => ({ worlds: [] }) })
    const wave = new Uint8Array(44)
    wave.set(new TextEncoder().encode('RIFF'), 0)
    wave.set(new TextEncoder().encode('WAVE'), 8)
    const response = await fetch(`${base}/worldsoul/v1/worlds/w/players/p/voice`, {
      method: 'POST', headers: { 'content-type': 'audio/wav' }, body: wave,
    })
    expect(response.status).toBe(200)
    expect(await response.json()).toMatchObject({ transcript: '你好' })
    expect(voice).toHaveBeenCalledOnce()
    expect(voice.mock.calls[0]?.slice(0, 2)).toEqual(['w', 'p'])
    expect(Array.from(voice.mock.calls[0]?.[2] as Uint8Array)).toEqual(Array.from(wave))
    expect(voice.mock.calls[0]?.[3]).toBe('audio/wav')
  })

  it('returns a transcript without starting the legacy combined voice turn', async () => {
    const transcribeVoice = vi.fn(async () => '立即回显')
    const voice = vi.fn()
    const base = await start({ transcribeVoice, voice, snapshot: () => ({ worlds: [] }) })
    const wave = new Uint8Array(44)
    wave.set(new TextEncoder().encode('RIFF'), 0)
    wave.set(new TextEncoder().encode('WAVE'), 8)
    const response = await fetch(`${base}/worldsoul/v1/worlds/w/players/p/voice/transcribe`, {
      method: 'POST', headers: { 'content-type': 'audio/wav' }, body: wave,
    })
    expect(response.status).toBe(200)
    expect(await response.json()).toEqual({ transcript: '立即回显' })
    expect(transcribeVoice).toHaveBeenCalledOnce()
    expect(voice).not.toHaveBeenCalled()
  })

  it('rejects malformed WAV before invoking speech recognition', async () => {
    const voice = vi.fn()
    const base = await start({ voice, snapshot: () => ({ worlds: [] }) })
    const response = await fetch(`${base}/worldsoul/v1/worlds/w/players/p/voice`, {
      method: 'POST', headers: { 'content-type': 'audio/wav' }, body: new Uint8Array(44),
    })
    expect(response.status).toBe(400)
    expect(voice).not.toHaveBeenCalled()
  })

  it('reads and updates speech settings without returning the API key', async () => {
    const configuration = {
      endpoint: 'https://api.openai.com/v1/audio/transcriptions', model: 'whisper-1',
      credentialRef: 'OPENAI_API_KEY', timeoutMs: 60_000,
      credential: { configured: true, source: 'file', writable: true },
    }
    const runtime = {
      snapshot: () => ({ worlds: [] }),
      speechConfiguration: vi.fn(async () => configuration),
      configureSpeech: vi.fn(async () => configuration),
    }
    const base = await start(runtime)
    const target = `${base}/worldsoul/v1/speech-config`
    expect(await (await fetch(target)).json()).toEqual(configuration)
    const response = await fetch(target, {
      method: 'PUT', headers: { 'content-type': 'application/json' },
      body: JSON.stringify({
        endpoint: configuration.endpoint, model: configuration.model,
        credentialRef: configuration.credentialRef, timeoutMs: configuration.timeoutMs, apiKey: 'speech-secret',
      }),
    })
    expect(response.status).toBe(200)
    expect(await response.text()).not.toContain('speech-secret')
    expect(runtime.configureSpeech).toHaveBeenCalledOnce()
  })
})
