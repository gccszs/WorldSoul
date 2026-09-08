package com.worldsoul.agent.provider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Abstraction over LLM backends (DeepSeek, OpenAI, echo, etc.).
 */
public interface AIProvider {

    /**
     * Sends a chat completion request asynchronously.
     */
    CompletableFuture<ChatResponse> chat(List<ChatMessage> messages);

    /**
     * Provider id used in configuration.
     */
    String getId();
}
