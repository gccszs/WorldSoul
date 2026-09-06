package com.worldsoul.command;

import java.util.Locale;
import java.util.Set;

/** Minecraft-version-independent lexical command policy applied before Brigadier parsing. */
public final class CommandPolicy {
    private static final int MAX_COMMAND_CHARS = 512;
    private static final Set<String> ALLOWED_ROOTS = Set.of(
            "advancement", "attribute", "clear", "clone", "damage", "effect", "enchant",
            "experience", "fill", "forceload", "function", "gamemode", "gamerule", "give",
            "item", "kill", "locate", "loot", "particle", "place", "playsound", "recipe",
            "ride", "say", "schedule", "scoreboard", "setblock", "setworldspawn", "spawnpoint",
            "spreadplayers", "stopsound", "summon", "tag", "team", "teleport", "tellraw",
            "time", "title", "tp", "weather", "worldborder");

    private CommandPolicy() {}

    /** Validate syntax-independent safety and return a private rejection reason. */
    public static Validation validate(String command, boolean deityMode) {
        if (!deityMode) return Validation.reject("commands are only available in deity mode");
        if (command == null || command.isBlank()) return Validation.reject("command is blank");
        if (command.length() > MAX_COMMAND_CHARS) return Validation.reject("command is too long");
        if (command.startsWith("/")) return Validation.reject("command must not start with slash");
        if (command.indexOf('\n') >= 0 || command.indexOf('\r') >= 0) return Validation.reject("command must be one line");
        String root = command.stripLeading().split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        if (!ALLOWED_ROOTS.contains(root)) return Validation.reject("command root is not allowed: " + root);
        return Validation.accept();
    }

    public static final class Validation {
        private final boolean accepted;
        private final String error;

        private Validation(boolean accepted, String error) { this.accepted = accepted; this.error = error; }
        public static Validation accept() { return new Validation(true, ""); }
        public static Validation reject(String error) { return new Validation(false, error); }
        public boolean isAccepted() { return accepted; }
        public String getError() { return error; }
    }
}
