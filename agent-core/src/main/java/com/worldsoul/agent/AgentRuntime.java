package com.worldsoul.agent;

import com.worldsoul.agent.config.AgentConfig;
import com.worldsoul.agent.conversation.ConversationManager;
import com.worldsoul.agent.personality.PersonalityEngine;
import com.worldsoul.agent.protocol.ReplyParser;
import com.worldsoul.agent.provider.AIProvider;
import com.worldsoul.agent.provider.AIProviderFactory;
import com.worldsoul.agent.world.WorldBridge;

/**
 * Facade used by harness and mod to bootstrap the agent stack.
 */
public final class AgentRuntime {

    private final AgentConfig config;
    private final AIProvider provider;
    private final ConversationManager conversationManager;

    public AgentRuntime(AgentConfig config, WorldBridge worldBridge) {
        this.config = config;
        this.provider = AIProviderFactory.create(config);
        this.conversationManager = new ConversationManager(
                config,
                provider,
                new PersonalityEngine(),
                new ReplyParser(),
                worldBridge);
    }

    public AgentConfig getConfig() {
        return config;
    }

    public AIProvider getProvider() {
        return provider;
    }

    public ConversationManager getConversationManager() {
        return conversationManager;
    }
}
