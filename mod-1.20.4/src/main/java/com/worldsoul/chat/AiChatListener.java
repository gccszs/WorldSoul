package com.worldsoul.chat;

import com.worldsoul.harness.PlayerAgentManager;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

/**
 * Intercepts {@code @AI } chat messages and forwards them to harness.
 */
public final class AiChatListener {

    private static final String PREFIX = "@AI ";

    private final PlayerAgentManager agentManager;

    public AiChatListener(PlayerAgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            String content = message.getContent().getString();
            if (!isAiMessage(content)) {
                return true;
            }
            String userText = extractMessage(content);
            if (userText.isEmpty()) {
                return false;
            }
            // Keep the original chat line visible, then forward the question to harness.
            agentManager.chat(sender, userText);
            return true;
        });
    }

    static boolean isAiMessage(String content) {
        if (content == null) {
            return false;
        }
        String trimmed = content.stripLeading();
        return trimmed.length() >= PREFIX.length()
                && trimmed.regionMatches(true, 0, PREFIX, 0, PREFIX.length());
    }

    static String extractMessage(String content) {
        String trimmed = content.stripLeading();
        return trimmed.substring(PREFIX.length()).stripLeading();
    }
}
