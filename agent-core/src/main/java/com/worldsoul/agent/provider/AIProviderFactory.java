package com.worldsoul.agent.provider;

import com.worldsoul.agent.config.AgentConfig;

/**
 * Creates providers from configuration.
 */
public final class AIProviderFactory {

    private AIProviderFactory() {
    }

    /**
     * Resolves a provider. Unknown or missing keys fall back to echo.
     */
    public static AIProvider create(AgentConfig config) {
        String id = config.getProvider() == null ? "echo" : config.getProvider().trim().toLowerCase();
        return switch (id) {
            case "deepseek", "openai" -> new HttpChatProvider(config);
            default -> new EchoAIProvider();
        };
    }
}
