import { describe, expect, it, vi } from 'vitest'
import { WorldSoulRuntime } from '../src/runtime.ts'

vi.mock('@deepseek-ai/dsh-persona', () => ({ apply: vi.fn(), PERSONA_SECTION: 'deployment:persona' }))

interface FakeEvent {
  type: string
  seq: number
  time: number
  data: Record<string, unknown>
}

function assistantEvent(text: string, tokens: number, seq = 0): FakeEvent {
  return {
    type: 'assistant/message',
    seq,
    time: 1_700_000_000_000 + seq,
    data: {
      message: { content: [{ type: 'text', text }] },
      usage: { inputTokens: tokens, outputTokens: 0, totalTokens: tokens },
    },
  }
}

function runtimeWithPlayer(tokens: number, proposal = false, failureCode?: string) {
  const events: FakeEvent[] = tokens === 0 ? [] : [assistantEvent('旧回复', tokens)]
  const injected: unknown[] = []
  const followed: unknown[] = []
  const dispose = vi.fn(async () => undefined)
  const agent = {
    id: 'player-agent',
    session: {
      get seq() { return events.length },
      snapshotEvents: (offset?: number) => events.slice(offset ?? 0),
    },
    followup: (message: unknown) => {
      followed.push(message)
      if (failureCode === undefined) {
        events.push(assistantEvent('已生成修正方案', 1, events.length))
      } else {
        events.push({
          type: 'turn/end', seq: events.length, time: 1_700_000_000_000 + events.length,
          data: { turn: 1, reason: { kind: 'error', error: { code: failureCode, message: 'private provider detail' } } },
        })
      }
    },
    inject: (message: unknown) => { injected.push(message) },
    whenIdle: async () => undefined,
  }
  const player = {
    id: 'player', name: 'Alex', handle: { agent, dispose },
    proposals: new Map(proposal ? [['proposal-1', { id: 'proposal-1', command: 'give @s stone', rationale: 'test' }]] : []),
    mode: 'deity', live: { mode: 'deity' }, limit: 100, active: true, tail: Promise.resolve(),
  }
  const world = {
    id: 'world', handle: { agent: { id: 'world-agent', session: { snapshotEvents: () => [] } }, dispose: vi.fn() },
    players: new Map([['player', player]]), limit: 100,
  }
  const runtime = new WorldSoulRuntime({} as never, {
    defaultTokenLimit: 100, warningRatio: 0.8, maxCommandChars: 512,
    transcribe: async () => '语音文本',
  })
  const internal = runtime as unknown as { worlds: Map<string, unknown> }
  internal.worlds.set('world', world)
  return { runtime, player, events, followed, injected, dispose }
}

