/** WorldSoul wire and runtime value types. */

/** Supported WorldSoul behavior mode. */
export type WorldSoulMode = 'guide' | 'deity' | 'troll'

/** One non-empty inventory or equipment stack from Minecraft's item registry. */
export interface ItemStackSnapshot {
  readonly item: string
  readonly count: number
  readonly damage?: number
  readonly maxDamage?: number
}

/** One occupied main-inventory slot. */
export interface InventoryStackSnapshot extends ItemStackSnapshot {
  readonly slot: number
}

/** One occupied equipment slot. */
export interface EquipmentStackSnapshot extends ItemStackSnapshot {
  readonly slot: 'head' | 'chest' | 'legs' | 'feet' | 'mainhand' | 'offhand'
}

/** Version-neutral live player facts supplied by the Minecraft mod. */
export interface PlayerSnapshot {
  readonly dimension: string
  readonly x: number
  readonly y: number
  readonly z: number
  readonly yaw: number
  readonly pitch: number
  readonly facing: 'north' | 'south' | 'east' | 'west'
  readonly health: number
  readonly maxHealth: number
  readonly foodLevel: number
  readonly gameMode: string
  readonly onGround: boolean
  readonly selectedHotbarSlot: number
  readonly inventory: readonly InventoryStackSnapshot[]
  readonly equipment: readonly EquipmentStackSnapshot[]
}

/** A command proposed by the model and awaiting Minecraft-side validation. */
export interface CommandProposal {
  readonly id: string
  readonly command: string
  readonly rationale: string
}

/** Usage state returned to the mod and dashboard. */
export interface UsageStatus {
  readonly used: number
  readonly limit: number
  readonly ratio: number
  readonly state: 'normal' | 'warning' | 'exhausted'
  readonly disabled: boolean
}

/** One completed model turn returned to a Minecraft client. */
export interface TurnReply {
  readonly speech: string
  readonly commands: readonly CommandProposal[]
  readonly usage: UsageStatus
}

/** One plain-text, player-visible message projected from a durable Agent session. */
export interface ConversationMessage {
  readonly seq: number
  readonly time: number
  readonly role: 'player' | 'agent'
  readonly text: string
}

/** Incremental conversation projection consumed by the operator dashboard. */
export interface ConversationSnapshot {
  readonly revision: number
  readonly messages: readonly ConversationMessage[]
}
