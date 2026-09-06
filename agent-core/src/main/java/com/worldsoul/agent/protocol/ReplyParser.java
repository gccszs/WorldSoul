package com.worldsoul.agent.protocol;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses model text into speech + optional action lines.
 *
 * <p>Action lines use a simple scaffold protocol:
 * {@code [[action:give item=diamond_sword count=1]]}
 */
public final class ReplyParser {

    private static final Pattern ACTION_PATTERN = Pattern.compile(
            "\\[\\[action:([a-zA-Z_]+)([^\\]]*)]]");

    /**
     * Parses raw model content.
     */
    public AgentReply parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return new AgentReply("", List.of());
        }

        List<AgentAction> actions = new ArrayList<>();
        Matcher matcher = ACTION_PATTERN.matcher(raw);
        StringBuffer speechBuffer = new StringBuffer();
        while (matcher.find()) {
            String typeToken = matcher.group(1);
            String argsToken = matcher.group(2) == null ? "" : matcher.group(2).trim();
            actions.add(new AgentAction(parseType(typeToken), parseArguments(argsToken)));
            matcher.appendReplacement(speechBuffer, "");
        }
        matcher.appendTail(speechBuffer);

        return new AgentReply(speechBuffer.toString().trim(), actions);
    }

    private ActionType parseType(String token) {
        try {
            return ActionType.valueOf(token.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return ActionType.UNKNOWN;
        }
    }

    private Map<String, String> parseArguments(String rawArgs) {
        Map<String, String> args = new LinkedHashMap<>();
        if (rawArgs == null || rawArgs.isBlank()) {
            return args;
        }
        String[] parts = rawArgs.trim().split("\\s+");
        for (String part : parts) {
            int eq = part.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            args.put(part.substring(0, eq), part.substring(eq + 1));
        }
        return args;
    }
}
