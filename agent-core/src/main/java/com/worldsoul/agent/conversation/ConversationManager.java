package com.worldsoul.agent.conversation;

import com.worldsoul.agent.config.AgentConfig;
import com.worldsoul.agent.personality.PersonalityEngine;
import com.worldsoul.agent.protocol.AgentAction;
import com.worldsoul.agent.protocol.AgentReply;
import com.worldsoul.agent.protocol.ReplyParser;
import com.worldsoul.agent.provider.AIProvider;
import com.worldsoul.agent.provider.ChatMessage;
import com.worldsoul.agent.provider.ChatResponse;
import com.worldsoul.agent.world.WorldBridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrates prompt build, model call, reply parse, and world execution.
 */
public final class ConversationManager {

    private final AgentConfig config;
    private final AIProvider provider;
    private final PersonalityEngine personalityEngine;
    private final ReplyParser replyParser;
    private final WorldBridge worldBridge;
    private final Map<UUID, ConversationSession> sessions = new ConcurrentHashMap<>();

    public ConversationManager(
            AgentConfig config,
            AIProvider provider,
            PersonalityEngine personalityEngine,
            ReplyParser replyParser,
            WorldBridge worldBridge) {
        this.config = config;
        this.provider = provider;
        this.personalityEngine = personalityEngine;
        this.replyParser = replyParser;
        this.worldBridge = worldBridge;
    }

    /**
     * Clears conversation history for a player/session.
     */
    public void clearSession(UUID playerId) {
        sessions.remove(playerId);
    }

    /**
     * Handles one player message asynchronously.
     */
    public CompletableFuture<TurnResult> handleMessage(UUID playerId, String userText, String gameContext) {
        ConversationSession session = sessions.computeIfAbsent(
                playerId,
                id -> new ConversationSession(id, config.getMaxHistory()));

        List<ChatMessage> prompt = new ArrayList<>();
        prompt.add(ChatMessage.system(personalityEngine.buildSystemPrompt(config.getMode())));
        if (gameContext != null && !gameContext.isBlank()) {
            prompt.add(ChatMessage.system("当前游戏状态:\n" + gameContext));
        }
        prompt.addAll(session.getHistory());
        prompt.add(ChatMessage.user(userText));

        return provider.chat(prompt).thenApply(response -> {
            AgentReply reply = replyParser.parse(response.getContent());
            List<String> actionResults = new ArrayList<>();
            for (AgentAction action : reply.getActions()) {
                actionResults.add(worldBridge.execute(action));
            }

            session.add(ChatMessage.user(userText));
            session.add(ChatMessage.assistant(response.getContent()));
            return new TurnResult(reply, response, actionResults);
        });
    }

    /**
     * Result of a single conversation turn.
     */
    public static final class TurnResult {
        private final AgentReply reply;
        private final ChatResponse response;
        private final List<String> actionResults;

        public TurnResult(AgentReply reply, ChatResponse response, List<String> actionResults) {
            this.reply = reply;
            this.response = response;
            this.actionResults = List.copyOf(actionResults);
        }

        public AgentReply getReply() {
            return reply;
        }

        public ChatResponse getResponse() {
            return response;
        }

        public List<String> getActionResults() {
            return actionResults;
        }
    }
}
