package com.worldsoul.harness;

import java.util.Set;

/** Shared wire values accepted by the Mod and Harness mode endpoints. */
public final class AgentMode {
    private static final Set<String> SUPPORTED = Set.of("guide", "deity", "troll");

    private AgentMode() {}

    public static boolean isSupported(String mode) { return SUPPORTED.contains(mode); }
}