describe('WorldSoul runtime safety boundaries', () => {
  it('persists Agent defaults and rotates credentials without exposing the secret', async () => {
    const saveSelection = vi.fn(async () => undefined)
    const set = vi.fn(async () => undefined)
    const runtime = new WorldSoulRuntime({
      agentDefaultModel: {
        currentSelection: () => ({ provider: 'deepseek-official', model: 'deepseek-v4-pro', reasoningEffort: 'high' }),
        saveSelection,
      },
      credentials: {
        describe: vi.fn(async () => ({ configured: true, source: 'file', writable: true })),
        set,
      },
    } as never, {
      defaultTokenLimit: 100, warningRatio: 0.8, maxCommandChars: 512,
      transcribe: async () => 'unused',
    })

    const result = await runtime.configureAgent({
      provider: 'deepseek-official', model: 'deepseek-v4-pro', reasoningEffort: 'high', apiKey: 'rotated-secret',
    })
    expect(set).toHaveBeenCalledWith('DEEPSEEK_API_KEY', 'rotated-secret')
    expect(saveSelection).toHaveBeenCalledWith({
      provider: 'deepseek-official', model: 'deepseek-v4-pro', reasoningEffort: 'high',
    })
    expect(result.credential).toEqual({
      ref: 'DEEPSEEK_API_KEY', configured: true, source: 'file', writable: true,
    })
    expect(JSON.stringify(result)).not.toContain('rotated-secret')
  })

  it('rejects unsupported Agent configuration before writing it', async () => {
    const saveSelection = vi.fn()
    const set = vi.fn()
    const runtime = new WorldSoulRuntime({
      agentDefaultModel: { currentSelection: vi.fn(), saveSelection },
      credentials: { describe: vi.fn(), set },
    } as never, {
      defaultTokenLimit: 100, warningRatio: 0.8, maxCommandChars: 512,
      transcribe: async () => 'unused',
    })
    await expect(runtime.configureAgent({ provider: 'unknown', model: 'anything' }))
      .rejects.toMatchObject({ status: 400 })
    await expect(runtime.configureAgent({ provider: 'deepseek-official', model: 'unknown' }))
      .rejects.toMatchObject({ status: 400 })
    expect(set).not.toHaveBeenCalled()
    expect(saveSelection).not.toHaveBeenCalled()
  })

  it('creates one save-global Agent and isolated child Agents per player', async () => {
    const disposed: string[] = []
    const localTools = new Map<string, string[]>()
    const personaTexts = new Map<string, () => string>()
    const registry = {
      create: vi.fn(async (options: {
        sessionId: string
        agentOptions?: { provider: string, model: string }
        setup?: (ctx: unknown) => void | Promise<void>
      }) => {
        const tools: string[] = []
        const agent = {
          id: options.sessionId,
          session: { seq: 0, snapshotEvents: () => [] },
          ctx: {
            agents: registry,
            systemPrompt: {
              section: (definition: { text: string | (() => string) }) => {
                const text = definition.text
                personaTexts.set(options.sessionId, typeof text === 'function' ? text : () => text)
                return () => undefined
              },
              getSectionOrder: () => 0,
              suppressRuntimeContext: () => () => undefined,
            },
            tools: {
              register: (definition: { name: string }) => { tools.push(definition.name); return () => undefined },
              restrict: vi.fn(() => () => undefined),
            },
          },
          followup: vi.fn(), inject: vi.fn(), whenIdle: async () => undefined,
        }
        await options.setup?.(agent.ctx)
        localTools.set(options.sessionId, tools)
        return { agent, dispose: async () => { disposed.push(options.sessionId) } }
      }),
      resume: vi.fn(),
    }
    const runtime = new WorldSoulRuntime({
      agents: registry,
      agentDefaultModel: { currentSelection: () => ({ provider: 'deepseek-official', model: 'deepseek-v4-flash' }) },
    } as never, {
      defaultTokenLimit: 100, warningRatio: 0.8, maxCommandChars: 512,
      transcribe: async () => 'unused',
    })
    await runtime.playerOnline('save', 'player-a', 'Alex', 'guide')
    await runtime.playerOnline('save', 'player-b', 'Steve', 'deity')
    const status = runtime.snapshot() as { worlds: Array<{ globalAgentId: string, players: Array<{ id: string, agentId: string }> }> }
    expect(status.worlds).toHaveLength(1)
    expect(status.worlds[0]?.players).toHaveLength(2)
    expect(status.worlds[0]?.players[0]?.agentId).not.toBe(status.worlds[0]?.players[1]?.agentId)
    for (const call of registry.create.mock.calls) {
      expect(call[0].agentOptions).toEqual({ provider: 'deepseek-official', model: 'deepseek-v4-flash' })
    }
    for (const player of status.worlds[0]?.players ?? []) {
      expect(localTools.get(player.agentId)).toEqual(['minecraft_command'])
    }
    const first = status.worlds[0]?.players[0]
    expect(first).toBeDefined()
    runtime.updatePlayerSnapshot('save', 'player-a', {
      dimension: 'minecraft:overworld', x: 12.5, y: 64, z: -3, yaw: 90, pitch: 10,
      facing: 'west', health: 18, maxHealth: 20, foodLevel: 17, gameMode: 'survival', onGround: true,
      selectedHotbarSlot: 2,
      inventory: [{ slot: 2, item: 'minecraft:diamond_pickaxe', count: 1, damage: 10, maxDamage: 1561 }],
      equipment: [{ slot: 'head', item: 'minecraft:diamond_helmet', count: 1, damage: 4, maxDamage: 363 }],
    })
    runtime.setMode('save', 'player-a', 'troll')
    expect(personaTexts.get(first!.agentId)?.()).toContain('沙雕模式')
    expect(personaTexts.get(first!.agentId)?.()).toContain('X=12.50')
    expect(personaTexts.get(first!.agentId)?.()).toContain('朝向 west')
    expect(personaTexts.get(first!.agentId)?.()).toContain('minecraft:diamond_pickaxe×1')
    expect(personaTexts.get(first!.agentId)?.()).toContain('head:minecraft:diamond_helmet×1')
    await runtime.playerOffline('save', 'player-a')
    expect(disposed).toContain(status.worlds[0]?.players[0]?.agentId)
    expect((runtime.snapshot() as { worlds: Array<{ players: unknown[] }> }).worlds[0]?.players).toHaveLength(1)
  })

  it('rejects new turns once a player Agent reaches its token threshold', async () => {
    const { runtime, followed } = runtimeWithPlayer(100)
    await expect(runtime.chat('world', 'player', '继续')).rejects.toMatchObject({ status: 429 })
    expect(followed).toHaveLength(0)
  })

  it('keeps command validation errors private while asking for correction', async () => {
    const { runtime, followed } = runtimeWithPlayer(1, true)
    const reply = await runtime.commandResult('world', 'player', 'proposal-1', false, 'private brigadier detail')
    expect(JSON.stringify(followed)).toContain('private brigadier detail')
    expect(reply.speech).toBe('已生成修正方案')
    expect(JSON.stringify(reply)).not.toContain('private brigadier detail')
  })

  it('routes a transcript through the same isolated chat turn', async () => {
    const { runtime, followed } = runtimeWithPlayer(0)
    const result = await runtime.voice('world', 'player', new Uint8Array([1]), 'audio/wav')
    expect(result.transcript).toBe('语音文本')
    expect(JSON.stringify(followed)).toContain('语音文本')
    expect(result.reply.speech).toBe('已生成修正方案')
  })

  it('returns a voice transcript before starting the Agent turn', async () => {
    const { runtime, followed } = runtimeWithPlayer(0)
    const transcript = await runtime.transcribeVoice('world', 'player', new Uint8Array([1]), 'audio/wav')
    expect(transcript).toBe('语音文本')
    expect(followed).toHaveLength(0)
    await runtime.chat('world', 'player', transcript)
    expect(JSON.stringify(followed)).toContain('语音文本')
  })

  it('returns a safe authentication failure instead of an empty successful reply', async () => {
    const { runtime } = runtimeWithPlayer(0, false, 'AUTH')
    await expect(runtime.chat('world', 'player', '你好')).rejects.toMatchObject({
      status: 503,
      message: 'model authentication failed; configure a valid API key in Harness',
    })
  })

  it('projects live player conversation without private plugin feedback', () => {
    const { runtime, events } = runtimeWithPlayer(0)
    events.push(
      { type: 'user/message', seq: 0, time: 100, data: { content: [{ type: 'text', text: '给我石头' }], source: { kind: 'user' } } },
      { type: 'assistant/message', seq: 1, time: 101, data: { message: { content: [{ type: 'text', text: '正在处理' }] } } },
      { type: 'user/message', seq: 2, time: 102, data: { content: [{ type: 'text', text: 'private brigadier detail' }], source: { kind: 'plugin' } } },
      { type: 'assistant/message', seq: 3, time: 103, data: { message: { content: [{ type: 'text', text: '命令已修正' }] } } },
    )

    expect(runtime.conversation('world', 'player')).toEqual({
      revision: 3,
      messages: [
        { seq: 0, time: 100, role: 'player', text: '给我石头' },
        { seq: 1, time: 101, role: 'agent', text: '正在处理' },
        { seq: 3, time: 103, role: 'agent', text: '命令已修正' },
      ],
    })
    expect(runtime.conversation('world', 'player', 1).messages).toEqual([
      { seq: 3, time: 103, role: 'agent', text: '命令已修正' },
    ])
  })

  it('removes and disposes only the player that goes offline', async () => {
    const { runtime, dispose } = runtimeWithPlayer(0)
    await runtime.playerOffline('world', 'player')
    expect(dispose).toHaveBeenCalledOnce()
    expect((runtime.snapshot() as { worlds: Array<{ players: unknown[] }> }).worlds[0]?.players).toHaveLength(0)
  })
})
