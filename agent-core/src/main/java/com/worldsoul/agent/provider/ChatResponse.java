package com.worldsoul.agent.provider;

import java.util.Objects;

/**
 * Response returned by an {@link AIProvider}.
 */
public final class ChatResponse {

    private final String content;
    private final int promptTokens;
    private final int completionTokens;

    public ChatResponse(String content, int promptTokens, int completionTokens) {
        this.content = Objects.requireNonNull(content, "content");
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
    }

    public String getContent() {
        return content;
    }

    public int getPromptTokens() {
        return promptTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public int getTotalTokens() {
        return promptTokens + completionTokens;
    }
}
