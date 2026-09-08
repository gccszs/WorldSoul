package com.worldsoul.harness;

import java.util.List;

/** Minecraft-version-neutral player facts sent to the Harness before a turn. */
public final class PlayerSnapshot {
    private final String dimension;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final String facing;
    private final float health;
    private final float maxHealth;
    private final int foodLevel;
    private final String gameMode;
    private final boolean onGround;
    private final int selectedHotbarSlot;
    private final List<InventoryStack> inventory;
    private final List<EquipmentStack> equipment;

    public PlayerSnapshot(
            String dimension, double x, double y, double z, float yaw, float pitch, String facing,
            float health, float maxHealth, int foodLevel, String gameMode, boolean onGround,
            int selectedHotbarSlot, List<InventoryStack> inventory, List<EquipmentStack> equipment) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.facing = facing;
        this.health = health;
        this.maxHealth = maxHealth;
        this.foodLevel = foodLevel;
        this.gameMode = gameMode;
        this.onGround = onGround;
        this.selectedHotbarSlot = selectedHotbarSlot;
        this.inventory = List.copyOf(inventory);
        this.equipment = List.copyOf(equipment);
    }

    public static final class InventoryStack {
        private final int slot;
        private final String item;
        private final int count;
        private final Integer damage;
        private final Integer maxDamage;

        public InventoryStack(int slot, String item, int count, Integer damage, Integer maxDamage) {
            this.slot = slot;
            this.item = item;
            this.count = count;
            this.damage = damage;
            this.maxDamage = maxDamage;
        }
    }

    public static final class EquipmentStack {
        private final String slot;
        private final String item;
        private final int count;
        private final Integer damage;
        private final Integer maxDamage;

        public EquipmentStack(String slot, String item, int count, Integer damage, Integer maxDamage) {
            this.slot = slot;
            this.item = item;
            this.count = count;
            this.damage = damage;
            this.maxDamage = maxDamage;
        }
    }
}
