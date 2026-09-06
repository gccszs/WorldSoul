package com.worldsoul.agent.personality;

/**
 * WorldSoul god-mode personality presets from the product design.
 */
public enum GodMode {
    GUIDE("guide", "向导模式"),
    DEITY("deity", "神明降临"),
    TROLL("troll", "沙雕模式");

    private final String id;
    private final String displayName;

    GodMode(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parses a mode id such as guide/deity/troll.
     */
    public static GodMode fromId(String raw) {
        if (raw == null || raw.isBlank()) {
            return GUIDE;
        }
        String normalized = raw.trim().toLowerCase();
        for (GodMode mode : values()) {
            if (mode.id.equals(normalized) || mode.name().equalsIgnoreCase(normalized)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown mode: " + raw);
    }
}
