/** Token accounting over durable DeepSeek Harness session events. */

import type { Session } from '@deepseek-ai/dsh-session'
import type { UsageStatus } from './types.ts'

/**
 * Sum provider-reported usage without double-counting disjoint cache buckets.
 * @param session - Durable Agent session whose assistant events carry usage.
 * @returns Saturating sum of provider-reported tokens.
 */
export function usedTokens(session: Session): number {
  let total = 0
  for (const event of session.snapshotEvents()) {
    if (event.type !== 'assistant/message' || event.data.usage === undefined) continue
    const usage = event.data.usage
    const amount = usage.totalTokens ?? usage.inputTokens + usage.outputTokens
      + (usage.cacheReadTokens ?? 0) + (usage.cacheWriteTokens ?? 0)
    total = Math.min(Number.MAX_SAFE_INTEGER, total + amount)
  }
  return total
}

/**
 * Classify one agent against its configured hard limit and warning ratio.
 * @param used - Tokens consumed by the Agent.
 * @param limit - Positive configured hard limit.
 * @param warningRatio - Fraction at which warning state begins.
 * @returns Usage totals and derived warning or disabled state.
 */
export function usageStatus(used: number, limit: number, warningRatio: number): UsageStatus {
  const ratio = limit === 0 ? 1 : used / limit
  const state = used >= limit ? 'exhausted' : ratio >= warningRatio ? 'warning' : 'normal'
  return { used, limit, ratio, state, disabled: state === 'exhausted' }
}
