import { createServer, type Server } from 'node:http'
import type { AddressInfo } from 'node:net'
import { afterEach, describe, expect, it } from 'vitest'
import { transcribeAudio } from '../src/speech.ts'

let server: Server | undefined

afterEach(async () => {
  if (server === undefined) return
  await new Promise<void>((resolve, reject) => server!.close(error => error === undefined ? resolve() : reject(error)))
  server = undefined
})

async function provider(status: number, body: string): Promise<string> {
  server = createServer((request, response) => {
    expect(request.method).toBe('POST')
    expect(request.headers['content-type']).toContain('multipart/form-data')
    response.writeHead(status, { 'content-type': 'application/json' })
    response.end(body)
  })
  await new Promise<void>((resolve, reject) => {
    server!.once('error', reject)
    server!.listen(0, '127.0.0.1', resolve)
  })
  return `http://127.0.0.1:${String((server.address() as AddressInfo).port)}`
}

describe('WorldSoul speech adapter', () => {
  it('posts in-memory audio and returns a trimmed transcript', async () => {
    const endpoint = await provider(200, JSON.stringify({ text: '  你好世界  ' }))
    await expect(transcribeAudio({} as never, {
      speechEndpoint: endpoint,
      speechModel: 'whisper-1',
      speechCredentialEnv: '',
      speechTimeoutMs: 2_000,
    }, new Uint8Array([1, 2, 3]), 'audio/wav')).resolves.toBe('你好世界')
  })

  it('does not expose provider response details on rejection', async () => {
    const endpoint = await provider(500, JSON.stringify({ error: 'provider secret' }))
    await expect(transcribeAudio({} as never, {
      speechEndpoint: endpoint,
      speechModel: 'whisper-1',
      speechCredentialEnv: '',
      speechTimeoutMs: 2_000,
    }, new Uint8Array([1]), 'audio/wav')).rejects.toThrow('speech recognition provider rejected the request')
  })
})
