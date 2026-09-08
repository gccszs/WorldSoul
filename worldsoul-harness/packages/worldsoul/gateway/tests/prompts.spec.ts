import { describe, expect, it } from 'vitest'
import { promptForMode } from '../src/prompts.ts'
import { usageStatus } from '../src/usage.ts'

describe('WorldSoul prompts', () => {
  it('keeps guide mode search-like and plain text', () => {
    const prompt = promptForMode('guide')
    expect(prompt).toContain('攻略搜索')
    expect(prompt).toContain('不使用 Markdown')
    expect(prompt).not.toContain('```')
  })

  it('keeps deity mode console-like and delegates commands to validation', () => {
    const prompt = promptForMode('deity')
    expect(prompt).toContain('受控的 Minecraft 控制台')
    expect(prompt).toContain('minecraft_command')
    expect(prompt).toContain('游戏端校验')
  })
})

describe('token threshold classification', () => {
  it('classifies normal, warning, and exhausted usage', () => {
    expect(usageStatus(79, 100, 0.8).state).toBe('normal')
    expect(usageStatus(80, 100, 0.8).state).toBe('warning')
    expect(usageStatus(100, 100, 0.8)).toMatchObject({ state: 'exhausted', disabled: true })
  })
})
