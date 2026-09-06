package com.worldsoul.agent.conversation;

import com.worldsoul.agent.provider.ChatMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Per-player conversation history.
 */
public final class ConversationSession {

    private final UUID playerId;
    private final List<ChatMessage> history = new ArrayList<>();
    private final int maxHistory;

    public ConversationSession(UUID playerId, int maxHistory) {
        this.playerId = playerId;
        this.maxHistory = Math.max(2, maxHistory);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void add(ChatMessage message) {
        history.add(message);
        while (history.size() > maxHistory) {
            history.remove(0);
        }
    }

    public List<ChatMessage> getHistory() {
        return Collections.unmodifiableList(history);
    }
}
